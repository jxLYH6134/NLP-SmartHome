/**
 * Base64URL 编解码工具
 * 用于家庭组邀请码的生成和解析
 */

/**
 * 将UUID转换为Base64URL格式
 * @param {string} uuid - 标准UUID字符串
 * @returns {string} Base64URL编码的字符串
 */
export function uuidToBase64URL(uuid) {
	if (!uuid || typeof uuid !== 'string') {
		throw new Error('Invalid UUID')
	}

	// 移除UUID中的连字符
	const cleanUuid = uuid.replace(/-/g, '')

	// 将十六进制字符串转换为字节数组
	const bytes = []
	for (let i = 0; i < cleanUuid.length; i += 2) {
		bytes.push(parseInt(cleanUuid.substr(i, 2), 16))
	}

	// 转换为Base64
	let base64 = ''
	if (typeof btoa !== 'undefined') {
		// 浏览器环境
		base64 = btoa(String.fromCharCode.apply(null, bytes))
	} else {
		// 其他环境的fallback
		const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
		let result = ''
		let i = 0
		while (i < bytes.length) {
			const a = bytes[i++]
			const b = i < bytes.length ? bytes[i++] : 0
			const c = i < bytes.length ? bytes[i++] : 0

			const bitmap = (a << 16) | (b << 8) | c

			result += chars.charAt((bitmap >> 18) & 63)
			result += chars.charAt((bitmap >> 12) & 63)
			result += i - 2 < bytes.length ? chars.charAt((bitmap >> 6) & 63) : '='
			result += i - 1 < bytes.length ? chars.charAt(bitmap & 63) : '='
		}
		base64 = result
	}

	// 转换为Base64URL格式（替换+为-，/为_，移除=填充）
	return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')
}

/**
 * 将Base64URL格式转换回UUID
 * @param {string} base64url - Base64URL编码的字符串
 * @returns {string} 标准UUID字符串
 */
export function base64URLToUuid(base64url) {
	if (!base64url || typeof base64url !== 'string') {
		throw new Error('Invalid Base64URL string')
	}

	// 转换回标准Base64格式
	let base64 = base64url.replace(/-/g, '+').replace(/_/g, '/')

	// 添加必要的填充
	while (base64.length % 4) {
		base64 += '='
	}

	// 解码Base64
	let bytes = []
	if (typeof atob !== 'undefined') {
		// 浏览器环境
		const binaryString = atob(base64)
		for (let i = 0; i < binaryString.length; i++) {
			bytes.push(binaryString.charCodeAt(i))
		}
	} else {
		// 其他环境的fallback
		const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
		const lookup = {}
		for (let i = 0; i < chars.length; i++) {
			lookup[chars[i]] = i
		}

		for (let i = 0; i < base64.length; i += 4) {
			const a = lookup[base64[i]] || 0
			const b = lookup[base64[i + 1]] || 0
			const c = lookup[base64[i + 2]] || 0
			const d = lookup[base64[i + 3]] || 0

			const bitmap = (a << 18) | (b << 12) | (c << 6) | d

			bytes.push((bitmap >> 16) & 255)
			if (base64[i + 2] !== '=') bytes.push((bitmap >> 8) & 255)
			if (base64[i + 3] !== '=') bytes.push(bitmap & 255)
		}
	}

	// 将字节数组转换为十六进制字符串
	const hex = bytes.map(b => b.toString(16).padStart(2, '0')).join('')

	// 格式化为标准UUID格式
	if (hex.length !== 32) {
		throw new Error('Invalid UUID length after decoding')
	}

	return [
		hex.substr(0, 8),
		hex.substr(8, 4),
		hex.substr(12, 4),
		hex.substr(16, 4),
		hex.substr(20, 12)
	].join('-')
}

/**
 * 生成家庭组邀请文本
 * @param {string} groupName - 家庭组名称
 * @param {string} familyGroupId - 家庭组UUID
 * @returns {string} 邀请文本
 */
export function generateInviteText(groupName, familyGroupId) {
	const base64Code = uuidToBase64URL(familyGroupId)
	return `邀请你进入家庭组${groupName}，复制这条信息打开APP→${base64Code}`
}

/**
 * 从文本中提取邀请码
 * @param {string} text - 包含邀请码的文本
 * @returns {string|null} 提取的家庭组UUID，如果没有找到则返回null
 */
export function extractInviteCode(text) {
	if (!text || typeof text !== 'string') {
		return null
	}

	// 匹配邀请文本格式
	const regex = /邀请你进入家庭组.*?复制这条信息打开APP→([A-Za-z0-9_-]+)/
	const match = text.match(regex)

	if (!match || !match[1]) {
		return null
	}

	try {
		return base64URLToUuid(match[1])
	} catch (error) {
		console.error('Failed to decode invite code:', error)
		return null
	}
}