<template>
	<view class="family-container">
		<view class="header">
			<text class="title">家庭组管理</text>
		</view>

		<!-- 无家庭组状态 -->
		<view v-if="!familyGroup && !loading" class="empty-state">
			<view class="empty-icon">
				<text>🏠</text>
			</view>
			<view class="empty-title">
				<text>暂无家庭组</text>
			</view>
			<view class="empty-desc">
				<text>创建或加入一个家庭组，与家人共享智能设备</text>
			</view>
			<view class="empty-actions">
				<button class="action-btn primary" @click="showCreateDialog">
					创建家庭组
				</button>
				<button class="action-btn secondary" @click="showJoinDialog">
					加入家庭组
				</button>
			</view>
		</view>

		<!-- 家庭组信息 -->
		<view v-if="familyGroup" class="family-list">
			<view class="family-info">
				<view class="family-title">
					<text v-if="familyGroup.ownerId === currentUserId" class="owner-badge">管理员</text>
					<text v-else class="member-badge">成员</text>
					<text class="family-name">{{ familyGroup.groupName }}</text>
					<view v-if="familyGroup.ownerId === currentUserId" class="admin-actions">
						<text class="action-btn" @click="generateInvite(familyGroup)">邀请</text>
						<text class="action-btn" @click="showEditDialog(familyGroup)">编辑</text>
						<text class="action-btn delete" @click="deleteGroup(familyGroup)">删除</text>
					</view>
					<view v-else class="member-actions">
						<text class="action-btn delete" @click="leaveGroup(familyGroup)">退出</text>
					</view>
				</view>
				<text class="family-id">ID: {{ familyGroup.familyGroupId }}</text>
			</view>

			<!-- 房间管理 -->
			<view class="room-section">
				<view class="room-header">
					<text class="room-title">我的房间</text>
				</view>

				<view v-if="userRooms.length === 0" class="no-rooms">
					<text>暂无房间，请先创建房间</text>
				</view>

				<view v-else class="room-list">
					<view v-for="room in userRooms" :key="room.roomId" class="room-item">
						<view class="room-info">
							<text class="room-name">{{ room.roomName }}</text>
							<text class="room-status"
								:class="{ active: room.familyGroupId === familyGroup.familyGroupId }">
								{{ room.familyGroupId === familyGroup.familyGroupId ? '已加入' : '未加入' }}
							</text>
						</view>
						<switch :checked="room.familyGroupId === familyGroup.familyGroupId"
							@change="toggleRoom(room, familyGroup.familyGroupId)" class="room-switch" color="#19CD90" />
					</view>
				</view>
			</view>
		</view>

		<!-- 创建家庭组对话框 -->
		<uni-popup ref="createPopup" type="dialog" :mask-click="false">
			<view class="dialog">
				<view class="dialog-title">
					<text>创建家庭组</text>
				</view>
				<view class="dialog-content">
					<input v-model="newGroupName" class="dialog-input" placeholder="请输入家庭组名称" maxlength="20" />
				</view>
				<view class="dialog-actions">
					<button class="dialog-btn cancel" @click="closeCreateDialog">取消</button>
					<button class="dialog-btn confirm" @click="createGroup" :disabled="!newGroupName.trim()">创建</button>
				</view>
			</view>
		</uni-popup>

		<!-- 编辑家庭组对话框 -->
		<uni-popup ref="editPopup" type="dialog" :mask-click="false">
			<view class="dialog">
				<view class="dialog-title">
					<text>编辑家庭组</text>
				</view>
				<view class="dialog-content">
					<input v-model="editGroupName" class="dialog-input" placeholder="请输入家庭组名称" maxlength="20" />
				</view>
				<view class="dialog-actions">
					<button class="dialog-btn cancel" @click="closeEditDialog">取消</button>
					<button class="dialog-btn confirm" @click="updateGroup"
						:disabled="!editGroupName.trim()">保存</button>
				</view>
			</view>
		</uni-popup>

		<!-- 加载状态 -->
		<view v-if="loading" class="loading">
			<text>加载中...</text>
		</view>
	</view>
</template>

