<template>
	<view class="account-container">
		<view class="account-form">
			<view class="header">
				<view class="logo">
					<text class="logo-text">智能家居</text>
				</view>
				<text class="welcome">✋重置密码</text>
			</view>
			<input v-model="userId" placeholder="请输入手机号" />
			<view class="code-row">
				<input v-model="verifyCode" placeholder="请输入验证码" />
				<button @click="handleSendCode" :disabled="countdown > 0">
					{{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
				</button>
			</view>
			<input v-model="passwordNew" placeholder="请输入新密码" type="password" />
			<button @click="handleReset">重置密码</button>
			<view class="links center">
				<text @click="goToLogin">返回登录</text>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		forgetPassword,
		sendCode
	} from '@/utils/api.js'

	export default {
		data() {
			return {
				userId: '',
				passwordNew: '',
				verifyCode: '',
				countdown: 0
			}
		},
		methods: {
			async handleSendCode() {
				if (!this.userId) {
					uni.showToast({
						title: '请先输入手机号',
						icon: 'none'
					})
					return
				}

				try {
					const response = await sendCode(this.userId, 1)
					if (response.code === 0) {
						uni.showToast({
							title: '验证码发送成功',
							icon: 'success'
						})
						this.startCountdown()
					}
				} catch (error) {
					console.error('发送验证码失败:', error)
				}
			},

			async handleReset() {
				if (!this.userId || !this.passwordNew || !this.verifyCode) {
					uni.showToast({
						title: '请填写完整信息',
						icon: 'none'
					})
					return
				}

				try {
					const response = await forgetPassword(this.userId, this.passwordNew, this.verifyCode)
					if (response.code === 0) {
						uni.showToast({
							title: '密码重置成功',
							icon: 'success'
						})
						// 跳转到登录页
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					}
				} catch (error) {
					console.error('重置密码失败:', error)
				}
			},

			startCountdown() {
				this.countdown = 5
				const timer = setInterval(() => {
					this.countdown--
					if (this.countdown <= 0) {
						clearInterval(timer)
					}
				}, 1000)
			},

			goToLogin() {
				uni.navigateBack()
			}
		}
	}
</script>

<style lang="scss">
	@import 'account';
</style>