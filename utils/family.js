import {
	authRequest
} from './request.js'

// 获取家庭组信息
export function getFamilyGroup() {
	return authRequest.get('/api/family/get')
}

// 创建家庭组
export function createFamilyGroup(groupName) {
	return authRequest.post('/api/family/create', {
		groupName: groupName
	})
}

// 更新家庭组信息
export function updateFamilyGroup(groupName) {
	return authRequest.put('/api/family/update', {
		groupName: groupName
	})
}

// 删除家庭组
export function deleteFamilyGroup() {
	return authRequest.delete('/api/family/delete')
}

// 加入家庭组
export function joinFamilyGroup(familyGroupId) {
	return authRequest.post('/api/family/join', {
		familyGroupId: familyGroupId
	})
}

// 退出家庭组
export function leaveFamilyGroup() {
	return authRequest.post('/api/family/leave')
}