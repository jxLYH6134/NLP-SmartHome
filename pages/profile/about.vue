<template>
	<view class="about-container">
		<view class="header">
			<text class="title">关于</text>
		</view>

		<!-- 应用信息 -->
		<view class="app-info">
			<view class="app-logo">
				<image src="/static/logo.png" class="logo-image" mode="aspectFit"></image>
			</view>
			<view class="app-name">
				<text>智能家居</text>
			</view>
			<view class="app-version">
				<text>版本 1.0.0</text>
			</view>
			<view class="app-desc">
				<text>让智能生活触手可及</text>
			</view>
		</view>

		<!-- 详细信息 -->
		<view class="info-list">
			<view class="info-item">
				<text class="info-label">开发者</text>
				<text class="info-value">LYH</text>
			</view>

			<view class="info-item">
				<text class="info-label">发布日期</text>
				<text class="info-value">2025年6月</text>
			</view>

			<view class="info-item">
				<text class="info-label">支持系统</text>
				<text class="info-value">iOS 12.0+ / Android 8.0+</text>
			</view>

			<view class="info-item">
				<text class="info-label">官方网站</text>
				<text class="info-value link">smarthome.crychic.link</text>
			</view>

			<view class="info-item">
				<text class="info-label">客服邮箱</text>
				<text class="info-value link">support@crychic.link</text>
			</view>
		</view>

		<!-- 功能介绍 -->
		<view class="features">
			<view class="features-title">
				<text>主要功能</text>
			</view>
			<view class="features-list">
				<view class="feature-item">
					<text class="feature-icon">🏡</text>
					<view class="feature-content">
						<text class="feature-title">智能设备管理</text>
						<text class="feature-desc">统一管理家中所有智能设备，实现远程控制</text>
					</view>
				</view>

				<view class="feature-item">
					<text class="feature-icon">👨‍👩‍👧‍👦</text>
					<view class="feature-content">
						<text class="feature-title">家庭组管理</text>
						<text class="feature-desc">创建家庭组，与家人共享智能设备控制权限</text>
					</view>
				</view>

				<view class="feature-item">
					<text class="feature-icon">🎭</text>
					<view class="feature-content">
						<text class="feature-title">智能场景</text>
						<text class="feature-desc">控制设备，一句话的事</text>
					</view>
				</view>

				<view class="feature-item">
					<text class="feature-icon">🔐</text>
					<view class="feature-content">
						<text class="feature-title">安全保障</text>
						<text class="feature-desc">多重安全验证，保护您的隐私和数据安全</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 版权信息 -->
		<view class="copyright">
			<text>© 2025 SmartHome Team. All rights reserved.</text>
		</view>
	</view>
</template>

<script>
</script>

<style lang="scss">
	.about-container {
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

	.app-info {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 60rpx 40rpx;
		margin-bottom: 30rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);

		.app-logo {
			width: 120rpx;
			height: 120rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 30rpx;

			.logo-image {
				width: 120rpx;
				height: 120rpx;
				border-radius: 25rpx;
			}
		}

		.app-name {
			margin-bottom: 16rpx;

			text {
				font-size: 40rpx;
				font-weight: bold;
				color: #333;
			}
		}

		.app-version {
			margin-bottom: 20rpx;

			text {
				font-size: 28rpx;
				color: #666666;
			}
		}

		.app-desc {
			text {
				font-size: 26rpx;
				color: #666666;
				line-height: 1.5;
			}
		}
	}

	.info-list {
		background: #ffffff;
		border-radius: 20rpx;
		margin-bottom: 30rpx;
		overflow: hidden;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
	}

	.info-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx 40rpx;
		border-bottom: 2rpx solid #E8F5F0;

		&:last-child {
			border-bottom: none;
		}

		.info-label {
			font-size: 28rpx;
			color: #333;
			font-weight: 500;
		}

		.info-value {
			font-size: 28rpx;
			color: #666666;
			text-align: right;
			flex: 1;
			margin-left: 40rpx;

			&.link {
				color: #19CD90;
			}
		}
	}

	.features {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);

		.features-title {
			margin-bottom: 40rpx;

			text {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}
		}
	}

	.feature-item {
		display: flex;
		align-items: flex-start;
		margin-bottom: 40rpx;

		&:last-child {
			margin-bottom: 0;
		}

		.feature-icon {
			font-size: 40rpx;
			margin-right: 24rpx;
			margin-top: 8rpx;
		}

		.feature-content {
			flex: 1;

			.feature-title {
				display: block;
				font-size: 30rpx;
				color: #333;
				font-weight: 500;
				margin-bottom: 12rpx;
			}

			.feature-desc {
				display: block;
				font-size: 26rpx;
				color: #666666;
				line-height: 1.5;
			}
		}
	}

	.copyright {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx 20rpx;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
		text-align: center;

		text {
			font-size: 24rpx;
			color: #999;
		}
	}
</style>