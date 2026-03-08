// 注销确认模态框组件
Vue.component('logout-confirm-modal', {
    props: ['visible'],
    template: `
        <div :class="{'active': visible}" class="modal-overlay" @click.self="close">
            <div class="modal-box w-full max-w-md mx-4">
                <div class="bg-gradient-to-r from-rose-400 to-rose-500 text-white p-6 rounded-t-lg">
                    <div class="flex items-center">
                        <i class="fas fa-exclamation-triangle text-xl mr-3"></i>
                        <h3 class="text-xl font-bold">确认注销</h3>
                    </div>
                </div>

                <div class="p-6">
                    <div class="text-center">
                        <div class="w-16 h-16 bg-rose-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i class="fas fa-sign-out-alt text-rose-400 text-2xl"></i>
                        </div>
                        <h4 class="text-lg font-semibold text-rose-800 mb-2">确认要注销吗？</h4>
                        <p class="text-rose-600">
                            注销后，您的个人信息将从系统中移除，不会再自动签到。<br>
                            下次使用需要重新登录。
                        </p>
                    </div>
                </div>

                <div class="border-t border-pink-100 p-6">
                    <div class="flex justify-center gap-4">
                        <button @click="close"
                                class="px-6 py-2 border border-pink-300 rounded-lg text-pink-600 hover:bg-pink-50 transition-colors">
                            取消
                        </button>
                        <button @click="confirm"
                                class="bg-rose-400 text-white px-6 py-2 rounded-lg font-medium hover:bg-rose-500 transition-colors flex items-center">
                            <i class="fas fa-sign-out-alt mr-2"></i>
                            确认注销
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `,
    methods: {
        close() {
            this.$emit('close');
        },
        confirm() {
            this.$emit('confirm');
        }
    }
});
