<script lang="ts" setup>
import RightTopPosition from '../components/RightTopPosition.vue';
import {useSettingStore} from '@/store/modules/settingStore';
import {$y9_SSO} from '@/main';
// 个人信息 —— 头像
const userInfo = JSON.parse(sessionStorage.getItem('ssoUserInfo'));

// 菜单默认收缩属性
const props = defineProps({
  menuCollapsed: {
    type: Boolean,
    default: false,
  },
});
// 注入 字体变量
const fontSizeObj: any = inject('sizeObjInfo');
// 全屏功能
const {isFullscreen, toggle} = useFullscreen();
const toggleFullScreen = toggle;

// 菜单收缩功能
const settingStore = useSettingStore();
const toggleCollapsedFunc = () => {
  settingStore.$patch({
    menuCollapsed: !settingStore.getMenuCollapsed,
  });
};

// 白天黑夜功能
const isDark = useDark({
  selector: 'html',
  valueDark: 'theme-dark',
  valueLight: '',
});
const toggleDark = useToggle(isDark);

// 锁屏
const lockScreenFunc = () => {
  settingStore.$patch({
    lockScreen: true,
  });
};

const backHomeMethod = () => {
  window.location.href = import.meta.env.VUE_APP_HOST_HOMEINDEX;
};

// 搜索
const searchFunc = () => {
};
// 刷新
const emits = defineEmits(['refresh']);
const refreshFunc = () => {
  // window.location = window.location.href
  emits('refresh');
};
const logout = () => {
  try {
    // const params = {
    //   to: {path: window.location.pathname},
    //   logoutUrl: import.meta.env.VUE_APP_SSO_LOGOUT_URL + import.meta.env.VUE_APP_NAME + '/',
    //   __y9delete__: () => {
    //     // 删除前执行的函数
    //     console.log('删除前执行的函数');
    //   },
    // };
    $y9_SSO.ssoLogout({});
  } catch (error) {
    ElMessage.error(error.message || 'Has Error');
  }
};
</script>

<template>
  <div id="right-top">
    <div class="left">
      <div class="indexlayout-flexible" @click="toggleCollapsedFunc">
        <i v-if="menuCollapsed" class="ri-menu-unfold-line"></i>
        <i v-else class="ri-menu-fold-line"></i>
      </div>
    </div>
    <div class="right">
      <div v-show="settingStore.getLock" class="item" @click="lockScreenFunc">
        <i class="ri-lock-2-line"></i>
        <span>{{ $t('锁屏') }}</span>
      </div>
      <!-- <div class="item search" @click="searchFunc" v-show="settingStore.getSearch">
          <i class="ri-search-line"></i>
          <span>搜索</span>
      </div> -->
      <div v-show="settingStore.getRefresh" class="item" @click="refreshFunc">
        <i class="ri-refresh-line"></i>
        <span>{{ $t('刷新') }}</span>
      </div>
      <div v-show="settingStore.getFullScreeen" class="item" @click="toggleFullScreen">
        <i class="ri-fullscreen-line"></i>
        <span>{{ $t('全屏') }}</span>
      </div>
      <div class="item web-setting">
        <i class="ri-edit-box-line"></i>
        <span>{{ $t('设置') }}</span>
      </div>
      <!-- <div class="item notify" v-show="settingStore.getNotify">
          <el-badge :value="3" class="badge"></el-badge>
          <i class="ri-notification-line"></i>
      </div> -->
      <!-- <div class="item isDark">
          <i class="ri-moon-line" @click="toggleDark" v-if="!isDark"></i>
          <i class="ri-sun-line" @click="toggleDark" v-else></i>
      </div> -->
      <!-- <div class="item user">
          <RightTopPosition />
      </div> -->
      <RightTopPosition/>
      <div class="item user">
        <!-- 头像测试链接地址：https://www.youshengyun.com/fileManager/files/e6b5d41fd2bd4cdda538139f9b7848c7.jpg -->
        <el-avatar :src="userInfo.avator ? userInfo.avator : ''"> {{ userInfo.loginName }}</el-avatar>
      </div>
      <!--            <div class="item" @click="backHomeMethod">-->
      <!--                <i class="ri-arrow-go-back-line"></i>-->
      <!--                <span>{{ $t('首页') }}</span>-->
      <!--            </div>-->
      <div class="item" @click="logout">
        <i class="ri-logout-box-r-line"></i>
        <span>{{ $t('退出') }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import '@/theme/global-vars.scss';

#right-top {
  background-color: var(--el-bg-color);
  color: var(--el-text-color-primary);
  display: flex;
  justify-content: space-between;
  width: 100%;
  //暂时不变，等dark版本追加
  border-bottom: 1px solid var(--el-border-color-base);
  box-shadow: 2px 2px 2px 1px #0000000f;
  z-index: 1;

  .left,
  .right {
    display: flex;
    height: $headerHeight;
    line-height: $headerHeight;
    font-size: v-bind('fontSizeObj.extraLargeFont');
  }

  .indexlayout-flexible {
    width: $headerHeight;
    height: $headerHeight;
    line-height: $headerHeight;
    text-align: center;
    cursor: pointer;

    &:hover {
      background-color: var(--bg-color);
      color: var(--el-text-color-primary);
    }
  }

  .right {
    padding-right: 15px;

    & > .item {
      overflow: hidden;
      padding: 0 11px;
      min-width: 5px;
      color: var(--el-menu-text-color);
      cursor: pointer;
      display: flex;
      align-items: center;

      i {
        position: relative;
        // top: 4px;
      }

      span {
        font-size: v-bind('fontSizeObj.baseFontSize');
        margin-left: 5px;
      }

      &:hover {
        border-bottom: 2px solid var(--el-border-color-light);
        color: var(--el-color-primary);
      }

      &.notify {
        .badge {
          position: absolute;
          z-index: 1;

          & > .el-badge__content--danger {
            background-color: var(--el-color-danger);
          }
        }
      }

      &.user {
        //min-width: 202px;
        display: flex;
        align-items: center;

        & > img {
          height: $headerHeight - 18px;
          width: $headerHeight - 18px;
          border-radius: 50%;
        }

        & > .name {
          font-size: v-bind('fontSizeObj.baseFontSize');
          display: flex;
          flex-direction: column;
          justify-content: end;

          span {
            line-height: v-bind('fontSizeObj.lineHeight');
            text-align: end;
          }
        }

        i {
          color: var(--el-color-primary);
          font-size: 48px;
          margin-left: 8px;
          margin-bottom: 8px;
        }

        .el-avatar {
          background-color: var(--el-color-primary);
        }
      }

      &:hover {
        cursor: pointer;
        border-bottom: none; // 鼠标划过或点击时不显示下划线
      }
    }
  }
}
</style>
