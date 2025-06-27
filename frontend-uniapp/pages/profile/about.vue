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

	.copyright {
		padding-top: 30rpx;
		padding-bottom: 60rpx;
		text-align: center;

		text {
			font-size: 24rpx;
			color: #999;
		}
	}
</style>