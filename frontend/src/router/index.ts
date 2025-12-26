import { createRouter, createWebHashHistory } from 'vue-router'
import PlcInfoPage from '../pages/PlcInfoPage.vue'
import PlcAddrPage from '../pages/PlcAddrPage.vue'
import ComboDebugPage from '../pages/ComboDebugPage.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/combo-debug' },
    { path: '/plc-info', component: PlcInfoPage },
    { path: '/plc-addr', component: PlcAddrPage },
    { path: '/combo-debug', component: ComboDebugPage }
  ]
})

export default router
