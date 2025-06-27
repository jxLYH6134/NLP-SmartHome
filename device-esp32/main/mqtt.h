#ifndef MQTT_H
#define MQTT_H

#include "dht11.h"
#include <stdint.h>

// 声明外部变量
extern const char *deviceId;
extern dht11_t dht11_sensor;
extern int8_t target_temp;
extern int8_t relay_state;

/**
 * @brief 初始化MQTT客户端
 */
void init_mqtt(void);

/**
 * @brief 发布消息到指定的MQTT主题
 *
 * @param topic 目标主题
 * @param data 要发布的消息内容
 */
void mqtt_publish(const char *topic, const char *data);

#endif /* MQTT_H */
