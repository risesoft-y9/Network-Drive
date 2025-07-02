/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-01 17:47:36
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-06 17:47:18
 * @FilePath: \vue\y9vue-dataAssets\src\router\index.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import { routerBeforeEach } from '@/router/checkRouter';
import NProgress from 'nprogress';
import { createRouter, createWebHistory } from 'vue-router';
import { RouteRecordRaw } from 'vue-router';
import interfaceRouter from './modules/interfaceRouter';
import libraryRouter from './modules/libraryRouter';
import examineRouter from './modules/examineRouter';
import dictionaryRouter from './modules/dictionaryRouter';
import pretreatmentRouter from './modules/pretreatmentRouter';
import sourceRouter from './modules/sourceRouter';
import generateApiRouter from './modules/generateApiRouter';

//constantRoutes为不需要动态判断权限的路由，如登录、404、500等
export const constantRoutes: Array<any> = [
    {
        path: '/',
        name: 'index',
        hidden: true,
        redirect: '/register',
    },
    {
        path: '/401',
        hidden: true,
        meta: {
            title: 'Not Permission'
        },
        component: () => import('@/views/401/index.vue'),
    },
    {
        path: '/404',
        hidden: true,
        meta: {
            title: 'Not Found'
        },
        component: () => import('@/views/404/index.vue'),
    },
    {
        path: '/goto',
        hidden: true,
        component: () => import('@/views/qrcode/goto/index.vue'),
        meta: {
            title: '二维码信息'
        }
    }
];
// 懒加载
const lazy = (path) => {
    // return import.meta.glob(`./${path}`)
    return () => import(`@/views/${path}.vue`);
}
let routes: RouteRecordRaw[] = []
//asyncRoutes需求动态判断权限并动态添加的页面  这里的路由模块顺序也是菜单显示的顺序（位置：src->router->modules）
export const asyncRoutes = [
    pretreatmentRouter,
    interfaceRouter,
    sourceRouter,
    examineRouter,
    libraryRouter,
    dictionaryRouter,
    generateApiRouter
    //...routes
    // 引入其他模块路由

];

//创建路由模式，采用history模式没有“#”
const router = createRouter({
    history: createWebHistory(import.meta.env.VUE_APP_PUBLIC_PATH),
    routes: constantRoutes,
});

//在用户点击前，进入routerBeforeEach去判断用户是否有权限
//全部判断逻辑请查看checkRouter.js
router.beforeEach(routerBeforeEach);
router.afterEach(() => {
    NProgress.done();
});
export default router;
