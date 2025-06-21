<template>
	<view class="detail-container">
		<view class="header">
			<text class="title">设备详情</text>
		</view>

		<view v-if="initialLoading" class="loading">
			<text>加载中...</text>
		</view>

		<view v-else-if="device" class="device-info">
			<!-- 设备基本信息 -->
			<view class="device-header">
				<view class="device-name">
					<text>{{ device.deviceName || getDeviceName(device.type) }}</text>
					<uni-icons class="edit" type="compose" size="20" color="#19CD90"
						@click="openEditDialog"></uni-icons>
				</view>
				<view class="device-id">
					<text>设备ID: {{ device.deviceId }}</text>
				</view>
			</view>

			<!-- 房间选择器 -->
			<view class="room-selector">
				<text class="selector-label">所属房间</text>
				<uni-data-select v-model="selectedRoomId" :localdata="roomOptions" @change="onRoomChange"
					placeholder="请选择房间" :disabled="!isDeviceOwner">
				</uni-data-select>
				<text v-if="!isDeviceOwner" class="permission-hint">此设备来自家庭组共享，无法修改房间</text>
			</view>


			<!-- 冰箱控制 -->
			<view v-if="device.type === 'refrigerator'" class="control-panel">
				<view class="control-item">
					<text class="control-label">冷冻模式</text>
					<switch :checked="device.params.freeze" @change="onSwitchChange('freeze', $event)" color="#19CD90">
					</switch>
				</view>
				<view class="control-item">
					<text class="control-label">目标温度: {{ device.params.targetTemp }}°C</text>
					<slider :min="5" :max="20" :value="device.params.targetTemp"
						@change="onSliderChange('targetTemp', $event)" class="control-slider" activeColor="#19CD90">
					</slider>
				</view>
				<view class="status-item">
					<text class="status-label">内部温度</text>
					<text class="status-value">{{ device.params.insideTemp }}°C</text>
				</view>
				<view class="status-item">
					<text class="status-label">内部湿度</text>
					<text class="status-value">{{ device.params.insideHumi }}%</text>
				</view>
				<view class="status-item">
					<text class="status-label">门状态</text>
					<text class="status-value">{{ device.params.opened ? '已打开' : '已关闭' }}</text>
				</view>
			</view>

			<!-- 空调控制 -->
			<view v-else-if="device.type === 'airConditioner'" class="control-panel">
				<view class="control-item">
					<text class="control-label">电源</text>
					<switch :checked="device.params.power" @change="onSwitchChange('power', $event)" color="#19CD90">
					</switch>
				</view>
				<view class="control-item">
					<text class="control-label">模式</text>
					<picker :value="modeIndex" :range="modes" @change="onModeChange">
						<view class="picker-text">{{ device.params.mode }}</view>
					</picker>
				</view>
				<view class="control-item">
					<text class="control-label">目标温度: {{ device.params.targetTemp }}°C</text>
					<slider :min="16" :max="30" :value="device.params.targetTemp"
						@change="onSliderChange('targetTemp', $event)" class="control-slider" activeColor="#19CD90">
					</slider>
				</view>
				<view class="control-item">
					<text class="control-label">风速: {{ device.params.fanSpeed }}</text>
					<slider :min="1" :max="5" :value="device.params.fanSpeed"
						@change="onSliderChange('fanSpeed', $event)" class="control-slider" activeColor="#19CD90">
					</slider>
				</view>
			</view>

			<!-- 灯光控制 -->
			<view v-else-if="device.type === 'lamp'" class="control-panel">
				<view class="control-item">
					<text class="control-label">电源</text>
					<switch :checked="device.params.power" @change="onSwitchChange('power', $event)" color="#19CD90">
					</switch>
				</view>
				<view class="control-item">
					<text class="control-label">亮度: {{ device.params.brightness }}%</text>
					<slider :min="1" :max="100" :value="device.params.brightness"
						@change="onSliderChange('brightness', $event)" class="control-slider" activeColor="#19CD90">
					</slider>
				</view>
				<view class="control-item">
					<text class="control-label">色温: {{ device.params.colorTemperature }}K</text>
					<slider :min="4500" :max="8000" :value="device.params.colorTemperature"
						@change="onSliderChange('colorTemperature', $event)" class="control-slider"
						activeColor="#19CD90"></slider>
				</view>
			</view>

			<!-- 环境传感器显示 -->
			<view v-else-if="device.type === 'envSensor'" class="sensor-panel">
				<view class="sensor-item">
					<text class="sensor-label">温度</text>
					<text class="sensor-value">{{ device.params.temperature }}°C</text>
				</view>
				<view class="sensor-item">
					<text class="sensor-label">湿度</text>
					<text class="sensor-value">{{ device.params.humidity }}%</text>
				</view>
				<view class="sensor-item">
					<text class="sensor-label">紫外线指数</text>
					<view class="uv-indicator" :class="getUVClass(device.params.UV)"></view>
					<text class="sensor-value">{{ device.params.UV }}</text>
				</view>
				<view class="sensor-item">
					<text class="sensor-label">空气质量</text>
					<text class="sensor-value">{{ device.params.airQuality }}</text>
				</view>
				<view class="sensor-item">
					<text class="sensor-label">光照强度</text>
					<text class="sensor-value">{{ device.params.illumination }}</text>
				</view>
			</view>

			<!-- 燃气检测器 -->
			<view v-else-if="device.type === 'gasDetector'" class="sensor-panel">
				<view class="gas-status" :class="device.params.concentration > 0 ? 'danger' : 'safe'">
					<text class="gas-label">燃气状态</text>
					<text class="gas-value">{{ device.params.concentration > 0 ? '检测到泄露' : '安全' }}</text>
				</view>
				<view class="sensor-item">
					<text class="sensor-label">浓度</text>
					<text class="sensor-value">{{ device.params.concentration }}</text>
				</view>
			</view>

			<!-- 空气净化器控制 -->
			<view v-else-if="device.type === 'airPurifier'" class="control-panel">
				<view class="control-item">
					<text class="control-label">电源</text>
					<switch :checked="device.params.power" @change="onSwitchChange('power', $event)" color="#19CD90">
					</switch>
				</view>
			</view>

			<!-- 删除设备按钮 -->
			<view class="delete-section">
				<button class="delete-btn" @click="confirmDelete">删除设备</button>
			</view>
		</view>

		<view v-else class="error">
			<text>设备信息加载失败</text>
		</view>

		<!-- 编辑设备名称对话框 -->
		<uni-popup ref="editPopup" type="dialog" :mask-click="false">
			<view class="dialog">
				<view class="dialog-title">
					<text>编辑设备名称</text>
				</view>
				<view class="dialog-content">
					<input v-model="editDeviceName" class="dialog-input" placeholder="请输入设备名称" maxlength="20" />
				</view>
				<view class="dialog-actions">
					<button class="dialog-btn cancel" @click="closeEditDialog">取消</button>
					<button class="dialog-btn confirm" @click="updateDeviceName"
						:disabled="!editDeviceName.trim()">保存</button>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
	import {
		getDevice,
		controlDevice,
		updateDevice,
		deleteDevice
	} from '@/utils/device.js'
	import {
		getUserRooms
	} from '@/utils/room.js'

	export default {
		data() {
			return {
				deviceId: '',
				device: null,
				initialLoading: true,
				refreshTimer: null,
				modes: ['制冷', '制热', '送风', '除湿'],
				modeIndex: 0,
				roomOptions: [],
				selectedRoomId: '',
				editDeviceName: '',
				// 存储用户控制的值，防止被自动刷新覆盖
				userControlledValues: {},
				currentUserId: ''
			}
		},
		onLoad(options) {
			this.deviceId = options.deviceId
			this.currentUserId = uni.getStorageSync('userId')
			this.loadDeviceInfo()
			this.loadRooms()
			this.startAutoRefresh()
		},
		onUnload() {
			this.stopAutoRefresh()
		},
		computed: {
			isDeviceOwner() {
				return this.device && this.device.ownerId === this.currentUserId
			}
		},
		methods: {
			async loadDeviceInfo(isRefresh = false) {
				try {
					if (!isRefresh) {
						this.initialLoading = true
					}
					const response = await getDevice(this.deviceId)
					const newDevice = response.data

					if (this.device && isRefresh) {
						// 自动刷新时，只更新只读值，保留用户控制的值
						this.updateReadOnlyValues(newDevice)
					} else {
						// 初次加载或手动刷新，完全更新
						this.device = newDevice
						this.selectedRoomId = newDevice.roomId || ''
					}

					// 设置空调模式索引
					if (this.device.type === 'airConditioner') {
						this.modeIndex = this.modes.indexOf(this.device.params.mode) || 0
					}
				} catch (error) {
					console.error('加载设备信息失败:', error)
					if (!isRefresh) {
						uni.showToast({
							title: '加载失败',
							icon: 'none'
						})
					}
				} finally {
					if (!isRefresh) {
						this.initialLoading = false
					}
				}
			},

			updateReadOnlyValues(newDevice) {
				// 定义只读字段
				const readOnlyFields = {
					refrigerator: ['insideTemp', 'insideHumi', 'opened'],
					envSensor: ['temperature', 'humidity', 'UV', 'airQuality', 'illumination'],
					gasDetector: ['concentration']
				}

				const fieldsToUpdate = readOnlyFields[this.device.type] || []
				fieldsToUpdate.forEach(field => {
					if (newDevice.params[field] !== undefined) {
						this.device.params[field] = newDevice.params[field]
					}
				})

				// 更新设备基本信息
				this.device.deviceName = newDevice.deviceName
				this.device.roomId = newDevice.roomId
			},

			async loadRooms() {
				try {
					const response = await getUserRooms()
					this.roomOptions = response.data.map(room => ({
						value: room.roomId,
						text: room.roomName
					}))
				} catch (error) {
					console.error('加载房间列表失败:', error)
				}
			},

			startAutoRefresh() {
				this.refreshTimer = setInterval(() => {
					this.loadDeviceInfo(true)
				}, 3000)
			},

			stopAutoRefresh() {
				if (this.refreshTimer) {
					clearInterval(this.refreshTimer)
					this.refreshTimer = null
				}
			},

			async controlDeviceParam(param, value) {
				try {
					const params = {}
					params[param] = value

					await controlDevice(this.deviceId, params)

					// 立即更新本地状态并记录用户控制的值
					this.device.params[param] = value
					this.userControlledValues[param] = value
				} catch (error) {
					console.error('控制设备失败:', error)
					uni.showToast({
						title: '控制失败',
						icon: 'none'
					})
				}
			},

			onSwitchChange(param, event) {
				const value = event.detail.value
				this.controlDeviceParam(param, value)
			},

			onSliderChange(param, event) {
				const value = event.detail.value
				this.controlDeviceParam(param, value)
			},

			onModeChange(event) {
				const index = event.detail.value
				this.modeIndex = index
				const mode = this.modes[index]
				this.controlDeviceParam('mode', mode)
			},

			getDeviceName(type) {
				const names = {
					refrigerator: '冰箱',
					airConditioner: '空调',
					lamp: '智能灯',
					envSensor: '环境传感器',
					gasDetector: '燃气检测器',
					airPurifier: '空气净化器'
				}
				return names[type] || '未知设备'
			},

			getUVClass(uvIndex) {
				if (uvIndex === 0) return 'uv-green'
				if (uvIndex === 1) return 'uv-yellow'
				if (uvIndex === 2) return 'uv-orange'
				if (uvIndex === 3) return 'uv-red'
				if (uvIndex === 4) return 'uv-purple'
				return 'uv-green'
			},

			async onRoomChange(value) {
				if (!this.isDeviceOwner) {
					uni.showToast({
						title: '无权限修改',
						icon: 'none'
					})
					return
				}
				try {
					await updateDevice(this.deviceId, undefined, value)
					this.device.roomId = value
					uni.showToast({
						title: '房间更新成功',
						icon: 'success'
					})
				} catch (error) {
					console.error('更新房间失败:', error)
					uni.showToast({
						title: '更新失败',
						icon: 'none'
					})
				}
			},

			openEditDialog() {
				this.editDeviceName = this.device.deviceName || this.getDeviceName(this.device.type)
				this.$refs.editPopup.open()
			},

			closeEditDialog() {
				this.$refs.editPopup.close()
				this.editDeviceName = ''
			},

			async updateDeviceName() {
				try {
					await updateDevice(this.deviceId, this.editDeviceName, undefined)
					this.device.deviceName = this.editDeviceName
					this.closeEditDialog()
					uni.showToast({
						title: '设备名称更新成功',
						icon: 'success'
					})
				} catch (error) {
					console.error('更新设备名称失败:', error)
					uni.showToast({
						title: '更新失败',
						icon: 'none'
					})
				}
			},

			confirmDelete() {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除这个设备吗？删除后需要重新配对',
					confirmColor: '#FF4757',
					success: (res) => {
						if (res.confirm) {
							this.deleteDeviceAction()
						}
					}
				})
			},

			async deleteDeviceAction() {
				try {
					// 停止自动刷新
					this.stopAutoRefresh()
					await deleteDevice(this.deviceId)
					uni.showToast({
						title: '删除成功',
						icon: 'success'
					})
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				} catch (error) {
					console.error('删除设备失败:', error)
					uni.showToast({
						title: '删除失败',
						icon: 'none'
					})
				}
			}
		}
	}
