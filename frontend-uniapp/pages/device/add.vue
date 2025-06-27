<template>
	<view class="add-device-container">
		<view class="header">
			<text class="title">添加设备</text>
			<text class="subtitle">扫描设备二维码即可添加</text>
		</view>

		<view class="scan-area">
			<view class="scan-frame">
				<view class="corner top-left"></view>
				<view class="corner top-right"></view>
				<view class="corner bottom-left"></view>
				<view class="corner bottom-right"></view>
				<view class="scan-icon">
					<text class="iconfont">📷</text>
				</view>
			</view>
			<text class="scan-tip">将设备二维码放入框内</text>
		</view>

		<view class="action-buttons">
			<button class="scan-btn" @click="startScan" :disabled="isScanning">
				{{ isScanning ? '扫描中...' : '开始扫描' }}
			</button>
		</view>
	</view>
</template>

<script>
	import {
		updateDevice
	} from '@/utils/device.js'

	export default {
		data() {
			return {
				isScanning: false
			}
		},
		methods: {
			// 开始扫码
			startScan() {
				this.isScanning = true

				uni.scanCode({
					success: (res) => {
						this.addDevice(res.result)
					},
					fail: (err) => {
						uni.showToast({
							title: '扫码失败',
							icon: 'none'
						})
						this.isScanning = false
					},
					complete: () => {
						this.isScanning = false
					}
				})
			},

			// 添加设备
			async addDevice(deviceId) {
				try {
					uni.showLoading({
						title: '添加中...'
					})

					const result = await updateDevice(deviceId)

					uni.hideLoading()

					if (result.code === 0) {
						uni.showToast({
							title: '添加成功',
							icon: 'success'
						})

						// 延迟返回上一页
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					} else {
						uni.showToast({
							title: result.info || '添加失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.hideLoading()
					uni.showToast({
						title: '添加失败',
						icon: 'none'
					})
				}
			}
		}
	}
</script>

<style lang="scss">
	.add-device-container {
		padding-top: calc(var(--status-bar-height) + 100rpx);
		background: linear-gradient(135deg, #E8F8F3 0%, #C7F0E1 50%, #A8E6D1 100%);
		min-height: calc(100vh - var(--status-bar-height) - 100rpx);

		.header {
			text-align: center;
			margin-bottom: 80rpx;

			.title {
				display: block;
				font-size: 48rpx;
				font-weight: bold;
				color: #19CD90;
				margin-bottom: 20rpx;
			}

			.subtitle {
				display: block;
				font-size: 28rpx;
				color: #666666;
			}
		}

		.scan-area {
			display: flex;
			flex-direction: column;
			align-items: center;
			margin-bottom: 100rpx;

			.scan-frame {
				position: relative;
				width: 400rpx;
				height: 400rpx;
				background: rgba(255, 255, 255, 0.9);
				border-radius: 20rpx;
				box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
				display: flex;
				align-items: center;
				justify-content: center;
				margin-bottom: 40rpx;

				.corner {
					position: absolute;
					width: 60rpx;
					height: 60rpx;
					border: 6rpx solid #19CD90;

					&.top-left {
						top: 20rpx;
						left: 20rpx;
						border-right: none;
						border-bottom: none;
						border-radius: 20rpx 0 0 0;
					}

					&.top-right {
						top: 20rpx;
						right: 20rpx;
						border-left: none;
						border-bottom: none;
						border-radius: 0 20rpx 0 0;
					}

					&.bottom-left {
						bottom: 20rpx;
						left: 20rpx;
						border-right: none;
						border-top: none;
						border-radius: 0 0 0 20rpx;
					}

					&.bottom-right {
						bottom: 20rpx;
						right: 20rpx;
						border-left: none;
						border-top: none;
						border-radius: 0 0 20rpx 0;
					}
				}

				.scan-icon {
					font-size: 80rpx;
					color: #19CD90;
					opacity: 0.6;
				}
			}

			.scan-tip {
				font-size: 28rpx;
				color: #666666;
				text-align: center;
			}
		}

		.action-buttons {
			padding: 0 40rpx;

			.scan-btn {
				width: 100%;
				height: 88rpx;
				background: linear-gradient(135deg, #19CD90 0%, #15B882 100%);
				color: #ffffff;
				border: none;
				border-radius: 44rpx;
				font-size: 32rpx;
				font-weight: bold;
				box-shadow: 0 8rpx 24rpx rgba(25, 205, 144, 0.3);

				&:active {
					box-shadow: 0 4rpx 12rpx rgba(25, 205, 144, 0.3);
					transform: translateY(2rpx);
				}

				&:disabled {
					background: #F5F5F5;
					color: #CCCCCC;
					box-shadow: none;
					transform: none;
				}
			}
		}
	}
</style>