<script>
	import {
		getFamilyGroup,
		createFamilyGroup,
		updateFamilyGroup,
		deleteFamilyGroup,
		joinFamilyGroup,
		leaveFamilyGroup
	} from '@/utils/family.js'
	import {
		updateRoom,
		getUserRooms
	} from '@/utils/room.js'
	import {
		generateInviteText,
		extractInviteCode
	} from '@/utils/base64.js'

	export default {
		data() {
			return {
				familyGroup: null,
				userRooms: [],
				currentUserId: '',
				loading: false,
				newGroupName: '',
				editGroupName: ''
			}
		},
		onLoad() {
			this.currentUserId = uni.getStorageSync('userId')
			this.loadData()
		},
		methods: {
			async loadData() {
				this.loading = true
				try {
					// 并行加载家庭组和房间数据
					const [familyResponse, roomResponse] = await Promise.all([
						getFamilyGroup(),
						getUserRooms()
					])

					if (familyResponse.code === 0) {
						this.familyGroup = familyResponse.data
						if (!this.familyGroup) {
							this.checkClipboard()
						}
					}

					if (roomResponse.code === 0) {
						this.userRooms = roomResponse.data || []
					}
				} catch (error) {
					uni.showToast({
						title: '加载失败，请重试',
						icon: 'none'
					})
				} finally {
					this.loading = false
				}
			},

			showCreateDialog() {
				// 确保编辑弹窗关闭
				if (this.$refs.editPopup) {
					this.$refs.editPopup.close()
				}
				this.newGroupName = ''
				this.$refs.createPopup.open()
			},

			closeCreateDialog() {
				this.$refs.createPopup.close()
			},

			async createGroup() {
				if (!this.newGroupName.trim()) {
					return
				}

				try {
					const response = await createFamilyGroup(this.newGroupName.trim())
					if (response.code === 0) {
						uni.showToast({
							title: '创建成功',
							icon: 'success'
						})
						this.closeCreateDialog()
						this.loadData()
					} else {
						uni.showToast({
							title: response.info || '创建失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					})
				}
			},

			showJoinDialog() {
				uni.showModal({
					title: '加入家庭组',
					content: '复制邀请码后回到此页面即可自动识别并加入家庭组',
					showCancel: false
				})
			},

			showEditDialog(group) {
				// 确保创建弹窗关闭
				if (this.$refs.createPopup) {
					this.$refs.createPopup.close()
				}
				this.editGroupName = group.groupName
				this.$refs.editPopup.open()
			},

			closeEditDialog() {
				this.$refs.editPopup.close()
			},

			async updateGroup() {
				if (!this.editGroupName.trim()) {
					return
				}

				try {
					const response = await updateFamilyGroup(
						this.editGroupName.trim()
					)
					if (response.code === 0) {
						uni.showToast({
							title: '修改成功',
							icon: 'success'
						})
						this.closeEditDialog()
						this.loadData()
					} else {
						uni.showToast({
							title: response.info || '修改失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					})
				}
			},

			async toggleRoom(room, familyGroupId) {
				try {
					const newFamilyGroupId = room.familyGroupId === familyGroupId ? null : familyGroupId
					const response = await updateRoom(room.roomId, room.roomName, newFamilyGroupId)

					if (response.code === 0) {
						// 更新本地数据
						const roomIndex = this.userRooms.findIndex(r => r.roomId === room.roomId)
						if (roomIndex !== -1) {
							this.userRooms[roomIndex].familyGroupId = newFamilyGroupId
						}
					} else {
						uni.showToast({
							title: response.info || '操作失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					})
				}
			},

			// 生成邀请码
			generateInvite(group) {
				try {
					const inviteText = generateInviteText(group.groupName, group.familyGroupId)

					// 复制到剪贴板
					uni.setClipboardData({
						data: inviteText,
						success: () => {
							uni.showToast({
								title: '邀请码已复制',
								icon: 'success'
							})
						},
						fail: () => {
							uni.showModal({
								title: '邀请码',
								content: inviteText,
								showCancel: false
							})
						}
					})
				} catch (error) {
					uni.showToast({
						title: '生成邀请码失败',
						icon: 'none'
					})
				}
			},

			// 检查剪贴板中的邀请码
			checkClipboard() {
				uni.getClipboardData({
					success: (res) => {
						const familyGroupId = extractInviteCode(res.data)
						if (familyGroupId) {
							this.handleInviteCode(familyGroupId, res.data)
						}
					}
				})
			},

			// 处理邀请码
			handleInviteCode(familyGroupId, inviteText) {
				// 检查是否已经在该家庭组中
				if (this.familyGroup && this.familyGroup.familyGroupId === familyGroupId) {
					uni.showToast({
						title: '您已在该家庭组中',
						icon: 'none'
					})
					uni.setClipboardData({
						data: ''
					})
					return
				}

				// 提取家庭组名称
				const groupNameMatch = inviteText.match(/邀请你进入家庭组(.*?)，/)
				const groupName = groupNameMatch ? groupNameMatch[1] : '未知家庭组'

				uni.showModal({
					title: '加入家庭组',
					content: `是否加入家庭组"${groupName}"？`,
					success: (res) => {
						if (res.confirm) {
							this.joinFamily(familyGroupId)
						} else {
							uni.setClipboardData({
								data: ''
							})
						}
					}
				})
			},

			// 加入家庭组
			async joinFamily(familyGroupId) {
				try {
					const response = await joinFamilyGroup(familyGroupId)

					if (response.code === 0) {
						uni.showToast({
							title: '加入家庭组成功',
							icon: 'success'
						})
						// 清空剪贴板
						uni.setClipboardData({
							data: ''
						})
						// 重新加载数据
						this.loadData()
					} else {
						uni.showToast({
							title: response.info || '加入家庭组失败',
							icon: 'none'
						})
					}
				} catch (error) {
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					})
				}
			},

			// 删除家庭组
			async deleteGroup(group) {
				uni.showModal({
					title: '确认删除',
					content: `确定要删除家庭组"${group.groupName}"吗？此操作不可恢复。`,
					success: async (res) => {
						if (res.confirm) {
							try {
								const response = await deleteFamilyGroup()
								if (response.code === 0) {
									uni.showToast({
										title: '删除成功'
									})
									this.loadData()
								} else {
									uni.showToast({
										title: response.info || '删除失败',
										icon: 'none'
									})
								}
							} catch (error) {
								uni.showToast({
									title: '网络错误，请重试',
									icon: 'none'
								})
							}
						}
					}
				})
			},

			// 退出家庭组
			async leaveGroup(group) {
				uni.showModal({
					title: '退出家庭组',
					content: `确定要退出家庭组"${group.groupName}"吗？`,
					success: async (res) => {
						if (res.confirm) {
							try {
								const response = await leaveFamilyGroup()
								if (response.code === 0) {
									uni.showToast({
										title: '退出家庭组成功'
									})
									this.loadData()
								} else {
									uni.showToast({
										title: response.info || '退出家庭组失败',
										icon: 'none'
									})
								}
							} catch (error) {
								uni.showToast({
									title: '网络错误，请重试',
									icon: 'none'
								})
							}
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss">
	.family-container {
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

	.empty-state {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 80rpx 40rpx;
		text-align: center;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
		position: relative;

		.empty-icon {
			margin-bottom: 30rpx;

			text {
				font-size: 80rpx;
			}
		}

		.empty-title {
			margin-bottom: 20rpx;

			text {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
			}
		}

		.empty-desc {
			margin-bottom: 60rpx;

			text {
				font-size: 28rpx;
				color: #666666;
				line-height: 1.5;
			}
		}

		.empty-actions {
			display: flex;

			.action-btn {
				padding: 10rpx 40rpx;
				border-radius: 12rpx;
				font-size: 28rpx;
				border: none;

				&.primary {
					background: linear-gradient(135deg, #19CD90 0%, #15B882 100%);
					color: #ffffff;
					box-shadow: 0 8rpx 24rpx rgba(25, 205, 144, 0.3);
				}

				&.secondary {
					background: #F8FDFC;
					color: #19CD90;
					border: 2rpx solid #19CD90;
				}
			}
		}
	}

	.family-list {
		display: flex;
		flex-direction: column;
		background: #ffffff;
		border-radius: 20rpx;
		padding: 40rpx;
		box-shadow: 0 20rpx 60rpx rgba(25, 205, 144, 0.15);
	}

	.family-info {
		margin-bottom: 30rpx;

		.family-title {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-bottom: 12rpx;

			.owner-badge {
				background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
				color: #333;
				padding: 6rpx 12rpx;
				border-radius: 6rpx;
				font-size: 18rpx;
				font-weight: bold;
				box-shadow: 0 4rpx 12rpx rgba(255, 215, 0, 0.3);
				flex-shrink: 0;
			}

			.member-badge {
				background: linear-gradient(135deg, #C7F0E1 0%, #A8E6D1 100%);
				color: #19CD90;
				padding: 6rpx 12rpx;
				border-radius: 6rpx;
				font-size: 18rpx;
				font-weight: 500;
				flex-shrink: 0;
			}

			.family-name {
				font-size: 32rpx;
				font-weight: bold;
				color: #333;
				flex: 1;
			}
		}

		.family-id {
			font-size: 20rpx;
			color: #999;
		}
	}

	.admin-actions,
	.member-actions {
		display: flex;
		gap: 8rpx;
		flex-shrink: 0;

		.action-btn {
			color: #19CD90;
			font-size: 22rpx;
			padding: 6rpx 12rpx;
			background: rgba(25, 205, 144, 0.1);
			border-radius: 6rpx;
			border: 1rpx solid rgba(25, 205, 144, 0.3);
			transition: all 0.3s ease;
			text-align: center;
			min-width: 60rpx;

			&:active {
				background: rgba(25, 205, 144, 0.2);
				transform: scale(0.95);
			}

			&.delete {
				color: #FF4757;
				background: rgba(255, 71, 87, 0.1);
				border: 1rpx solid rgba(255, 71, 87, 0.3);

				&:active {
					background: rgba(255, 71, 87, 0.2);
				}
			}
		}
	}

	.room-section {
		border-top: 1rpx solid #f0f0f0;
		padding-top: 30rpx;

		.room-header {
			margin-bottom: 30rpx;

			.room-title {
				font-size: 28rpx;
				font-weight: 500;
				color: #333;
			}
		}

		.no-rooms {
			text-align: center;
			padding: 40rpx 0;

			text {
				font-size: 26rpx;
				color: #999;
			}
		}
	}

	.room-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;

		.room-item {
			display: flex;
			align-items: center;
			padding: 24rpx;
			background-color: #f8f8f8;
			border-radius: 12rpx;

			.room-info {
				flex: 1;

				.room-name {
					display: block;
					font-size: 28rpx;
					color: #333;
				}

				.room-status {
					display: block;
					font-size: 22rpx;
					color: #999;

					&.active {
						color: #19CD90;
					}
				}
			}

			.room-switch {
				transform: scale(0.8);
			}
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

	.loading {
		text-align: center;
		padding: 100rpx 0;

		text {
			font-size: 28rpx;
			color: #666;
		}
	}
</style>