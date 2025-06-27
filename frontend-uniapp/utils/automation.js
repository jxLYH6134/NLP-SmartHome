import {
	authRequest
} from './request.js'

// 创建自动化规则
export function createRule(description) {
	return authRequest.post('/api/automation/create', {
		description: description
	})
}

// 切换规则状态（启用/禁用）
export function toggleRuleStatus(ruleId) {
	return authRequest.put('/api/automation/toggle', {
		ruleId: ruleId
	})
}

// 删除自动化规则
export function deleteRule(ruleId) {
	return authRequest.delete('/api/automation/delete', {
		ruleId: ruleId
	})
}

// 获取用户自动化规则列表
export function getUserRules() {
	return authRequest.get('/api/automation/list')
}