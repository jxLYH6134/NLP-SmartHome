import json
import random
import time
import threading
from datetime import datetime
import paho.mqtt.client as mqtt

class MQTTSimulator:
    def __init__(self, config_file="config.json"):
        self.config_file = config_file
        self.devices = []
        self.client = mqtt.Client()
        self.broker_host = "crychic.link"
        self.broker_port = 1883
        self.status_topic = "/status"
        self.control_topic = "/control"
        self.running = False
        
        # 设置MQTT回调函数
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        
        self.load_config()
    
    def load_config(self):
        """加载设备配置"""
        try:
            with open(self.config_file, 'r', encoding='utf-8') as f:
                self.devices = json.load(f)
            print(f"已加载 {len(self.devices)} 个设备配置")
        except FileNotFoundError:
            print(f"配置文件 {self.config_file} 不存在")
        except json.JSONDecodeError:
            print(f"配置文件 {self.config_file} 格式错误")
    
    def generate_random_value(self, param_config):
        """根据配置生成随机值"""
        if isinstance(param_config, dict) and "min" in param_config and "max" in param_config:
            # 如果是范围配置，生成随机数
            min_val = param_config["min"]
            max_val = param_config["max"]
            if isinstance(min_val, int) and isinstance(max_val, int):
                return random.randint(min_val, max_val)
            else:
                return round(random.uniform(min_val, max_val), 2)
        else:
            # 如果是固定值，直接返回
            return param_config
    
    def generate_device_status(self, device):
        """生成设备状态数据"""
        status = {
            "command": "status",
            "deviceId": device["deviceId"],
            "type": device["type"],
            "params": {}
        }
        
        # 处理参数，生成随机值或使用固定值
        for key, value in device["params"].items():
            status["params"][key] = self.generate_random_value(value)
        
        return status
    
    def on_connect(self, client, userdata, flags, rc):
        """MQTT连接回调"""
        if rc == 0:
            print("成功连接到MQTT服务器")
            # 订阅控制主题
            client.subscribe(self.control_topic)
            print(f"已订阅控制主题: {self.control_topic}")
        else:
            print(f"连接失败，错误代码: {rc}")
    
    def on_message(self, client, userdata, msg):
        """接收到MQTT消息的回调"""
        try:
            message = json.loads(msg.payload.decode('utf-8'))
            
            # 查找对应的设备并更新参数
            device_id = message.get("deviceId")
            if device_id:
                for device in self.devices:
                    if device["deviceId"] == device_id:
                        # 更新设备参数
                        if "params" in message:
                            for key, value in message["params"].items():
                                if key in device["params"]:
                                    # 如果原来是范围配置，保持范围配置格式
                                    if isinstance(device["params"][key], dict) and "min" in device["params"][key]:
                                        # 对于范围配置，我们可以选择更新目标值或直接设置固定值
                                        # 这里选择设置为固定值
                                        device["params"][key] = value
                                    else:
                                        device["params"][key] = value
                                    print(f"设备 {device_id} 的参数 {key} 已更新为 {value}")
                        break
        except json.JSONDecodeError:
            print(f"无法解析控制命令: {msg.payload.decode('utf-8')}")
        except Exception as e:
            print(f"处理控制命令时出错: {e}")
    
    def publish_status(self):
        """发布设备状态"""
        while self.running:
            for device in self.devices:
                status = self.generate_device_status(device)
                message = json.dumps(status, ensure_ascii=False)
                
                result = self.client.publish(self.status_topic, message)
            # 等待指定时间后再次发送
            time.sleep(3)  # 每3秒发送一次
    
    def start(self):
        """启动模拟器"""
        try:
            # 连接到MQTT服务器
            print(f"正在连接到 {self.broker_host}:{self.broker_port}")
            self.client.connect(self.broker_host, self.broker_port, 60)
            
            # 启动MQTT客户端循环
            self.client.loop_start()
            
            # 等待连接建立
            time.sleep(2)
            
            # 开始发送状态数据
            self.running = True
            status_thread = threading.Thread(target=self.publish_status)
            status_thread.daemon = True
            status_thread.start()
            
            print("MQTT模拟器已启动，按 Ctrl+C 停止")
            
            # 保持主线程运行
            while True:
                time.sleep(1)
                
        except KeyboardInterrupt:
            print("\n正在停止模拟器...")
            self.stop()
        except Exception as e:
            print(f"启动失败: {e}")
    
    def stop(self):
        """停止模拟器"""
        self.running = False
        self.client.loop_stop()
        self.client.disconnect()
        print("模拟器已停止")

if __name__ == "__main__":
    simulator = MQTTSimulator()
    simulator.start()