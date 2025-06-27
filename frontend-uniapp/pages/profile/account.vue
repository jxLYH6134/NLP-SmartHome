<template>
	<view class="account-container">
		<view class="header">
			<text class="title">账户管理</text>
		</view>

		<view class="form-container">
			<view class="form-item">
				<text class="label">当前密码</text>
				<input class="input" type="password" v-model="oldPassword" placeholder="请输入当前密码" />
			</view>

			<view class="form-item">
				<text class="label">新密码</text>
				<input class="input" type="password" v-model="newPassword" placeholder="请输入新密码" />
			</view>

			<view class="form-item">
				<text class="label">确认新密码</text>
				<input class="input" type="password" v-model="confirmPassword" placeholder="请再次输入新密码" />
			</view>

			<button class="submit-btn" @click="changePassword" :disabled="loading">
				{{ loading ? '修改中...' : '修改密码' }}
			</button>

			<button class="logout-btn" @click="logout">
				退出登录
			</button>
		</view>
	</view>
</template>

<script>
	import {
		changePassword
	} from '@/utils/api.js'

	export default {
		data() {
			return {
				oldPassword: '',
				newPassword: '',
				confirmPassword: '',
				loading: false
			}
		},
		methods: {
			async changePassword() {
				if (!this.oldPassword) {
					uni.showToast({
						title: '请输入当前密码',
						icon: 'none'
					})
					return
				}

				if (!this.newPassword) {
					uni.showToast({
						title: '请输入新密码',
						icon: 'none'
					})
					return
				}

				if (this.newPassword !== this.confirmPassword) {
					uni.showToast({
						title: '两次输入的密码不一致',
						icon: 'none'
					})
					return
				}

				this.loading = true

				try {
					const userId = uni.getStorageSync('userId')
					const response = await changePassword(userId, this.oldPassword, this.newPassword)

					if (response.code === 0) {
						uni.showToast({
							title: '密码修改成功',
							icon: 'success'
						})

						// 延迟返回上一页
						setTimeout(() => {
							// 清空表单
							this.oldPassword = ''
							this.newPassword = ''
							this.confirmPassword = ''
							uni.navigateBack()
						}, 1500)
					} else {
						uni.showToast({
							title: response.info || '修改失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('修改密码失败:', error)
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					})
				} finally {
					this.loading = false
				}
			},
			logout() {
				uni.showModal({
					title: '确认退出',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							// 清除存储
							uni.removeStorageSync('token')
							uni.removeStorageSync('userId')

							// 重启到登录页面
							uni.reLaunch({
								url: '/pages/account/login'
							})
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss">
	.account-container {
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

	.form-container {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
	}

	.form-item {
		margin-bottom: 40rpx;

		.label {
			display: block;
			font-size: 28rpx;
			color: #333;
			margin-bottom: 20rpx;
			font-weight: 500;
		}

		.input {
			width: 100%;
			height: 80rpx;
			border: 2rpx solid #E8F5F0;
			border-radius: 12rpx;
			padding: 0 20rpx;
			font-size: 28rpx;
			box-sizing: border-box;
			transition: border-color 0.3s;

			&:focus {
				border-color: #19CD90;
			}
		}
	}

	.submit-btn {
		width: 100%;
		height: 88rpx;
		background: linear-gradient(135deg, #19CD90 0%, #15B882 100%);
		color: #ffffff;
		border: none;
		border-radius: 12rpx;
		font-size: 32rpx;
		font-weight: 500;
		margin-top: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(25, 205, 144, 0.3);
		transition: opacity 0.3s;

		&:active {
			box-shadow: 0 4rpx 12rpx rgba(25, 205, 144, 0.3);
		}

		&:disabled {
			opacity: 0.6;
		}
	}

	.logout-btn {
		width: 100%;
		height: 88rpx;
		background: #F8FDFC;
		color: #19CD90;
		border: 2rpx solid #19CD90;
		border-radius: 12rpx;
		font-size: 32rpx;
		font-weight: 500;
		margin-top: 20rpx;
		transition: all 0.3s;

		&:active {
			background: #19CD90;
			color: #ffffff;
		}

		&:active:not(:disabled) {
			opacity: 0.8;
		}
	}
</style>