</script>

<style lang="scss">
	.detail-container {
		padding: 20rpx;
		padding-top: calc(var(--status-bar-height) + 20rpx);
		background: linear-gradient(135deg, #E8F8F3 0%, #C7F0E1 50%, #A8E6D1 100%);
		min-height: calc(100vh - var(--status-bar-height) - 40rpx);
	}

	.header {
		padding: 40rpx 0;
		text-align: center;

		.title {
			font-size: 36rpx;
			font-weight: bold;
			color: #19CD90;
		}
	}

	.loading,
	.error {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 60rpx 40rpx;
		text-align: center;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);

		text {
			font-size: 28rpx;
			color: #666666;
		}
	}

	.device-info {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
	}

	.room-selector {
		margin-bottom: 40rpx;
		padding-bottom: 30rpx;
		border-bottom: 2rpx solid #E8F5F0;

		.selector-label {
			font-size: 28rpx;
			color: #333;
			font-weight: 500;
			margin-bottom: 20rpx;
			display: block;
		}

		.permission-hint {
			font-size: 24rpx;
			color: #999;
			margin-top: 10rpx;
			display: block;
		}
	}

	.device-header {
		margin-bottom: 40rpx;
		padding-bottom: 30rpx;
		border-bottom: 2rpx solid #E8F5F0;

		.device-name {
			margin-bottom: 16rpx;
			display: flex;
			align-items: center;
			justify-content: space-between;

			text {
				font-size: 40rpx;
				font-weight: bold;
				color: #333;
				flex: 1;
			}

			.edit {
				flex: 0;
			}
		}

		.device-id {
			text {
				font-size: 26rpx;
				color: #666666;
			}
		}
	}

	.control-panel,
	.sensor-panel {

		.control-item,
		.sensor-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 30rpx 0;
			border-bottom: 2rpx solid #E8F5F0;

			&:last-child {
				border-bottom: none;
			}

			.control-label,
			.sensor-label {
				font-size: 28rpx;
				color: #333;
				font-weight: 500;
				flex: 1;
			}

			.sensor-value {
				font-size: 28rpx;
				color: #19CD90;
				font-weight: bold;
			}

			switch {
				transform: scale(0.8);
			}
		}

		.control-slider {
			flex: 1;
			margin-left: 40rpx;
			margin-top: 20rpx;
		}

		.picker-text {
			font-size: 28rpx;
			color: #19CD90;
			padding: 10rpx 20rpx;
			border: 2rpx solid #19CD90;
			border-radius: 10rpx;
			background: #F8FDFC;
			text-align: center;
			min-width: 120rpx;
		}
	}

	.status-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20rpx 0;
		border-bottom: 2rpx solid #E8F5F0;

		&:last-child {
			border-bottom: none;
		}

		.status-label {
			font-size: 26rpx;
			color: #666666;
		}

		.status-value {
			font-size: 26rpx;
			color: #333;
			font-weight: 500;
		}
	}

	.gas-status {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx;
		border-radius: 15rpx;
		margin-bottom: 30rpx;

		&.safe {
			background: linear-gradient(135deg, #E8F8F3 0%, #C7F0E1 100%);
			border: 2rpx solid #19CD90;

			.gas-label,
			.gas-value {
				color: #19CD90;
			}
		}

		&.danger {
			background: linear-gradient(135deg, #FFE8E8 0%, #FFCCCC 100%);
			border: 2rpx solid #FF4444;

			.gas-label,
			.gas-value {
				color: #FF4444;
			}
		}

		.gas-label,
		.gas-value {
			font-size: 32rpx;
			font-weight: bold;
		}
	}

	.uv-indicator {
		width: 30rpx;
		height: 30rpx;
		border-radius: 50%;
		margin: 0 20rpx;

		&.uv-green {
			background: #4CAF50;
		}

		&.uv-yellow {
			background: #FFEB3B;
		}

		&.uv-orange {
			background: #FF9800;
		}

		&.uv-red {
			background: #F44336;
		}

		&.uv-purple {
			background: #9C27B0;
		}
	}

	.sensor-item {
		position: relative;

		&:has(.uv-indicator) {
			display: flex;
			align-items: center;
			justify-content: flex-start;

			.sensor-value {
				margin-left: auto;
			}
		}
	}

	.dialog {
		background-color: #fff;
		border-radius: 20rpx;
		padding: 40rpx;
		width: 600rpx;

		.dialog-title {
			text-align: center;
			margin-bottom: 40rpx;

			text {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}
		}

		.dialog-content {
			margin-bottom: 40rpx;

			.dialog-input {
				width: 100%;
				height: 80rpx;
				border: 2rpx solid #e0e0e0;
				border-radius: 12rpx;
				padding: 0 20rpx;
				font-size: 28rpx;
				box-sizing: border-box;
			}
		}

		.dialog-actions {
			display: flex;
			gap: 20rpx;

			.dialog-btn {
				flex: 1;
				height: 75rpx;
				border-radius: 12rpx;
				font-size: 28rpx;
				border: none;

				&.cancel {
					background-color: #f0f0f0;
					color: #666;
				}

				&.confirm {
					background: linear-gradient(135deg, #19CD90 0%, #15B882 100%);
					color: #fff;
					box-shadow: 0 8rpx 24rpx rgba(25, 205, 144, 0.3);
					transition: all 0.3s ease;

					&[disabled] {
						opacity: 0.4;
						cursor: not-allowed;
					}
				}
			}
		}
	}

	.delete-section {
		margin-top: 40rpx;
		padding-top: 30rpx;
		border-top: 2rpx solid #E8F5F0;

		.delete-btn {
			width: 100%;
			height: 80rpx;
			border-radius: 12rpx;
			font-size: 28rpx;
			border: 1rpx solid rgba(255, 71, 87, 0.3);
			color: #FF4757;
			background: rgba(255, 71, 87, 0.1);

			&:active {
				background: rgba(255, 71, 87, 0.2);
			}
		}
	}
</style>
