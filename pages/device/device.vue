<template>
	<view class="device-container">
		<!-- 顶部导航栏 -->
		<view class="header">
			<view class="header-left">
				<button class="room-btn" @click="showDrawer('drawer')">
					<text class="room-text">{{ currentRoomName }}</text>
					<text class="arrow">►</text>
				</button>
			</view>
			<view class="header-right">
				<image class="add-btn" @click="goToAdd" src="/static/add.png"></image>
			</view>
		</view>

		<!-- 设备列表 -->
		<view class="device-list">
			<view class="device-grid">
				<view v-for="device in devices" :key="device.deviceId" class="device-card" @click="goToDetail(device)">
					<view class="device-icon">
						<text class="icon">{{ getDeviceIcon(device.type) }}</text>
					</view>
					<view class="device-info">
						<text class="device-name">{{ device.deviceName }}</text>
						<text class="device-status"
							:class="{ online: checkDeviceStatus(device) === '在线' }">{{ checkDeviceStatus(device) }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 房间选择抽屉 -->
		<uni-drawer ref="drawer" mode="left" :width="280">
			<view class="drawer-content">
				<view class="drawer-header">
					<text class="drawer-title">选择房间</text>
				</view>
				<view class="drawer-body">
					<scroll-view class="room-list" scroll-y="true">
						<view class="room-item" :class="{ active: selectedRoomId === null }"
							@click="selectRoom(null, '所有设备')">
							<text>所有设备</text>
							<view class="divider"></view>
						</view>
						<view v-for="room in rooms" :key="room.roomId" class="room-item"
							:class="{ active: selectedRoomId === room.roomId }"
							@click="selectRoom(room.roomId, room.roomName)">
							<text>{{ room.roomName }}</text>
							<view class="divider"></view>
						</view>
					</scroll-view>
					<view class="room-item create-room" @click="showCreateRoom">
						<text class="create-icon">+</text>
						<text>新建房间</text>
					</view>
				</view>
			</view>
		</uni-drawer>

		<!-- 创建房间对话框 -->
		<uni-popup ref="createPopup" type="dialog" :mask-click="false">
			<view class="dialog">
				<view class="dialog-title">
					<text>新建房间</text>
				</view>
				<view class="dialog-content">
					<input v-model="newRoomName" class="dialog-input" placeholder="请输入房间名称" maxlength="20" />
				</view>
				<view class="dialog-actions">
					<button class="dialog-btn cancel" @click="closeCreateDialog">取消</button>
					<button class="dialog-btn confirm" @click="createRoom" :disabled="!newRoomName.trim()">创建</button>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
	import {
		getUserRooms,
		createRoom
	} from '@/utils/room.js'
	import {
		getUserDevices,
		getRoomDevices
	} from '@/utils/device.js'

	export default {
		data() {
			return {
				rooms: [], // 房间列表
				devices: [], // 设备列表
				selectedRoomId: null, // 当前选中的房间ID
				currentRoomName: '所有设备', // 当前房间名称
				newRoomName: '', // 新建房间名称
				loading: false,
				refreshTimer: null // 定时器
			}
		},
		onLoad() {
			this.loadRooms()
			this.loadDevices()
			this.startAutoRefresh()
		},
		onShow() {
			// 页面显示时重新启动定时器
			this.startAutoRefresh()
		},
		onHide() {
			// 页面隐藏时停止定时器，避免在跳转到登录页等情况下继续运行
			this.stopAutoRefresh()
		},
		onUnload() {
			this.stopAutoRefresh()
		},
		methods: {
			// 加载房间列表
			async loadRooms() {
				try {
					const res = await getUserRooms()
					if (res.code === 0) {
						let rooms = res.data || []
						// 按房间名称排序，如果名称相同则按房间ID排序
						rooms.sort((a, b) => {
							const nameA = String(a.roomName || '').toLowerCase()
							const nameB = String(b.roomName || '').toLowerCase()
							if (nameA === nameB) {
								const idA = String(a.roomId || '')
								const idB = String(b.roomId || '')
								return idA < idB ? -1 : idA > idB ? 1 : 0
							}
							return nameA < nameB ? -1 : nameA > nameB ? 1 : 0
						})
						this.rooms = rooms
					}
				} catch (error) {
					console.error('加载房间列表失败:', error)
				}
			},

			// 加载设备列表
			async loadDevices() {
				try {
					this.loading = true
					let res
					if (this.selectedRoomId) {
						res = await getRoomDevices(this.selectedRoomId)
					} else {
						res = await getUserDevices()
					}
					if (res.code === 0) {
						let devices = res.data || []
						// 按设备名称排序，如果名称相同则按设备ID排序
						devices.sort((a, b) => {
							const nameA = (a.deviceName || '').toLowerCase()
							const nameB = (b.deviceName || '').toLowerCase()
							if (nameA === nameB) {
								return (a.deviceId || '').localeCompare(b.deviceId || '')
							}
							return nameA.localeCompare(nameB)
						})
						this.devices = devices
					}
				} catch (error) {
					console.error('加载设备列表失败:', error)
				} finally {
					this.loading = false
				}
			},

			// 选择房间
			selectRoom(roomId, roomName) {
				this.selectedRoomId = roomId
				this.currentRoomName = roomName
				this.$refs.drawer.close()
				this.loadDevices()
			},

			// 打开抽屉
			showDrawer(e) {
				this.loadRooms()
				this.$refs[e].open()
			},

			// 显示创建房间对话框
			showCreateRoom() {
				this.$refs.drawer.close()
				this.$refs.createPopup.open()
			},

			// 创建房间
			async createRoom() {
				if (!this.newRoomName.trim()) return
				try {
					const res = await createRoom(this.newRoomName.trim())
					if (res.code === 0) {
						uni.showToast({
							title: '创建成功',
							icon: 'success'
						})
						this.newRoomName = ''
						this.closeCreateDialog()
						this.loadRooms()
					} else {
						uni.showToast({
							title: res.info || '创建失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('创建房间失败:', error)
					uni.showToast({
						title: '创建失败',
						icon: 'none'
					})
				}
			},

			// 关闭创建对话框
			closeCreateDialog() {
				this.$refs.createPopup.close()
				this.newRoomName = ''
			},

			// 跳转到添加设备页面
			goToAdd() {
				uni.navigateTo({
					url: '/pages/device/add'
				})
			},

			// 跳转到设备详情页面
			goToDetail(device) {
				uni.navigateTo({
					url: `/pages/device/detail?deviceId=${device.deviceId}&type=${device.type || ''}`
				})
			},

			// 开始自动刷新
			startAutoRefresh() {
				// 先清理已存在的定时器，避免重复启动
				this.stopAutoRefresh()
				this.refreshTimer = setInterval(() => {
					this.loadDevices()
				}, 3000) // 每3秒刷新一次
			},

			// 停止自动刷新
			stopAutoRefresh() {
				if (this.refreshTimer) {
					clearInterval(this.refreshTimer)
					this.refreshTimer = null
				}
			},

			// 检查设备状态
			checkDeviceStatus(device) {
				if (!device.lastHeartbeat) return '离线'
				const now = Date.now()
				const lastTime = new Date(device.lastHeartbeat).getTime()
				const diffMinutes = (now - lastTime) / (1000 * 60)
				return diffMinutes > 5 ? '离线' : '在线'
			},

			// 获取设备图标
			getDeviceIcon(type) {
				const iconMap = {
					refrigerator: '🧊',
					airConditioner: '❄️',
					lamp: '💡',
					envSensor: '🌡️',
					gasDetector: '🚨',
					airPurifier: '🌪️'
				}
				return iconMap[type] || '📱'
			}
		}
	}
</script>

<style lang="scss">
	.device-container {
		position: relative;
		min-height: calc(100vh - var(--status-bar-height) - 20rpx);
		padding-top: calc(var(--status-bar-height) + 20rpx);
		background: linear-gradient(135deg, #E8F8F3 0%, #C7F0E1 50%, #A8E6D1 100%);

		&::before {
			content: '';
			position: fixed;
			top: 0;
			left: 0;
			width: 100vw;
			height: 100vh;
			z-index: -1;
			pointer-events: none;
		}
	}

	// 顶部导航栏
	.header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20rpx 30rpx;

		.header-left {
			.room-btn {
				display: flex;
				align-items: center;
				font-size: 28rpx;
				background: #F8FDFC;
				border-color: #19CD90;
				transition: all 0.3s ease;

				.room-text {
					color: #333;
					margin-right: 8rpx;
				}

				.arrow {
					color: #19CD90;
					font-size: 20rpx;
				}

				&:active {
					transform: scale(0.95);
				}
			}
		}

		.header-right {
			.add-btn {
				width: 50rpx;
				height: 50rpx;
				transition: all 0.3s ease;

				&:active {
					transform: scale(0.9);
				}
			}
		}
	}

	// 设备列表
	.device-list {
		padding: 30rpx;

		.device-grid {
			display: grid;
			grid-template-columns: 1fr 1fr;
			gap: 20rpx;

			.device-card {
				background: rgba(255, 255, 255, 0.6);
				border-radius: 25rpx;
				padding: 30rpx;
				box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
				transition: all 0.3s ease;
				border: 2rpx solid transparent;

				&:active {
					transform: scale(0.98);
					border-color: #19CD90;
					background: #F8FDFC;
				}

				.device-icon {
					text-align: center;
					margin-bottom: 20rpx;

					.icon {
						font-size: 60rpx;
					}
				}

				.device-info {
					text-align: center;

					.device-name {
						display: block;
						font-size: 28rpx;
						color: #333;
						font-weight: 500;
						margin-bottom: 8rpx;
						white-space: nowrap;
						overflow: hidden;
						text-overflow: ellipsis;
						max-width: 100%;
					}

					.device-status {
						display: block;
						font-size: 24rpx;
						color: #999;

						&.online {
							color: #19CD90;
						}
					}
				}
			}
		}
	}

	// 抽屉样式
	.drawer-content {
		height: 100vh;
		background: #F8FDFC;
		box-shadow: 4rpx 0 20rpx rgba(0, 0, 0, 0.1);
		padding-top: calc(var(--status-bar-height) + 40rpx);
		display: flex;
		flex-direction: column;

		.drawer-header {
			padding: 0 40rpx 30rpx;
			border-bottom: 2rpx solid #E8F5F0;
			flex-shrink: 0;

			.drawer-title {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}
		}

		.drawer-body {
			flex: 1;
			display: flex;
			flex-direction: column;
			padding-top: 20rpx;
			padding-bottom: 40rpx;

			.room-list {
				flex: 1;
				height: 0;

				.room-item {
					width: 100%;
					padding: 30rpx 40rpx 0;
					background-color: transparent;
					text-align: left;
					font-size: 28rpx;
					color: #333;
					position: relative;
					transition: background-color 0.3s;
					cursor: pointer;

					&:active {
						background-color: #F0F9F6;
					}

					&.active {
						background-color: #F0F9F6;
						color: #19CD90;

						&::after {
							content: '';
							position: absolute;
							right: 0;
							top: 0;
							bottom: 0;
							width: 6rpx;
							background-color: #19CD90;
						}
					}

					.divider {
						height: 2rpx;
						background-color: #E8F5F0;
						margin-top: 30rpx;
						margin-left: 0;
						margin-right: 0;
					}
				}
			}

			.create-room {
				flex-shrink: 0;
				display: flex;
				align-items: center;
				padding: 30rpx 40rpx;
				color: #19CD90;
				background-color: transparent;
				margin-bottom: 250rpx;
				font-size: 28rpx;
				border-top: 2rpx solid #E8F5F0;
				transition: background-color 0.3s;
				cursor: pointer;

				&:active {
					background-color: #F0F9F6;
				}

				.create-icon {
					margin-right: 16rpx;
					font-size: 32rpx;
					font-weight: bold;
				}
			}
		}
	}

	// 对话框样式
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
</style>