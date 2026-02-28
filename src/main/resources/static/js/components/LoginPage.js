// 登录页面组件
Vue.component('login-page', {
    props: ['isLoading', 'error'],
    data() {
        return {
            loginForm: {
                username: '',
                password: ''
            }
        };
    },
    template: `
        <div class="flex items-center justify-center min-h-screen p-4 bg-gradient-to-br from-pink-50 to-rose-50">
            <div class="w-full max-w-md bg-white/90 backdrop-blur-sm rounded-xl shadow-lg p-8 border border-pink-200">
                <div class="text-center mb-8">
                    <div class="w-16 h-16 bg-gradient-to-r from-pink-400 to-rose-400 rounded-full flex items-center justify-center mx-auto mb-4">
                        <i class="fas fa-user-graduate text-white text-2xl"></i>
                    </div>
                    <h2 class="text-2xl font-bold text-pink-800">校园签到系统</h2>
                    <p class="text-pink-500 mt-2">请使用学号和密码登录</p>
                </div>

                <form @submit.prevent="handleLogin" class="space-y-4">
                    <div>
                        <label class="block text-pink-700 text-sm font-medium mb-2">学号/用户名</label>
                        <input v-model="loginForm.username" type="text" required placeholder="请输入学号"
                               class="w-full px-4 py-3 rounded-lg border border-pink-200 focus:border-pink-400 focus:ring-2 focus:ring-pink-200 outline-none transition-colors">
                    </div>

                    <div>
                        <label class="block text-pink-700 text-sm font-medium mb-2">密码</label>
                        <input v-model="loginForm.password" type="password" required placeholder="请输入密码"
                               class="w-full px-4 py-3 rounded-lg border border-pink-200 focus:border-pink-400 focus:ring-2 focus:ring-pink-200 outline-none transition-colors">
                    </div>

                    <button type="submit" :disabled="isLoading"
                            class="w-full bg-gradient-to-r from-pink-400 to-rose-400 text-white py-3 rounded-lg font-medium hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed">
                        <span v-if="isLoading">
                            <i class="fas fa-spinner fa-spin mr-2"></i>登录中...
                        </span>
                        <span v-else>登录</span>
                    </button>

                    <div class="bg-amber-50 border border-amber-300 text-amber-700 px-4 py-3 rounded-lg text-sm">
                        <i class="fas fa-info-circle mr-2"></i>如果登录提示"登录失败,请重新尝试"，不一定是账户密码错误哦，多尝试几次试试看~
                    </div>

                    <div v-if="error" class="bg-rose-50 border border-rose-200 text-rose-600 px-4 py-3 rounded-lg text-sm">
                        <i class="fas fa-exclamation-circle mr-2"></i>{{ error }}
                    </div>
                </form>

                    <div class="text-center mt-6 pt-4 border-t border-pink-100">
                        <p class="text-xs text-pink-400">陕ICP备2026004528号-1</p>
                    </div>
                </div>
            </div>
    `,
    methods: {
        handleLogin() {
            this.$emit('login', {
                username: this.loginForm.username,
                password: this.loginForm.password
            });
        }
    }
});
