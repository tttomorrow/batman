<template>
  <el-menu-item :index="menu.menuId.toString()" @click="gotoRouteHandle(menu.menuId)">
    <svg class="icon-svg aui-sidebar__menu-icon" aria-hidden="true"><use :xlink:href="`#${menu.icon}`"></use></svg>
    <span>{{ menu.name }}</span>
  </el-menu-item>
</template>

<script>
import SubMenu from './main-sidebar-sub-menu'
export default {
  name: 'sub-menu',
  props: {
    menu: {
      type: Object,
      required: true
    }
  },
  components: {
    SubMenu
  },
  methods: {
    // 通过menuId与动态(菜单)路由进行匹配跳转至指定路由
    gotoRouteHandle (menuId) {
      var route = window.SITE_CONFIG['dynamicMenuRoutes'].filter(item => item.meta.menuId === menuId)[0]
      if (route) {
        this.$router.push({ name: route.name })
      }
    }
  }
}
</script>
