import {
	API_CONFIG
} from './config.js'
import {
	authRequest,
	request
} from './request.js'
import md5 from 'js-md5'

function generateSign(userId) {
	const timestamp = Date.now()
	const str = userId + timestamp + API_CONFIG.SIGN_SALT
	return {
		timestamp: timestamp,
		sign: md5(str)
	}
}

// 发送验证码
export function sendCode(userId, type) {
	return new Promise(async (resolve, reject) => {
		try {
			const {
				timestamp,
				sign
			} = generateSign(userId)

			const response = await request('/api/sendVerifyCode', {
				userId: userId,
				type: type,
				timestamp: timestamp,
				sign: sign
			})

			resolve(response)
		} catch (error) {
			reject(error)
		}
	})
}

// 用户注册
export function userRegister(userId, password, verifyCode) {
	return new Promise(async (resolve, reject) => {
		try {
			const {
				timestamp,
				sign
			} = generateSign(userId)

			const response = await request('/api/userRegister', {
				userId: userId,
				password: password,
				verifyCode: verifyCode,
				timestamp: timestamp,
				sign: sign
			})

			resolve(response)
		} catch (error) {
			reject(error)
		}
	})
}

// 用户登录
export function userLogin(userId, password) {
	return new Promise(async (resolve, reject) => {
		try {
			const {
				timestamp,
				sign
			} = generateSign(userId)

			const response = await request('/api/userLogin', {
				userId: userId,
				password: password,
				timestamp: timestamp,
				sign: sign
			})

			// 如果登录成功，保存token和用户ID
			if (response.code === 0) {
				uni.setStorageSync('token', response.info)
				uni.setStorageSync('userId', userId)
			}

			resolve(response)
		} catch (error) {
			reject(error)
		}
	})
}

// 修改密码
export function changePassword(userId, password, passwordNew) {
	return new Promise(async (resolve, reject) => {
		try {
			const {
				timestamp,
				sign
			} = generateSign(userId)

			const response = await authRequest.post('/api/changePassword', {
				userId: userId,
				password: password,
				passwordNew: passwordNew,
				timestamp: timestamp,
				sign: sign
			})

			resolve(response)
		} catch (error) {
			reject(error)
		}
	})
}

// 忘记密码
export function forgetPassword(userId, passwordNew, verifyCode) {
	return new Promise(async (resolve, reject) => {
		try {
			const {
				timestamp,
				sign
			} = generateSign(userId)

			const response = await request('/api/forgetPassword', {
				userId: userId,
				passwordNew: passwordNew,
				verifyCode: verifyCode,
				timestamp: timestamp,
				sign: sign
			})

			resolve(response)
		} catch (error) {
			reject(error)
		}
	})
}

// 测试认证接口
export function testAuth() {
	return authRequest.post('/api/testAuth')
}