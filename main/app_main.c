#include "cJSON.h"
#include "driver/adc.h"
#include "driver/gpio.h"
#include "driver/ledc.h"
#include "esp_event.h"
#include "esp_log.h"
#include "esp_timer.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "mqtt.h"
#include "nvs_flash.h"
#include "nvs_value.h"
#include "smartconfig.h"
#include <stdio.h>

#define TAG "LIGHT_CTRL"

// GPIO定义
#define COLOR_BTN GPIO_NUM_16
#define CONTROL_BTN GPIO_NUM_17
#define SETTING_BTN GPIO_NUM_18
#define BUZZER_GPIO GPIO_NUM_32 // PWM

typedef struct {
    gpio_num_t pin;
    bool last_level;
} button_t;

button_t color_btn = {COLOR_BTN, true};
button_t control_btn = {CONTROL_BTN, true};
button_t setting_btn = {SETTING_BTN, true};

// 初始化GPIO
void init_gpio() {
    gpio_set_direction(COLOR_BTN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(COLOR_BTN, GPIO_PULLUP_ONLY);
    gpio_set_direction(CONTROL_BTN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(CONTROL_BTN, GPIO_PULLUP_ONLY);
    gpio_set_direction(SETTING_BTN, GPIO_MODE_INPUT);
    gpio_set_pull_mode(SETTING_BTN, GPIO_PULLUP_ONLY);
}

// 初始化PWM
void init_buzzer(void) {
    ledc_timer_config_t ledc_timer = {.speed_mode = LEDC_LOW_SPEED_MODE,
                                      .duty_resolution = LEDC_TIMER_3_BIT,
                                      .timer_num = LEDC_TIMER_0,
                                      .freq_hz = 1000, // 占位默认
                                      .clk_cfg = LEDC_USE_REF_TICK};
    ledc_timer_config(&ledc_timer);

    ledc_channel_config_t ledc_channel = {.gpio_num = BUZZER_GPIO,
                                          .speed_mode = LEDC_LOW_SPEED_MODE,
                                          .channel = LEDC_CHANNEL_0,
                                          .timer_sel = LEDC_TIMER_0,
                                          .duty = 0,
                                          .hpoint = 0};
    ledc_channel_config(&ledc_channel);
}

// 蜂鸣器
void buzzer_beep(uint32_t freq_hz, uint8_t duty, uint32_t duration_ms) {
    if (duty > 7)
        duty = 7;

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

// 上升沿检测
bool button_pressed(button_t *btn) {
    bool current = gpio_get_level(btn->pin);
    bool pressed = (btn->last_level == true && current == false);
    btn->last_level = current;
    return pressed;
}

// 按钮轮询任务（10ms周期）
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
                buzzer_beep(1000, 3000, 8);
            } else {
                if (xTaskGetTickCount() - setting_press_start >=
                    pdMS_TO_TICKS(3000)) {
                    // GPIO_NUM_18 Long
                    ESP_LOGE(TAG, "还原设置并重启");
                    nvs_flash_erase();
                    esp_restart();
                }
            }
        }

        if (button_pressed(&color_btn)) {
            // GPIO_NUM_16
            buzzer_beep(3000, 3000, 4);
        }

        if (button_pressed(&control_btn)) {
            // GPIO_NUM_17
            buzzer_beep(2000, 3000, 2);
        }

        vTaskDelay(pdMS_TO_TICKS(10));
    }
}

void app_main(void) {
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    init_gpio();
    init_buzzer();

    xTaskCreate(button_task, "button_task", 2048, NULL, 5, NULL);
}
