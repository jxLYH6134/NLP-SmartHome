import {
	authRequest
} from './request.js'

// 获取家庭组信息
export function getFamilyGroup(familyGroupId) {
	return authRequest.get('/api/family/get', {
		familyGroupId: familyGroupId
	})
}

// 创建家庭组
export function createFamilyGroup(groupName) {
	return authRequest.post('/api/family/create', {
		groupName: groupName
	})
}

// 更新家庭组信息
export function updateFamilyGroup(familyGroupId, groupName) {
	return authRequest.put('/api/family/update', {
		familyGroupId: familyGroupId,
		groupName: groupName
	})
}

// 删除家庭组
export function deleteFamilyGroup(familyGroupId) {
	return authRequest.delete('/api/family/delete', {
		familyGroupId: familyGroupId
	})
}

// 获取用户家庭组列表
export function getUserFamilyGroups() {
	return authRequest.get('/api/family/list')
}