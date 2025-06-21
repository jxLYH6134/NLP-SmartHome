import {
	authRequest
} from './request.js'

// 获取设备信息
export function getDevice(deviceId) {
	return authRequest.get('/api/device/get', {
		deviceId: deviceId
	})
}

// 控制设备
export function controlDevice(deviceId, params) {
	return authRequest.post('/api/device/control', {
		deviceId: deviceId,
		params: params
	})
}

// 更新设备信息
export function updateDevice(deviceId, deviceName, roomId) {
	const params = {
		deviceId: deviceId
	}
	if (deviceName !== undefined) {
		params.deviceName = deviceName
	}
	if (roomId !== undefined) {
		params.roomId = roomId
	}
	return authRequest.put('/api/device/update', params)
}

// 删除设备
export function deleteDevice(deviceId) {
	return authRequest.delete('/api/device/delete', {
		deviceId: deviceId
	})
}

// 获取房间设备列表
export function getRoomDevices(roomId) {
	return authRequest.get('/api/device/list/room', {
		roomId: roomId
	})
}

// 获取用户设备列表
export function getUserDevices() {
	return authRequest.get('/api/device/list/user')
}