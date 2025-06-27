import {
	authRequest
} from './request.js'

// 创建房间
export function createRoom(roomName) {
	return authRequest.post('/api/room/create', {
		roomName: roomName
	})
}

// 更新房间信息
export function updateRoom(roomId, roomName, familyGroupId) {
	return authRequest.put('/api/room/update', {
		roomId: roomId,
		roomName: roomName,
		familyGroupId: familyGroupId
	})
}

// 删除房间
export function deleteRoom(roomId) {
	return authRequest.delete('/api/room/delete', {
		roomId: roomId
	})
}

// 获取个人房间列表
export function getUserRooms() {
	return authRequest.get('/api/room/user')
}

// 获取全部房间列表
export function getAllRooms() {
	return authRequest.get('/api/room/family')
}
