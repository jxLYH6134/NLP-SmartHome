<template>
	<view>
		<view class="edgeInsetTop"></view>
		<view class="edgeInsetBottom"></view>
	</view>
</template>

<script>
	export default {
		data() {
			return {

			}
		},
		methods: {

		}
	}
</script>

<style lang="scss">
	.edgeInsetTop {
		width: 750rpx;
		height: var(--window-top);
	}

	.edgeInsetBottom {
		width: 750rpx;
		height: var(--window-bottom);
	}
</style>