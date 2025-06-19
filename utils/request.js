import {
	API_CONFIG
} from './config.js'

export function request(path, data = {}) {
	return new Promise((resolve, reject) => {
		uni.request({
			url: API_CONFIG.BASE_URL + path,
			method: 'POST',
			data: data,
			header: {
				'Content-Type': 'application/json'
			},
			success: (res) => {
				// 如果返回的code不为0，显示错误信息
				if (res.data.code !== 0 && res.data.info) {
					uni.showToast({
						title: res.data.info,
						icon: 'error'
					})
				}

				resolve(res.data)
			},
			fail: (err) => {
				uni.showToast({
					title: '请求失败',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

export const authRequest = {
	request(path, data = {}, method) {
		return new Promise((resolve, reject) => {
			const token = uni.getStorageSync('token')
			const userId = uni.getStorageSync('userId')

			if (!token || !userId) {
				uni.showToast({
					title: '请先登录',
					icon: 'none'
				})
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/account/login'
					})
				}, 1500)
				return
			}

			uni.request({
				url: API_CONFIG.BASE_URL + path,
				method: method,
				data: data,
				header: {
					'Content-Type': 'application/json',
					'Authorization': token,
					'X-User-Id': userId
				},
				success: (res) => {
					// 如果返回的code不为0，显示错误信息
					if (res.data.code !== 0 && res.data.info) {
						uni.showToast({
							title: res.data.info,
							icon: 'error'
						})
					}

					// 如果返回401，token无效，清除token并跳转到登录页
					if (res.data.code === 401) {
						uni.removeStorageSync('token')
						uni.removeStorageSync('userId')
						uni.showToast({
							title: '登录已过期，请重新登录',
							icon: 'none'
						})
						setTimeout(() => {
							uni.reLaunch({
								url: '/pages/account/login'
							})
						}, 1500)
						return
					}

					resolve(res.data)
				},
				fail: (err) => {
					uni.showToast({
						title: '请求失败',
						icon: 'none'
					})
					reject(err)
				}
			})
		})
	},

	get(path, data = {}) {
		return this.request(path, data, 'GET')
	},

	post(path, data = {}) {
		return this.request(path, data, 'POST')
	},

	put(path, data = {}) {
		return this.request(path, data, 'PUT')
	},

	delete(path, data = {}) {
		return this.request(path, data, 'DELETE')
	}
}