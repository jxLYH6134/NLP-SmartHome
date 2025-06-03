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
        msg_id = esp_mqtt_client_subscribe(client, "/topic/receive", 0);
        ESP_LOGI(TAG, "订阅成功, msg_id=%d", msg_id);
        break;

    case MQTT_EVENT_DISCONNECTED:
        ESP_LOGI(TAG, "MQTT已断开连接");
        break;

    case MQTT_EVENT_DATA:
        ESP_LOGI(TAG, "收到MQTT数据");
        printf("主题=%.*s\r\n", event->topic_len, event->topic);
        printf("数据=%.*s\r\n", event->data_len, event->data);

        // 解析收到的数据
        if (event->data_len > 0) {
            cJSON *root = cJSON_Parse(event->data);
            if (root) {
                cJSON *cmd = cJSON_GetObjectItem(root, "command");
                cJSON *params = cJSON_GetObjectItem(root, "params");

                if (cJSON_IsString(cmd) && params &&
                    strcmp(cmd->valuestring, "control") == 0) {
                    // cJSON *id = cJSON_GetObjectItem(params, "lightId");

                    // if (cJSON_IsString(id) &&
                    //     strcmp(id->valuestring, lightId) == 0) {
                    //     cJSON *rgb = cJSON_GetObjectItem(params, "rgb_state");
                    //     cJSON *autoMod = cJSON_GetObjectItem(params, "autoMod");

                    //     if (cJSON_IsNumber(rgb) && cJSON_IsBool(autoMod)) {
                    //         uint8_t new_rgb = rgb->valueint;
                    //         rgb_state = new_rgb % 8;
                    //         auto_mode = cJSON_IsTrue(autoMod);
                    //         update_rgb_output(rgb_state);
                    //         ESP_LOGI(TAG, "更新状态: RGB=%d, 模式=%s",
                    //                  rgb_state, auto_mode ? "自动" : "手动");
                    //     }
                    // } else {
                    //     ESP_LOGE(TAG, "非本设备ID或格式错误");
                    // }
                }
                cJSON_Delete(root);
            } else {
                ESP_LOGE(TAG, "JSON解析失败");
            }
        }
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
