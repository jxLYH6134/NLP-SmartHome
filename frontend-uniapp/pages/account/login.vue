<template>
	<view class="account-container">
		<view class="account-form">
			<view class="header">
				<view class="logo">
					<text class="logo-text" @click="showBaseUrlDialog">智能家居</text>
				</view>
				<text class="welcome">👋欢迎回来</text>
			</view>
			<input v-model="userId" placeholder="请输入账号" />
			<input v-model="password" placeholder="请输入密码" type="password" />
			<button @click="handleLogin">登录</button>
			<view class="links">
				<text @click="goToRegister">注册账号</text>
				<text @click="goToReset">忘记密码</text>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		userLogin
	} from '@/utils/api.js'

	export default {
		data() {
			return {
				userId: '',
				password: '',
				customBaseUrl: ''
			}
		},
		methods: {
			showBaseUrlDialog() {
				// 从本地存储获取已保存的BASE_URL
				const savedBaseUrl = uni.getStorageSync('customBaseUrl');
				this.customBaseUrl = savedBaseUrl || '';

				uni.showModal({
					title: '设置API地址',
					content: '请输入自定义的BASE_URL',
					editable: true,
					placeholderText: 'http://backend:8080',
					showCancel: true,
					cancelText: '取消',
					confirmText: '保存',
					inputValue: this.customBaseUrl,
					success: (res) => {
						if (res.confirm && res.content) {
							this.customBaseUrl = res.content;
							// 保存到本地存储
							uni.setStorageSync('customBaseUrl', this.customBaseUrl);
							uni.showToast({
								title: '设置成功',
								icon: 'success'
							});
						}
					}
				});
			},
			async handleLogin() {
				if (!this.userId || !this.password) {
					uni.showToast({
						title: '请填写完整信息',
						icon: 'none'
					})
					return
				}

				try {
					const response = await userLogin(this.userId, this.password)
					if (response.code === 0) {
						uni.showToast({
							title: '登录成功',
							icon: 'success'
						})
						// 跳转到首页
						setTimeout(() => {
							uni.switchTab({
								url: '/pages/device/device'
							})
						}, 1500)
					}
				} catch (error) {
					console.error('登录失败:', error)
				}
			},

			goToRegister() {
				uni.navigateTo({
					url: '/pages/account/register'
				})
			},

			goToReset() {
				uni.navigateTo({
					url: '/pages/account/reset'
				})
			}
		}
	}
</script>

<style lang="scss">
	@import './account.scss';
</style>