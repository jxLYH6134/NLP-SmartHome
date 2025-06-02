// #include "cJSON.h"
#include "dht11.h"
// #include "driver/adc.h"
#include "driver/gpio.h"
#include "driver/ledc.h"
#include "esp_event.h"
#include "esp_log.h"
// #include "esp_timer.h"
#include "freertos/FreeRTOS.h"
// #include "freertos/task.h"
// #include "mqtt.h"
#include "nvs_flash.h"
// #include "nvs_value.h"
// #include "smartconfig.h"
// #include <stdio.h>

#define TAG "Refrigerator"

// GPIO定义
#define UP_BTN GPIO_NUM_16
#define DN_BTN GPIO_NUM_17
#define SETTING_BTN GPIO_NUM_18
#define LED_GPIO GPIO_NUM_21
#define HALL_GPIO GPIO_NUM_22
#define DHT11_OUT GPIO_NUM_23
#define BUZZER_SIGNAL GPIO_NUM_32 // PWM
#define RELAY_GPIO GPIO_NUM_33

static int relay_state = 0;

typedef struct {
    gpio_num_t pin;
    bool last_level;
} button_t;

button_t up_btn = {UP_BTN, true};
button_t dn_btn = {DN_BTN, true};
button_t setting_btn = {SETTING_BTN, true};
dht11_t dht11_sensor;

// 初始化GPIO
void init_gpio() {
    gpio_set_direction(UP_BTN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(UP_BTN, GPIO_PULLUP_ONLY);
    gpio_set_direction(DN_BTN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(DN_BTN, GPIO_PULLUP_ONLY);
    gpio_set_direction(SETTING_BTN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(SETTING_BTN, GPIO_PULLUP_ONLY);

    gpio_set_direction(HALL_GPIO, GPIO_MODE_INPUT);
    gpio_set_direction(LED_GPIO, GPIO_MODE_OUTPUT);
    gpio_set_direction(RELAY_GPIO, GPIO_MODE_OUTPUT);
    gpio_set_level(RELAY_GPIO, relay_state);
}

// 初始化 DHT11
void init_dht11(void) { dht11_sensor.dht11_pin = DHT11_OUT; }

// 初始化PWM
void init_buzzer(void) {
    ledc_timer_config_t ledc_timer = {.speed_mode = LEDC_LOW_SPEED_MODE,
                                      .duty_resolution = LEDC_TIMER_3_BIT,
                                      .timer_num = LEDC_TIMER_0,
                                      .freq_hz = 1000, // 占位默认
                                      .clk_cfg = LEDC_USE_REF_TICK};
    ledc_timer_config(&ledc_timer);

    ledc_channel_config_t ledc_channel = {.gpio_num = BUZZER_SIGNAL,
                                          .speed_mode = LEDC_LOW_SPEED_MODE,
                                          .channel = LEDC_CHANNEL_0,
                                          .timer_sel = LEDC_TIMER_0,
                                          .duty = 0,
                                          .hpoint = 0};
    ledc_channel_config(&ledc_channel);
}

// 蜂鸣器
void buzzer_beep(uint16_t freq_hz, uint16_t duration_ms, uint8_t duty) {
    if (freq_hz > 5500)
        freq_hz = 5500;
    if (freq_hz < 150)
        freq_hz = 150;
    if (duty > 7)
        duty = 7;
    if (duty < 1)
        duty = 1;

    // 设置频率
    ledc_set_freq(LEDC_LOW_SPEED_MODE, LEDC_TIMER_0, freq_hz);

    // 启动 PWM
    ledc_set_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0, duty);
    ledc_update_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0);

    // 延时
    vTaskDelay(pdMS_TO_TICKS(duration_ms));

    // 关闭 PWM
    ledc_set_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0, 0);
    ledc_update_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0);
}

// 温湿度轮询任务 (2s)
void dht11_task(void *param) {
    while (1) {
        if (!dht11_read(&dht11_sensor, 5)) {
            printf("[Temperature]> %.1fC\n", dht11_sensor.temperature);
            printf("[Humidity]> %.1f%%\n", dht11_sensor.humidity);
        }
        vTaskDelay(2000 / portTICK_PERIOD_MS);
    }
}

