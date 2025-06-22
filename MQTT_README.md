# SmartHome MQTT 功能说明

## 概述
本系统已集成MQTT功能，用于与IoT设备进行实时通信。

## MQTT配置
- **服务器地址**: mqtt://crychic.link:1883
- **订阅主题**: /status (接收设备状态和警告)
- **发布主题**: /control (发送控制命令)

## 消息格式

### 1. 设备状态更新 (IoT设备 → 服务器)
**主题**: /status
```json
{
    "command": "status",
    "deviceId": "114514",
    "type": "refrigerator",
    "params": {
        "freeze": true,
        "opened": false,
        "targetTemp": 20
    }
}
```
- 系统会自动更新数据库中对应设备的参数
- 如果设备不存在，会自动创建新设备

### 2. 设备警告 (IoT设备 → 服务器)
**主题**: /status
```json
{
    "command": "warning",
    "deviceId": "114514",
    "params": "冰箱门长时间开启！"
}
```
- 系统会自动发送通知到配置的通知API

### 3. 设备控制 (服务器 → IoT设备)
**主题**: /control
```json
{
    "command": "control",
    "deviceId": "114514",
    "params": {
        "freeze": false
    }
}
```

## API接口

### 发送控制命令
**POST** `/api/device/control`

请求体:
```json
{
    "deviceId": "114514",
    "params": {
        "freeze": true,
        "targetTemp": 25
    }
}
```
- params中可以只包含需要更新的字段

## 功能特性

1. **自动重连**: MQTT客户端支持自动重连
2. **异常处理**: MQTT发送失败不会影响数据库操作
3. **实时监听**: 系统启动后自动订阅状态主题
4. **设备自动创建**: 收到未知设备状态时自动创建设备记录
5. **通知集成**: 设备警告自动发送到通知系统

## 日志
系统会记录以下MQTT相关日志:
- 连接状态
- 消息接收和发送
- 错误信息

查看日志以监控MQTT功能运行状态。