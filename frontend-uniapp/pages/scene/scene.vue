<template>
	<view class="scene-container">
		<image src="/static/AI-BG.jpg" class="ai-bg"></image>
		<!-- 顶部AI图片和描述 -->
		<view class="header-section">
			<view class="ai-image">
				<image src="/static/Apple-Intelligence.jpg" mode="aspectFill" class="ai-img"></image>
			</view>
			<view class="description">
				<text class="title">智能场景控制</text>
				<text class="subtitle">通过自然语言描述，让AI智能控制您的设备</text>
			</view>
		</view>

		<!-- 场景规则列表 -->
		<view class="rules-section">
			<view class="section-title">
				<text>我的场景规则</text>
			</view>
			<view class="rules-list">
				<view v-for="rule in rulesList" :key="rule.ruleId" class="rule-item-wrapper">
					<view class="rule-item" :style="{transform: `translateX(${rule.translateX || 0}px)`}"
						@touchstart="onTouchStart($event, rule)" @touchmove="onTouchMove($event, rule)"
						@touchend="onTouchEnd($event, rule)">
						<view class="rule-content">
							<text class="rule-description">{{ rule.description }}</text>
							<switch :checked="rule.isEnabled" @change="toggleRule(rule.ruleId)" color="#19CD90" />
						</view>
					</view>
					<view class="delete-button" @click="deleteRule(rule.ruleId)">
						<text class="delete-text">删除</text>
					</view>
				</view>
				<view v-if="rulesList.length === 0" class="empty-state">
					<text>暂无场景规则，点击右下角按钮添加</text>
				</view>
			</view>
		</view>

		<!-- 添加按钮 -->
		<view class="add-button" @click="openCreateDialog">
			<text class="add-icon">+</text>
		</view>

		<!-- 创建场景对话框 -->
		<uni-popup ref="createPopup" type="dialog" :mask-click="false">
			<view class="dialog">
				<view class="dialog-title">
					<text>创建智能场景</text>
				</view>
				<view class="dialog-content">
					<textarea v-model="newRuleDescription" class="dialog-textarea"
						placeholder="请描述您的场景需求，例如：在傍晚六点到早上六点开启灯光，色温8000K" maxlength="200" />
				</view>
				<view class="dialog-actions">
					<button class="dialog-btn cancel" @click="closeCreateDialog">取消</button>
					<button class="dialog-btn confirm" @click="createRule"
						:disabled="!newRuleDescription.trim()">创建</button>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
	import {
		getUserRules,
		createRule as createRuleAPI,
		toggleRuleStatus,
		deleteRule as deleteRuleAPI
	} from '@/utils/automation.js'

	export default {
		data() {
			return {
				rulesList: [],
				newRuleDescription: '',
				loading: false,
				startX: 0,
				startTime: 0
			}
		},
		onLoad() {
			this.loadRules()
		},
		methods: {
			// 加载场景规则列表
			async loadRules() {
				try {
					this.loading = true
					const response = await getUserRules()
					if (response.code === 0) {
						this.rulesList = response.data || []
					}
				} catch (error) {
					console.error('加载场景规则失败:', error)
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
				} finally {
					this.loading = false
				}
			},

			// 打开创建对话框
			openCreateDialog() {
				this.newRuleDescription = ''
				this.$refs.createPopup.open()
			},

			// 关闭创建对话框
			closeCreateDialog() {
				this.$refs.createPopup.close()
			},

			// 创建场景规则
			async createRule() {
				if (!this.newRuleDescription.trim()) {
					return
				}

				try {
					const response = await createRuleAPI(this.newRuleDescription.trim())
					if (response.code === 0) {
						uni.showToast({
							title: '创建成功',
							icon: 'success'
						})
						this.closeCreateDialog()
						this.loadRules() // 重新加载列表
					} else {
						uni.showToast({
							title: response.info || '创建失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.showToast({
						title: '创建失败',
						icon: 'none'
					})
				}
			},

			// 切换规则状态
			async toggleRule(ruleId) {
				try {
					const response = await toggleRuleStatus(ruleId)
					if (response.code === 0) {
						// 更新本地状态
						const rule = this.rulesList.find(r => r.ruleId === ruleId)
						if (rule) {
							rule.isEnabled = !rule.isEnabled
						}
						uni.showToast({
							title: rule.isEnabled ? '已启用' : '已禁用',
							icon: 'success'
						})
					} else {
						uni.showToast({
							title: '操作失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					})
				}
			},

			// 删除规则
			async deleteRule(ruleId) {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除这个场景规则吗？',
					success: async (res) => {
						if (res.confirm) {
							try {
								const response = await deleteRuleAPI(ruleId)
								if (response.code === 0) {
									uni.showToast({
										title: '已删除',
										icon: 'success'
									})
									this.loadRules() // 重新加载列表
								} else {
									uni.showToast({
										title: '删除失败',
										icon: 'none'
									})
								}
							} catch (error) {
								console.error('删除场景规则失败:', error)
								uni.showToast({
									title: '删除失败',
									icon: 'none'
								})
							}
						}
					}
				})
			},

			// 触摸开始
			onTouchStart(event, rule) {
				this.startX = event.touches[0].clientX
				this.startTime = Date.now()
				// 重置其他项的位置
				this.rulesList.forEach(item => {
					if (item.ruleId !== rule.ruleId) {
						item.translateX = 0
					}
				})
			},

			// 触摸移动
			onTouchMove(event, rule) {
				const currentX = event.touches[0].clientX
				const deltaX = currentX - this.startX

				// 只允许向左滑动
				if (deltaX < 0) {
					rule.translateX = Math.max(deltaX, -70) // 最大滑动80px
				} else {
					rule.translateX = 0
				}
			},

			// 触摸结束
			onTouchEnd(event, rule) {
				const endTime = Date.now()
				const duration = endTime - this.startTime

				// 如果滑动距离超过40px或者快速滑动，则显示删除按钮
				if (Math.abs(rule.translateX) > 60 || (Math.abs(rule.translateX) > 15 && duration < 300)) {
					rule.translateX = -70
				} else {
					rule.translateX = 0
				}
			}
		}
	}
</script>

<style lang="scss">
	.scene-container {
		padding: 20rpx;
		padding-top: calc(var(--status-bar-height) + 60rpx);
		padding-bottom: 280rpx;

		.ai-bg {
			position: fixed;
			top: 0;
			left: 0;
			width: 100vw;
			height: 100vh;
			z-index: -1;
			object-fit: cover;
		}

		.ai-bg::after {
			content: '';
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: rgba(255, 255, 255, 0.4);
			pointer-events: none;
		}
	}

	.header-section {
		margin-bottom: 40rpx;
		text-align: center;

		.ai-image {
			margin-bottom: 30rpx;

			.ai-img {
				width: 180rpx;
				height: 180rpx;
				border-radius: 40rpx;
				box-shadow: 0 20rpx 60rpx rgba(255, 255, 255, 0.5);
			}
		}

		.description {
			.title {
				display: block;
				font-size: 36rpx;
				font-weight: bold;
				color: #ffffff;
				margin-bottom: 15rpx;
			}

			.subtitle {
				display: block;
				font-size: 26rpx;
				color: rgba(255, 255, 255, 0.8);
				line-height: 1.5;
			}
		}
	}

	.rules-section {
		.section-title {
			margin-bottom: 30rpx;

			text {
				font-size: 32rpx;
				font-weight: bold;
				color: #ffffff;
			}
		}

		.rules-list {
			.rule-item-wrapper {
				position: relative;
				margin-bottom: 20rpx;
				overflow: hidden;
				border-radius: 20rpx;
				box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);

				.rule-item {
					background: rgba(255, 255, 255, 0.9);
					border-radius: 20rpx;
					padding: 30rpx;
					transition: transform 0.3s ease;
					position: relative;
					z-index: 2;

					.rule-content {
						display: flex;
						align-items: flex-start;
						justify-content: space-between;

						.rule-description {
							flex: 1;
							font-size: 28rpx;
							color: #333;
							line-height: 1.5;
							margin-right: 20rpx;
						}
					}
				}

				.delete-button {
					position: absolute;
					top: 0;
					right: -30rpx;
					bottom: 0;
					width: 190rpx;
					background: #ff4757;
					display: flex;
					align-items: center;
					justify-content: center;
					z-index: 1;
					border-radius: 0 22rpx 22rpx 0;

					.delete-text {
						color: #fff;
						font-size: 28rpx;
						font-weight: bold;
						margin-right: 20rpx;
					}
				}
			}

			.empty-state {
				text-align: center;
				padding: 80rpx 40rpx;
				background: rgba(255, 255, 255, 0.9);
				border-radius: 20rpx;

				text {
					font-size: 28rpx;
					color: #999;
				}
			}
		}
	}

	.add-button {
		position: fixed;
		bottom: calc(var(--window-bottom) + 30rpx);
		right: 40rpx;
		width: 100rpx;
		height: 100rpx;
		background: linear-gradient(135deg, #19CD90 0%, #15B882 100%);
		border-radius: 50rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 15rpx 40rpx rgba(25, 205, 144, 0.4);
		z-index: 999;

		.add-icon {
			font-size: 48rpx;
			color: #fff;
			font-weight: bold;
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

			.dialog-textarea {
				width: 100%;
				height: 200rpx;
				border: 2rpx solid #e0e0e0;
				border-radius: 12rpx;
				padding: 20rpx;
				font-size: 28rpx;
				box-sizing: border-box;
				resize: none;
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