// 上升沿检测
bool button_pressed(button_t *btn) {
    bool current = gpio_get_level(btn->pin);
    bool pressed = (btn->last_level == true && current == false);
    btn->last_level = current;
    return pressed;
}

// 按钮轮询任务 (10ms)
void button_task(void *arg) {
    TickType_t setting_press_start = 0;
    bool setting_pressed = false;

    while (1) {
        if (button_pressed(&setting_btn)) {
            setting_press_start = xTaskGetTickCount();
            setting_pressed = true;
        } else if (setting_pressed) {
            if (gpio_get_level(SETTING_BTN)) {
                setting_pressed = false;
                // GPIO_NUM_18 Short
                buzzer_beep(523, 100, 1); // D5
                relay_state = !relay_state;
                gpio_set_level(RELAY_GPIO, relay_state);
            } else {
                if (xTaskGetTickCount() - setting_press_start >=
                    pdMS_TO_TICKS(3000)) {
                    // GPIO_NUM_18 Long
                    ESP_LOGE(TAG, "还原设置并重启");
                    buzzer_beep(659, 100, 1);
                    buzzer_beep(587, 100, 1);
                    buzzer_beep(523, 100, 1);
                    nvs_flash_erase();
                    vTaskDelay(pdMS_TO_TICKS(2000));
                    esp_restart();
                }
            }
        }

        if (button_pressed(&up_btn)) {
            // GPIO_NUM_16
            buzzer_beep(523, 100, 1); // C5
            vTaskDelay(pdMS_TO_TICKS(50));
            buzzer_beep(659, 100, 1); // E5
        }

        if (button_pressed(&dn_btn)) {
            // GPIO_NUM_17
            buzzer_beep(659, 100, 1); // E5
            vTaskDelay(pdMS_TO_TICKS(50));
            buzzer_beep(523, 100, 1); // C5
        }

        vTaskDelay(pdMS_TO_TICKS(10));
    }
}

void hall_sensor_task(void *param) {
    const TickType_t alarm_delay = pdMS_TO_TICKS(3000);
    static TickType_t high_start_time = 0;
    static bool alarm_triggered = false;

    while (1) {
        bool current_state = gpio_get_level(HALL_GPIO);

        // LED 始终跟随 HALL 电平
        gpio_set_level(LED_GPIO, current_state);

        if (current_state) { // 高电平
            if (high_start_time == 0) {
                high_start_time = xTaskGetTickCount();
            } else if (!alarm_triggered &&
                       (xTaskGetTickCount() - high_start_time) >= alarm_delay) {
                alarm_triggered = true;

                // 设置 2500Hz 持续蜂鸣
                ledc_set_freq(LEDC_LOW_SPEED_MODE, LEDC_TIMER_0, 2500);
                ledc_set_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0, 7);
                ledc_update_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0);

                // 持续检查直到电平变低
                while (gpio_get_level(HALL_GPIO)) {
                    vTaskDelay(pdMS_TO_TICKS(50));
                }

                // 关闭蜂鸣器
                ledc_set_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0, 0);
                ledc_update_duty(LEDC_LOW_SPEED_MODE, LEDC_CHANNEL_0);
            }
        } else {
            high_start_time = 0;
            alarm_triggered = false;
        }

        vTaskDelay(pdMS_TO_TICKS(50));
    }
}

void app_main(void) {
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    init_gpio();
    init_buzzer();
    init_dht11();

    vTaskDelay(pdMS_TO_TICKS(1000));
    buzzer_beep(523, 100, 1);
    buzzer_beep(587, 100, 1);
    buzzer_beep(659, 100, 1);

    xTaskCreate(dht11_task, "dht11_task", 2048, NULL, 5, NULL);
    xTaskCreate(button_task, "button_task", 2048, NULL, 3, NULL);
    xTaskCreate(hall_sensor_task, "hall_sensor", 2048, NULL, 4, NULL);
}
