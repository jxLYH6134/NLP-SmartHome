# MQTT设备实时状态控制GUI

这是一个图形用户界面应用程序，用于实时控制和监控MQTT设备模拟器的状态。

**mqtt_realtime_gui.py** - 实时状态控制面板：直接控制模拟器内部状态，实时显示和编辑设备参数

## 功能特性

- 🖥️ 直观的图形界面，支持实时修改设备参数
- 🎛️ 多种控制组件：滑块、输入框、复选框、下拉菜单
- 📡 内置MQTT模拟器，无需单独启动
- 🔄 实时状态显示，修改即时生效
- 💾 配置保存功能，可将状态保存回config.json
- 🎮 启动/停止控制，随时管理模拟器运行状态
- 📱 滚动界面，支持大量设备显示

## 依赖关系

**重要说明**：`mqtt_realtime_gui.py` 依赖于 `mqtt_simulator.py` 基础模块。GUI内部会创建和管理MQTTSimulator实例，实现设备状态的实时控制和显示。

## 安装和运行

### 方法1：使用批处理文件
```bash
# 双击运行
start_realtime_gui.bat
```

### 方法2：命令行运行
```bash
# 确保已安装依赖
pip install -r requirements.txt

# 启动实时GUI
python mqtt_realtime_gui.py
```

## 界面说明

### 主要功能区域

1. **控制按钮区**：
   - **启动模拟器**：启动内置MQTT模拟器
   - **停止模拟器**：停止正在运行的模拟器
   - **保存配置**：将当前设备状态保存到config.json文件

2. **状态显示**：实时显示模拟器运行状态和连接状态

3. **设备控制面板**：为每个设备提供实时参数控制

### 设备控制面板
每个设备都有独立的控制面板，包含：

1. **设备信息**：显示设备类型和设备ID
2. **实时参数控制**：
   - **布尔值参数**：使用复选框（如电源开关、门开关等）
   - **数值参数**：使用输入框+滑块组合（如温度、湿度等）
   - **字符串参数**：使用下拉菜单（如工作模式等）
3. **即时生效**：修改参数后立即更新到模拟器内部状态，无需额外操作

### 参数类型和范围

- **温度参数** (`temperature`, `currentTemp`, `targetTemp`, `insideTemp`): -10°C 到 50°C
- **百分比参数** (`humidity`, `brightness`, `airQuality`, `illumination`, `waterLevel`): 0% 到 100%
- **风速等级** (`fanSpeed`): 1 到 5
- **色温** (`colorTemperature`): 2700K 到 6500K
- **浓度** (`concentration`): 0 到 1000
- **紫外线指数** (`UV`): 0 到 15

### 运行状态
- **模拟器未启动**：红色显示，模拟器处于停止状态
- **模拟器运行中**：绿色显示，模拟器正常运行并连接MQTT服务器
- **正在启动**：橙色显示，模拟器正在初始化

## 支持的设备类型

### 1. 冰箱 (refrigerator)
- `freeze`: 冷冻开关
- `opened`: 门开关状态
- `targetTemp`: 目标温度
- `insideTemp`: 内部温度
- `insideHumi`: 内部湿度

### 2. 空调 (airConditioner)
- `power`: 电源开关
- `mode`: 工作模式（制冷/制热/送风/除湿）
- `targetTemp`: 目标温度
- `fanSpeed`: 风速等级

### 3. 灯具 (lamp)
- `power`: 电源开关
- `colorTemperature`: 色温
- `brightness`: 亮度

### 4. 环境传感器 (envSensor)
- `temperature`: 温度
- `humidity`: 湿度
- `UV`: 紫外线指数
- `airQuality`: 空气质量
- `illumination`: 照度

### 5. 燃气检测器 (gasDetector)
- `concentration`: 燃气浓度

### 6. 空气净化器 (airPurifier)
- `power`: 电源开关

## 使用步骤

1. **启动应用程序**：运行 `start_realtime_gui.bat` 或 `python mqtt_realtime_gui.py`
2. **启动模拟器**：点击"启动模拟器"按钮，等待连接到MQTT服务器
3. **实时控制**：直接修改设备参数，更改会立即生效
4. **查看状态**：观察设备状态的实时变化
5. **保存配置**：如需保存当前状态，点击"保存配置"按钮
6. **停止模拟器**：使用完毕后点击"停止模拟器"按钮

## 注意事项

- 确保网络连接正常，能够访问MQTT服务器 `crychic.link:1883`
- 修改参数后会立即在模拟器中生效，下次心跳会发送新数值
- 设备配置来源于 `config.json` 文件
- 关闭窗口时会自动停止模拟器并断开连接
- GUI依赖于 `mqtt_simulator.py` 模块，确保该文件存在

## 故障排除

### 模拟器启动失败
- 检查网络连接
- 确认MQTT服务器地址和端口正确
- 检查防火墙设置
- 确保 `mqtt_simulator.py` 文件存在

### 参数修改无效
- 确认模拟器已启动
- 检查参数值是否在有效范围内
- 重启模拟器

### 界面显示异常
- 检查 `config.json` 文件格式
- 确认Python和tkinter环境正常
- 确保所有依赖文件存在
- 重启应用程序