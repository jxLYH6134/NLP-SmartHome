#ifndef NVS_VALUE_H
#define NVS_VALUE_H

#include <esp_err.h>

/**
 * @brief 初始化NVS闪存
 *
 * @return esp_err_t 初始化结果
 */
esp_err_t init_nvs(void);

/**
 * @brief 将字符串值写入NVS
 *
 * @param key 存储键名
 * @param value 要存储的字符串值
 */
void nvs_write(const char *key, const char *value);

/**
 * @brief 从NVS读取字符串值
 *
 * @param key 存储键名
 * @param value 用于存储读取到的字符串的缓冲区
 * @param max_length 缓冲区的最大长度
 * @return esp_err_t 读取操作的结果
 */
esp_err_t nvs_read(const char *key, char *value, size_t max_length);

#endif /* NVS_VALUE_H */