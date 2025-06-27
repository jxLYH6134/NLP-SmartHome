import tkinter as tk
from tkinter import ttk, messagebox
import json
import threading
import time
from mqtt_simulator import MQTTSimulator

class MQTTRealtimeGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("MQTT模拟器")
        self.root.geometry("480x820")
        
        # 初始化MQTT模拟器
        self.simulator = None
        self.simulator_thread = None
        self.running = False
        
        # 创建界面
        self.create_widgets()
        
        # 启动定时更新
        self.update_display()
    
    def create_widgets(self):
        """创建界面组件"""
        # 标题
        title_label = tk.Label(self.root, text="MQTT模拟器", font=("Arial", 16, "bold"))
        title_label.pack(pady=10)
        
        # 控制按钮框架
        control_frame = ttk.Frame(self.root)
        control_frame.pack(pady=10)
        
        # 启动/停止按钮
        self.start_button = ttk.Button(control_frame, text="启动模拟器", command=self.start_simulator)
        self.start_button.pack(side=tk.LEFT, padx=5)
        
        self.stop_button = ttk.Button(control_frame, text="停止模拟器", command=self.stop_simulator, state="disabled")
        self.stop_button.pack(side=tk.LEFT, padx=5)
        
        # 保存配置按钮
        self.save_button = ttk.Button(control_frame, text="保存配置", command=self.save_config)
        self.save_button.pack(side=tk.LEFT, padx=5)
        
        # 状态标签
        self.status_label = tk.Label(self.root, text="状态: 模拟器未启动", fg="red")
        self.status_label.pack(pady=5)
        
        # 创建主框架
        main_frame = ttk.Frame(self.root)
        main_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # 创建滚动框架
        canvas = tk.Canvas(main_frame)
        scrollbar = ttk.Scrollbar(main_frame, orient="vertical", command=canvas.yview)
        self.scrollable_frame = ttk.Frame(canvas)
        
        self.scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(scrollregion=canvas.bbox("all"))
        )
        
        canvas.create_window((0, 0), window=self.scrollable_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)
        
        canvas.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")
        
        # 设备控制面板容器
        self.device_frames = {}
    
    def start_simulator(self):
        """启动MQTT模拟器"""
        try:
            self.simulator = MQTTSimulator()
            self.running = True
            
            # 在单独线程中启动模拟器
            self.simulator_thread = threading.Thread(target=self.run_simulator)
            self.simulator_thread.daemon = True
            self.simulator_thread.start()
            
            # 更新界面状态
            self.start_button.config(state="disabled")
            self.stop_button.config(state="normal")
            self.status_label.config(text="状态: 模拟器运行中", fg="green")
            
            # 创建设备控制面板
            self.create_device_panels()
            
        except Exception as e:
            messagebox.showerror("错误", f"启动模拟器失败: {e}")
    
    def run_simulator(self):
        """在线程中运行模拟器"""
        try:
            # 连接到MQTT服务器
            self.simulator.client.connect(self.simulator.broker_host, self.simulator.broker_port, 60)
            self.simulator.client.loop_start()
            
            # 等待连接建立
            time.sleep(2)
            
            # 开始发送状态数据
            self.simulator.running = True
            status_thread = threading.Thread(target=self.simulator.publish_status)
            status_thread.daemon = True
            status_thread.start()
            
            # 保持运行
            while self.running:
                time.sleep(1)
                
        except Exception as e:
            print(f"模拟器运行错误: {e}")
    
    def stop_simulator(self):
        """停止MQTT模拟器"""
        self.running = False
        if self.simulator:
            self.simulator.stop()
        
        # 更新界面状态
        self.start_button.config(state="normal")
        self.stop_button.config(state="disabled")
        self.status_label.config(text="状态: 模拟器已停止", fg="red")
        
        # 清空设备面板
        for widget in self.scrollable_frame.winfo_children():
            widget.destroy()
        self.device_frames.clear()
    
    def create_device_panels(self):
        """创建设备控制面板"""
        if not self.simulator or not self.simulator.devices:
            return
        
        # 清空现有面板
        for widget in self.scrollable_frame.winfo_children():
            widget.destroy()
        self.device_frames.clear()
        
        # 为每个设备创建控制面板
        for i, device in enumerate(self.simulator.devices):
            self.create_device_panel(device, i)
    
    def create_device_panel(self, device, index):
        """为单个设备创建控制面板"""
        # 设备框架
        device_frame = ttk.LabelFrame(self.scrollable_frame, 
                                    text=f"{device['type']} - {device['deviceId'][:8]}...", 
                                    padding=10)
        device_frame.pack(fill=tk.X, pady=5)
        
        # 存储设备控制组件
        self.device_frames[device['deviceId']] = {
            'frame': device_frame, 
            'controls': {},
            'device_index': index
        }
        
        # 为每个参数创建控制组件
        for param_name, param_value in device['params'].items():
            param_frame = ttk.Frame(device_frame)
            param_frame.pack(fill=tk.X, pady=2)
            
            # 参数标签
            label = ttk.Label(param_frame, text=f"{param_name}:", width=15)
            label.pack(side=tk.LEFT, padx=(0, 10))
            
            # 当前值显示标签
            current_value_label = ttk.Label(param_frame, text="当前值:", width=8)
            current_value_label.pack(side=tk.LEFT, padx=(0, 5))
            
            # 根据参数类型创建不同的控制组件
            if isinstance(param_value, bool):
                # 布尔值使用复选框
                var = tk.BooleanVar(value=param_value)
                control = ttk.Checkbutton(param_frame, variable=var, 
                                        command=lambda d=device, p=param_name: self.update_device_param(d, p))
                control.pack(side=tk.LEFT)
                
                # 当前值显示
                current_var = tk.StringVar(value=str(param_value))
                current_label = ttk.Label(param_frame, textvariable=current_var, width=10, foreground="blue")
                current_label.pack(side=tk.LEFT, padx=(10, 0))
                
                self.device_frames[device['deviceId']]['controls'][param_name] = {
                    'var': var,
                    'current_var': current_var,
                    'type': 'bool'
                }
                
            elif isinstance(param_value, (int, float)):
                # 数值使用输入框和滑块
                control_frame = ttk.Frame(param_frame)
                control_frame.pack(side=tk.LEFT, fill=tk.X, expand=True)
                
                var = tk.DoubleVar(value=param_value)
                
                # 输入框
                entry = ttk.Entry(control_frame, textvariable=var, width=10)
                entry.pack(side=tk.LEFT, padx=(0, 10))
                entry.bind('<Return>', lambda e, d=device, p=param_name: self.update_device_param(d, p))
                
                # 滑块（根据参数类型设置范围）
                if param_name in ['temperature', 'currentTemp', 'insideTemp']:
                    scale = ttk.Scale(control_frame, from_=-10, to=50, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                elif param_name == 'targetTemp':
                    scale = ttk.Scale(control_frame, from_=16, to=30, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                elif param_name == 'brightness':
                    scale = ttk.Scale(control_frame, from_=1, to=100, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                elif param_name == 'fanSpeed':
                    scale = ttk.Scale(control_frame, from_=1, to=5, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                elif param_name == 'color':
                    scale = ttk.Scale(control_frame, from_=2700, to=6000, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                elif param_name == 'UV':
                    scale = ttk.Scale(control_frame, from_=0, to=4, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                else:
                    scale = ttk.Scale(control_frame, from_=0, to=100, variable=var, orient=tk.HORIZONTAL,
                                    command=lambda v, d=device, p=param_name: self.update_device_param(d, p))
                
                scale.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(0, 10))
                
                # 当前值显示
                current_var = tk.StringVar(value=str(param_value))
                current_label = ttk.Label(param_frame, textvariable=current_var, width=10, foreground="blue")
                current_label.pack(side=tk.LEFT)
                
                self.device_frames[device['deviceId']]['controls'][param_name] = {
                    'var': var,
                    'current_var': current_var,
                    'type': 'number'
                }
                
            elif isinstance(param_value, str):
                # 字符串使用下拉框或输入框
                if param_name == 'mode' and device['type'] == 'airConditioner':
                    var = tk.StringVar(value=param_value)
                    control = ttk.Combobox(param_frame, textvariable=var, 
                                         values=["制冷", "制热", "送风", "除湿"], state="readonly")
                    control.bind('<<ComboboxSelected>>', lambda e, d=device, p=param_name: self.update_device_param(d, p))
                else:
                    var = tk.StringVar(value=param_value)
                    control = ttk.Entry(param_frame, textvariable=var, width=20)
                    control.bind('<Return>', lambda e, d=device, p=param_name: self.update_device_param(d, p))
                
                control.pack(side=tk.LEFT, padx=(0, 10))
                
                # 当前值显示
                current_var = tk.StringVar(value=str(param_value))
                current_label = ttk.Label(param_frame, textvariable=current_var, width=15, foreground="blue")
                current_label.pack(side=tk.LEFT)
                
                self.device_frames[device['deviceId']]['controls'][param_name] = {
                    'var': var,
                    'current_var': current_var,
                    'type': 'string'
                }
    
    def update_device_param(self, device, param_name):
        """更新设备参数到模拟器"""
        if not self.simulator:
            return
        
        try:
            device_id = device['deviceId']
            control_info = self.device_frames[device_id]['controls'][param_name]
            new_value = control_info['var'].get()
            
            # 根据原始类型转换值
            original_value = device['params'][param_name]
            if isinstance(original_value, bool):
                new_value = bool(new_value)
            elif isinstance(original_value, int):
                new_value = int(new_value)
            elif isinstance(original_value, float):
                new_value = float(new_value)
            else:
                new_value = str(new_value)
            
            # 更新模拟器中的设备参数
            device_index = self.device_frames[device_id]['device_index']
            self.simulator.devices[device_index]['params'][param_name] = new_value
            
            # 更新当前值显示
            control_info['current_var'].set(str(new_value))
            
            print(f"已更新设备 {device_id[:8]}... 的参数 {param_name} 为 {new_value}")
            
        except Exception as e:
            messagebox.showerror("错误", f"更新参数失败: {e}")
    
    def update_display(self):
        """定时更新显示"""
        if self.simulator and self.simulator.devices:
            # 更新当前值显示
            for device in self.simulator.devices:
                device_id = device['deviceId']
                if device_id in self.device_frames:
                    for param_name, param_value in device['params'].items():
                        if param_name in self.device_frames[device_id]['controls']:
                            control_info = self.device_frames[device_id]['controls'][param_name]
                            
                            # 如果是范围配置，显示生成的随机值
                            if isinstance(param_value, dict) and "min" in param_value and "max" in param_value:
                                # 生成随机值用于显示
                                if isinstance(param_value["min"], int) and isinstance(param_value["max"], int):
                                    display_value = f"随机({param_value['min']}-{param_value['max']})"
                                else:
                                    display_value = f"随机({param_value['min']:.1f}-{param_value['max']:.1f})"
                                control_info['current_var'].set(display_value)
                            else:
                                # 更新右侧蓝色显示值
                                display_value = str(param_value)
                                control_info['current_var'].set(display_value)
                                
                                # 同步更新左侧控件的值，确保两边数值一致
                                try:
                                    if control_info['type'] == 'bool':
                                        control_info['var'].set(bool(param_value))
                                    elif control_info['type'] == 'number':
                                        control_info['var'].set(float(param_value))
                                    elif control_info['type'] == 'string':
                                        control_info['var'].set(str(param_value))
                                except (ValueError, TypeError):
                                    # 如果转换失败，保持原值
                                    pass
        
        # 每秒更新一次
        self.root.after(1000, self.update_display)
    
    def save_config(self):
        """保存当前配置到文件"""
        if not self.simulator:
            messagebox.showwarning("警告", "模拟器未启动，无法保存配置")
            return
        
        try:
            with open('config.json', 'w', encoding='utf-8') as f:
                json.dump(self.simulator.devices, f, ensure_ascii=False, indent=2)
            messagebox.showinfo("成功", "配置已保存到 config.json")
        except Exception as e:
            messagebox.showerror("错误", f"保存配置失败: {e}")
    
    def on_closing(self):
        """关闭窗口时的清理工作"""
        self.stop_simulator()
        self.root.destroy()

def main():
    root = tk.Tk()
    app = MQTTRealtimeGUI(root)
    root.protocol("WM_DELETE_WINDOW", app.on_closing)
    root.mainloop()

if __name__ == "__main__":
    main()