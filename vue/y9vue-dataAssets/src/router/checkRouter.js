import '@/assets/css/nprogress.css'; // progress bar style
import router from '@/router';
import { checkRole } from '@/router/checkRole';
import { constantRoutes } from '@/router/index';
import { useSettingStore } from '@/store/modules/settingStore';
import y9_storage from '@/utils/storage';
import NProgress from 'nprogress'; // progress bar
import { $y9_SSO } from '@/main';
import { getUserRole } from './getInitData';

NProgress.configure({ showSpinner: false, easing: 'ease', speed: 1000 });

// 路由白名单过滤
function routerWriteList(array, path) {
    let find = false;
    for (let index = 0; index < array.length; index++) {
        const item = array[index];
        if (item.path === path) {
            return true;
        }
        if (item.children) {
            find = routerWriteList(item.children, path);
        }
    }
    if (find) {
        return true;
    } else {
        return false;
    }
}

// 路由白名单
export async function checkWriteList(to, from) {
    // 白名单
    let isWriteList = routerWriteList(constantRoutes, to.path);
    //console.log('isWriteList',isWriteList);
    if (isWriteList) {
        if (to.path === '/' || to.path === '/login') {
            // 登陆过 导航直接跳过login页面进入系统
            const isRight = await $y9_SSO.checkToken();
            if ( isRight) {
                window.location = import.meta.env.VUE_APP_HOST_INDEX;
            }
        }
        return true;
    } else {
        return false;
    }
}

let userRole = ['user'];
async function check() {
    let  isTokenValid, isRoleValid;

    // access_token 是否过期
    isTokenValid = await $y9_SSO.checkToken();
    // console.log(`isTokenValid=${isTokenValid}`);
    if (!isTokenValid) {
        return false;
    }

    // 判断角色
    let result = await getUserRole();
    if(result) {
        userRole = ['systemAdmin'];
    }

    isRoleValid = (await checkRole(userRole)) ? true : false;
    if (!isRoleValid) {
        return false;
    }

    // token在有效期且角色已获取路由
    if (isTokenValid && isRoleValid) {
        return true;
    } else {
        if (!isTokenValid || !isRoleValid) {
            await $y9_SSO.ssoLogout({
                logoutUrl: import.meta.env.VUE_APP_SSO_LOGOUT_URL + import.meta.env.VUE_APP_NAME + '/',
            });
        }
        return false;
    }
}

// 所有路由上带的参数塞到一个对象里
const parseQueryString = (string) => {
    if (string == '') {
        return false;
    }
    var segments = string.split('&').map((s) => s.split('='));
    var queryString = {};
    segments.forEach((s) => (queryString[s[0]] = s[1]));
    return queryString;
};
// 存储任意路由的参数
const cacheQuery = (query) => {
    // 维护同一个参数，任意组件内可取出使用对应的参数
    let session_query = y9_storage.getObjectItem('query');
    if (session_query) {
        for (const key in query) {
            session_query[key] = query[key];
        }
        y9_storage.setObjectItem('query', session_query);
    } else {
        y9_storage.setObjectItem('query', query);
    }
};

let flag = 0;
export const routerBeforeEach = async (to, from) => {
    const settingStore = useSettingStore();
    // 进度条
    if (settingStore.getProgress) {
        NProgress.start();
    }

    // 路由是否带参数
    let query = parseQueryString(window.location.search.substring(1));
    if (query) {
        cacheQuery(query);
    }

    //console.log(to.path, from.path);
    // 检查路由白名单
    let isWriteRoute = await checkWriteList(to, from);
    // console.log(`isWriteList = ${isWriteRoute}`);
    if (isWriteRoute) {
        return true;
    }

    let CHECK = await check();
    // console.log(`CHECK = ${CHECK}`);
    if ( CHECK) {
        //console.log(await router.getRoutes(),router,from,to, router.hasRoute(to.name));
        if (!to.name) {
            let array = await router.getRoutes();
            // console.log(to, array)
            array.forEach((item) => {
                if (item.path === to.path && item.name) {
                    // console.log("=====", router.hasRoute(item.name));
                    router.push({ name: item.name });
                }
            });
        } else {
            return true;
        }
    }else{
        await $y9_SSO.checkLogin();
    }

    return false;
};
