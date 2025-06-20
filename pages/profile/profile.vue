<template>
	<view class="profile-container">
		<!-- 用户信息区域 -->
		<view class="user-info">
			<view class="avatar">
				<text class="avatar-text">😊</text>
			</view>
			<view class="user-id">
				<text>用户ID: {{ userId }}</text>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="menu-list">
			<view class="menu-item" @click="navigateTo('/pages/profile/account')">
				<view class="menu-icon">
					<!-- 预留图标位置 -->
					<text class="icon-placeholder">🔐</text>
				</view>
				<view class="menu-content">
					<text class="menu-title">账户管理</text>
					<text class="menu-desc">修改密码</text>
				</view>
				<text class="menu-arrow">></text>
			</view>

			<view class="menu-item" @click="navigateTo('/pages/profile/family')">
				<view class="menu-icon">
					<text class="icon-placeholder">👨‍👩‍👧‍👦</text>
				</view>
				<view class="menu-content">
					<text class="menu-title">家庭组管理</text>
					<text class="menu-desc">管理家庭组和房间</text>
				</view>
				<text class="menu-arrow">></text>
			</view>

			<view class="menu-item" @click="navigateTo('/pages/profile/settings')">
				<view class="menu-icon">
					<text class="icon-placeholder">⚙️</text>
				</view>
				<view class="menu-content">
					<text class="menu-title">设置</text>
					<text class="menu-desc">应用设置</text>
				</view>
				<text class="menu-arrow">></text>
			</view>

			<view class="menu-item" @click="navigateTo('/pages/profile/about')">
				<view class="menu-icon">
					<text class="icon-placeholder">ℹ️</text>
				</view>
				<view class="menu-content">
					<text class="menu-title">关于</text>
					<text class="menu-desc">应用信息</text>
				</view>
				<text class="menu-arrow">></text>
			</view>
		</view>

		<!-- 特色功能 -->
		<view class="features">
			<view class="features-title">
				<text>主要功能</text>
			</view>
			<view class="feature-item">
				<view class="feature-icon">
					<text class="icon-placeholder">🏡</text>
				</view>
				<view class="feature-content">
					<text class="feature-title">智能设备管理</text>
					<text class="feature-desc">统一管理家中所有智能设备，实现远程控制</text>
				</view>
			</view>

			<view class="feature-item">
				<view class="feature-icon">
					<text class="icon-placeholder">👨‍👩‍👧‍👦</text>
				</view>
				<view class="feature-content">
					<text class="feature-title">家庭组管理</text>
					<text class="feature-desc">创建家庭组，与家人共享智能设备控制权限</text>
				</view>
			</view>

			<view class="feature-item">
				<view class="feature-icon">
					<text class="icon-placeholder">🎭</text>
				</view>
				<view class="feature-content">
					<text class="feature-title">智能场景</text>
					<text class="feature-desc">控制设备，一句话的事</text>
				</view>
			</view>

			<view class="feature-item">
				<view class="feature-icon">
					<text class="icon-placeholder">🔐</text>
				</view>
				<view class="feature-content">
					<text class="feature-title">安全保障</text>
					<text class="feature-desc">多重安全验证，保护您的隐私和数据安全</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userId: ''
			}
		},
		onLoad() {
			this.userId = uni.getStorageSync('userId') || '未登录'
		},
		methods: {
			navigateTo(url) {
				uni.navigateTo({
					url: url
				})
			}
		}
	}
</script>

<style lang="scss">
	.profile-container {
		padding: 20rpx;
		padding-top: calc(var(--status-bar-height) + 20rpx);
		background: linear-gradient(135deg, #E8F8F3 0%, #C7F0E1 50%, #A8E6D1 100%);
		min-height: calc(100vh - var(--status-bar-height) - 40rpx);
	}

	.user-info {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx;
		margin-bottom: 30rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);

		.avatar {
			width: 120rpx;
			height: 120rpx;
			border-radius: 60rpx;
			background: linear-gradient(135deg, #19CD90 0%, #15B882 100%);
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 20rpx;

			.avatar-text {
				font-size: 64rpx;
			}
		}

		.user-id {
			color: #666666;
			font-size: 28rpx;
		}
	}

	.menu-list {
		background: #ffffff;
		border-radius: 20rpx;
		overflow: hidden;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 30rpx 40rpx;
		border-bottom: 2rpx solid #E8F5F0;
		transition: background-color 0.3s;

		&:last-child {
			border-bottom: none;
		}

		&:active {
			background-color: #F8FDFC;
		}

		.menu-icon {
			width: 80rpx;
			height: 80rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-right: 30rpx;

			.icon-placeholder {
				font-size: 40rpx;
			}
		}

		.menu-content {
			flex: 1;
			display: flex;
			flex-direction: column;

			.menu-title {
				font-size: 32rpx;
				color: #333;
				margin-bottom: 8rpx;
			}

			.menu-desc {
				font-size: 24rpx;
				color: #999;
			}
		}

		.menu-arrow {
			color: #19CD90;
			font-size: 32rpx;
		}
	}

	.features {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx 40rpx 0;
		margin: 30rpx 0;
		margin-bottom: var(--window-bottom);
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);

		.features-title {
			margin-bottom: 10rpx;

			text {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}
		}
	}

	.feature-item {
		display: flex;
		align-items: center;
		padding: 30rpx 0;
		border-bottom: 2rpx solid #E8F5F0;

		&:last-child {
			border-bottom: none;
		}

		.feature-icon {
			width: 80rpx;
			height: 80rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-right: 30rpx;

			.icon-placeholder {
				font-size: 40rpx;
			}
		}

		.feature-content {
			flex: 1;
			display: flex;
			flex-direction: column;

			.feature-title {
				font-size: 32rpx;
				color: #333;
				margin-bottom: 8rpx;
			}

			.feature-desc {
				font-size: 24rpx;
				color: #999;
			}
		}
	}
</style>