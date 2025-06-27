#include "cJSON.h"
#include "esp_event.h"
#include "esp_netif.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

#include "freertos/FreeRTOS.h"
#include "freertos/queue.h"
#include "freertos/semphr.h"
#include "freertos/task.h"

#include "lwip/dns.h"
#include "lwip/netdb.h"
#include "lwip/sockets.h"

#include "esp_log.h"
#include "mqtt.h"
#include "mqtt_client.h"

#define TAG "MQTT"

static esp_mqtt_client_handle_t client = NULL;

static void log_error_if_nonzero(const char *message, int error_code) {
    if (error_code != 0) {
        ESP_LOGE(TAG, "Last error %s: 0x%x", message, error_code);
    }
}

static void mqtt_event_handler(void *handler_args, esp_event_base_t base,
                               int32_t event_id, void *event_data) {
    esp_mqtt_event_handle_t event = event_data;
    esp_mqtt_client_handle_t client = event->client;
    int msg_id;
    switch ((esp_mqtt_event_id_t)event_id) {
    case MQTT_EVENT_CONNECTED:
        ESP_LOGI(TAG, "MQTT已连接");
        // 连接后订阅主题
        msg_id = esp_mqtt_client_subscribe(client, "/control", 0);
        ESP_LOGI(TAG, "订阅成功, msg_id=%d", msg_id);
        break;

    case MQTT_EVENT_DISCONNECTED:
        ESP_LOGI(TAG, "MQTT已断开连接");
        break;

    case MQTT_EVENT_DATA:
        ESP_LOGI(TAG, "收到MQTT数据: %.*s", event->data_len, event->data);

        if (event->data_len <= 0)
            break;

        cJSON *root = cJSON_Parse(event->data);
        if (!root) {
            ESP_LOGE(TAG, "JSON解析失败");
            break;
        }

        // 验证deviceId
        cJSON *deviceIdJson = cJSON_GetObjectItem(root, "deviceId");
        extern const char *deviceId;
        if (!cJSON_IsString(deviceIdJson) ||
            strcmp(deviceIdJson->valuestring, deviceId) != 0) {
            ESP_LOGE(TAG, "设备ID验证失败");
            cJSON_Delete(root);
            break;
        }

        // 处理参数
        cJSON *params = cJSON_GetObjectItem(root, "params");
        if (params) {
            extern int8_t target_temp, relay_state;
            extern void nvs_write_int(const char *key, int32_t value);
            extern void gpio_set_level(int gpio_num, uint32_t level);

            // 处理targetTemp
            cJSON *targetTempJson = cJSON_GetObjectItem(params, "targetTemp");
            if (cJSON_IsNumber(targetTempJson)) {
                int temp = targetTempJson->valueint;
                if (temp >= 0 && temp <= 20) {
                    target_temp = temp;
                    nvs_write_int("target_temp", target_temp);
                    ESP_LOGI(TAG, "更新目标温度: %d°C", target_temp);
                } else {
                    ESP_LOGE(TAG, "目标温度超出范围(0-20°C): %d", temp);
                }
            }

            // 处理freeze
            cJSON *freezeJson = cJSON_GetObjectItem(params, "freeze");
            if (cJSON_IsBool(freezeJson)) {
                relay_state = cJSON_IsTrue(freezeJson) ? 1 : 0;
                nvs_write_int("relay_state", relay_state);
                gpio_set_level(33, relay_state);
                ESP_LOGI(TAG, "更新制冷状态: %s",
                         relay_state ? "开启" : "关闭");
            }
        }

        cJSON_Delete(root);
        break;

    case MQTT_EVENT_ERROR:
        ESP_LOGI(TAG, "MQTT错误");
        if (event->error_handle->error_type == MQTT_ERROR_TYPE_TCP_TRANSPORT) {
            log_error_if_nonzero("TLS错误",
                                 event->error_handle->esp_tls_last_esp_err);
            log_error_if_nonzero("传输层错误",
                                 event->error_handle->esp_transport_sock_errno);
        }
        break;

    default:
        break;
    }
}

// 发布消息到指定主题
void mqtt_publish(const char *topic, const char *data) {
    if (client) {
        int msg_id = esp_mqtt_client_publish(client, topic, data, 0, 0, 0);
    }
}

static void mqtt_app_start(void) {
    esp_mqtt_client_config_t mqtt_cfg = {
        .broker.address.uri = "mqtt://crychic.link:1883",
    };

    client = esp_mqtt_client_init(&mqtt_cfg);
    esp_mqtt_client_register_event(client, ESP_EVENT_ANY_ID, mqtt_event_handler,
                                   NULL);
    esp_mqtt_client_start(client);
}

// 原app_main
void init_mqtt(void) {
    ESP_LOGI(TAG, "MQTT 启动中...");

    // 初始化系统组件
    ESP_ERROR_CHECK(esp_netif_init());

    // 启动MQTT客户端
    mqtt_app_start();
}
