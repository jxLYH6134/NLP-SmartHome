#ifndef MQTT_H
#define MQTT_H

#include <stdint.h>

// 声明外部变量
extern const char *lightId;
extern bool auto_mode;
extern uint8_t rgb_state;

// 声明外部函数
extern void update_rgb_output(uint8_t color);

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
