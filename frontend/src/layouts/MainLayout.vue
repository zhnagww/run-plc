<template>
  <el-config-provider>
    <el-container class="app-shell">
      <el-aside width="220px" class="app-aside">
        <div class="brand">
          <div class="brand__title">PLC管理系统</div>
          <div class="brand__sub">RUN-PLC</div>
        </div>
        <el-menu
          :default-active="active"
          class="app-menu"
          router
        >
          <el-menu-item index="/combo-debug">
            <span>组合调试</span>
          </el-menu-item>
          <el-menu-item index="/plc-addr">
            <span>PLC点位管理</span>
          </el-menu-item>
          <el-menu-item index="/plc-info">
            <span>PLC信息管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="app-header">
          <div class="app-header__left">
            <div class="page-title">{{ pageTitle }}</div>
          </div>
          <div class="app-header__right">
            <el-tag type="info" effect="dark">接口保持不变</el-tag>
          </div>
        </el-header>

        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const active = computed(() => route.path)

const pageTitle = computed(() => {
  switch (route.path) {
    case '/plc-info':
      return 'PLC信息管理'
    case '/plc-addr':
      return 'PLC点位管理'
    case '/combo-debug':
      return '组合调试'
    default:
      return 'PLC管理系统'
  }
})
</script>
