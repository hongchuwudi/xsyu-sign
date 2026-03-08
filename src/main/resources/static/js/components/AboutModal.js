// 关于软件模态框组件
Vue.component('about-modal', {
    props: ['visible'],
    template: `
        <div :class="{'active': visible}" class="modal-overlay" @click.self="close">
            <div class="modal-box w-full max-w-2xl mx-4">
                <div class="bg-gradient-to-r from-pink-400 to-rose-400 text-white p-6 rounded-t-lg">
                    <div class="flex justify-between items-center">
                        <h3 class="text-xl font-bold">关于软件</h3>
                        <button @click="close" class="text-white hover:text-pink-200 text-xl">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>

                <div class="p-6 max-h-[70vh] overflow-y-auto">
                    <!-- 软件信息 -->
                    <div class="text-center mb-8">
                        <div class="w-20 h-20 bg-gradient-to-r from-pink-400 to-rose-400 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i class="fas fa-calendar-check text-white text-3xl"></i>
                        </div>
                        <h2 class="text-2xl font-bold text-pink-800">校园签到系统</h2>
                        <p class="text-pink-500 mt-2">版本 1.0.0</p>
                    </div>

                    <!-- 使用说明 -->
                    <div class="bg-pink-50 rounded-lg p-6 border border-pink-100 mb-6">
                        <h4 class="font-bold text-pink-800 mb-3 text-lg">使用说明</h4>
                        <div class="space-y-2 text-pink-700">
                            <p class="flex items-start">
                                <i class="fas fa-check text-rose-400 mr-2 mt-1 flex-shrink-0"></i>
                                <span>本系统为西安石油大学校园签到辅助工具</span>
                            </p>
                            <p class="flex items-start">
                                <i class="fas fa-check text-rose-400 mr-2 mt-1 flex-shrink-0"></i>
                                <span>支持自动签到和手动签到两种模式</span>
                            </p>
                            <p class="flex items-start">
                                <i class="fas fa-check text-rose-400 mr-2 mt-1 flex-shrink-0"></i>
                                <span>请遵守学校规定，诚信使用签到系统</span>
                            </p>
                        </div>
                    </div>

                    <!-- 作者信息 -->
                    <div class="text-center">
                        <p class="text-sm text-pink-500">
                            <i class="far fa-copyright mr-1"></i> --@hongchu@--
                        </p>
                        <p class="text-xs text-pink-400 mt-2">
                            陕ICP备2026004528号-1
                        </p>
                    </div>
                </div>

                <div class="border-t border-pink-100 p-6">
                    <div class="flex justify-center">
                        <button @click="close"
                                class="px-6 py-2 bg-pink-400 text-white rounded-lg font-medium hover:bg-pink-500 transition-colors">
                            关闭
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `,
    methods: {
        close() {
            this.$emit('close');
        }
    }
});
