#include "nvs_value.h"
#include "esp_system.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "nvs.h"
#include "nvs_flash.h"
#include <stdio.h>
#include <string.h>

#define NVS_NAMESPACE "storage"
#define MAX_STRING_LENGTH 64

esp_err_t init_nvs(void) {
    esp_err_t err = nvs_flash_init();
    if (err == ESP_ERR_NVS_NO_FREE_PAGES ||
        err == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        err = nvs_flash_init();
    }
    return err;
}

void nvs_write(const char *key, const char *value) {
    nvs_handle_t my_handle;
    esp_err_t err;

    // 打开NVS句柄
    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &my_handle);
    if (err != ESP_OK) {
        printf("Error (%s) opening NVS handle!\n", esp_err_to_name(err));
        return;
    }

    // 写入字符串
    err = nvs_set_str(my_handle, key, value);
    printf("Writing %s to %s ... %s\n", value, key,
           (err != ESP_OK) ? "Failed!" : "Done");

    // 提交更改
    err = nvs_commit(my_handle);
    printf("Committing updates in NVS ... %s\n",
           (err != ESP_OK) ? "Failed!" : "Done");

    // 关闭句柄
    nvs_close(my_handle);
}

esp_err_t nvs_read(const char *key, char *value, size_t max_length) {
    nvs_handle_t my_handle;
    esp_err_t err;

    // 打开NVS句柄
    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &my_handle);
    if (err != ESP_OK) {
        printf("Error (%s) opening NVS handle!\n", esp_err_to_name(err));
        return err;
    }

    // 读取字符串
    err = nvs_get_str(my_handle, key, value, &max_length);
    if (err == ESP_OK) {
        printf("Reading %s from %s ... Done\n", value, key);
    } else {
        printf("Reading from %s ... Failed!\n", key);
    }

    // 关闭句柄
    nvs_close(my_handle);

    return err;
}

void nvs_write_int(const char *key, int8_t value) {
    nvs_handle_t my_handle;
    esp_err_t err;

    // 打开NVS句柄
    err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &my_handle);
    if (err != ESP_OK) {
        printf("Error (%s) opening NVS handle!\n", esp_err_to_name(err));
        return;
    }

    // 写入整数
    err = nvs_set_i8(my_handle, key, value);
    printf("Writing int %d to %s ... %s\n", value, key,
           (err != ESP_OK) ? "Failed!" : "Done");

    // 提交更改
    err = nvs_commit(my_handle);
    printf("Committing updates in NVS ... %s\n",
           (err != ESP_OK) ? "Failed!" : "Done");

    // 关闭句柄
    nvs_close(my_handle);
}

esp_err_t nvs_read_int(const char *key, int8_t *value) {
    nvs_handle_t my_handle;
    esp_err_t err;

    // 打开NVS句柄
    err = nvs_open(NVS_NAMESPACE, NVS_READONLY, &my_handle);
    if (err != ESP_OK) {
        printf("Error (%s) opening NVS handle!\n", esp_err_to_name(err));
        return err;
    }

    // 读取整数
    err = nvs_get_i8(my_handle, key, value);
    if (err == ESP_OK) {
        printf("Read int %d from key %s ... Done\n", *value, key);
    } else {
        printf("Reading from %s ... Failed!\n", key);
    }

    // 关闭句柄
    nvs_close(my_handle);

    return err;
}