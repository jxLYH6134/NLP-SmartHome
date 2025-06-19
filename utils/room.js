import {
	authRequest
} from './request.js'

// 获取房间信息
export function getRoom(roomId) {
	return authRequest.get('/api/room/get', {
		roomId: roomId
	})
}

// 创建房间
export function createRoom(roomName, familyGroupId) {
	return authRequest.post('/api/room/create', {
		roomName: roomName,
		familyGroupId: familyGroupId
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

// 获取家庭组房间列表
export function getFamilyGroupRooms(familyGroupId) {
	return authRequest.get('/api/room/list/family', {
		familyGroupId: familyGroupId
	})
}

// 获取用户房间列表
export function getUserRooms() {
	return authRequest.get('/api/room/list/user')
}