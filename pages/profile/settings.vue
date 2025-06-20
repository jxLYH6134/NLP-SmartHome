<template>
	<view class="settings-container">
		<view class="header">
			<text class="title">设置</text>
		</view>

		<!-- 通用设置 -->
		<view class="section">
			<view class="section-title">
				<text>通用设置</text>
			</view>
			<view class="settings-list">
				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">🌙</text>
						<text class="setting-title">深色模式</text>
					</view>
					<switch class="setting-switch" disabled />
				</view>

				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">🔔</text>
						<text class="setting-title">推送通知</text>
					</view>
					<switch class="setting-switch" checked disabled />
				</view>

				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">🌐</text>
						<text class="setting-title">语言</text>
					</view>
					<view class="setting-right">
						<text class="setting-value">简体中文</text>
						<text class="setting-arrow">></text>
					</view>
				</view>
			</view>
		</view>

		<!-- 设备设置 -->
		<view class="section">
			<view class="section-title">
				<text>设备设置</text>
			</view>
			<view class="settings-list">
				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">🔄</text>
						<text class="setting-title">设备状态同步</text>
					</view>
					<view class="setting-right">
						<text class="setting-value">实时</text>
						<text class="setting-arrow">></text>
					</view>
				</view>

				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">⚡</text>
						<text class="setting-title">节能模式</text>
					</view>
					<switch class="setting-switch" disabled />
				</view>
			</view>
		</view>

		<!-- 安全设置 -->
		<view class="section">
			<view class="section-title">
				<text>安全设置</text>
			</view>
			<view class="settings-list">
				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">🔒</text>
						<text class="setting-title">应用锁</text>
					</view>
					<switch class="setting-switch" disabled />
				</view>

				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">👆</text>
						<text class="setting-title">指纹解锁</text>
					</view>
					<switch class="setting-switch" disabled />
				</view>

				<view class="setting-item disabled">
					<view class="setting-left">
						<text class="setting-icon">🕐</text>
						<text class="setting-title">自动锁定时间</text>
					</view>
					<view class="setting-right">
						<text class="setting-value">5分钟</text>
						<text class="setting-arrow">></text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
</script>

<style lang="scss">
	.settings-container {
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

	.section {
		margin-bottom: 40rpx;

		.section-title {
			padding: 0 20rpx 20rpx;

			text {
				font-size: 28rpx;
				color: #666;
				font-weight: 500;
			}
		}
	}

	.settings-list {
		background: #ffffff;
		border-radius: 20rpx;
		overflow: hidden;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
	}

	.setting-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx 40rpx;
		border-bottom: 2rpx solid #E8F5F0;

		&:last-child {
			border-bottom: none;
		}

		&.disabled {
			opacity: 0.6;
			pointer-events: none;
		}

		.setting-left {
			display: flex;
			align-items: center;
			flex: 1;

			.setting-icon {
				font-size: 36rpx;
				margin-right: 24rpx;
			}

			.setting-title {
				font-size: 30rpx;
				color: #333;
			}
		}

		.setting-right {
			display: flex;
			align-items: center;

			.setting-value {
				font-size: 28rpx;
				color: #666;
				margin-right: 16rpx;
			}

			.setting-arrow {
				color: #19CD90;
				font-size: 28rpx;
			}
		}

		.setting-switch {
			transform: scale(0.8);
		}
	}
</style>