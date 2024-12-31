/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-10-11 15:51:53
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\collectRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const utilizeRouter = {
        path: '/utilize',
        component: () => import('@/layouts/index.vue'),
        name:'utilize',
        redirect: '/utilize/fullText',
        meta: {
            title: '档案利用',
            icon: 'ri-folder-user-line',
            roles: ['user','archivesManager'],
        },
        children:[
            {
                path: '/utilize/fullText',
                component: () => import('@/views/utilize/index.vue'),
                name: 'fullText',
                meta: {
                    title: '全文检索',
                    icon: 'ri-search-line',
                },
            },
            {
                path: '/utilize/condition',
                component: () => import('@/views/utilize/index.vue'),
                name: 'condition',
                meta: {
                    title: '条件检索',
                    icon: 'ri-file-search-line',
                },
            },
            {
                path: '/utilize/borrowingManage',
                component: () => import('@/views/utilize/index.vue'),
                name: 'borrowingManage',
                meta: {
                    title: '借阅管理',
                    icon: 'ri-folder-settings-line',
                },
            },
            {
                path: '/utilize/borrowingCar',
                component: () => import('@/views/utilize/index.vue'),
                name: 'borrowingCar',
                meta: {
                    title: '借阅车',
                    icon: 'ri-shopping-cart-2-line',
                },
            },
            {
                path: '/utilize/myBorrowing',
                component: () => import('@/views/utilize/index.vue'),
                name: 'myBorrowing',
                meta: {
                    title: '我的借阅',
                    icon: 'ri-file-user-line',
                },
            },
            {
                path: '/utilize/compilation',
                component: () => import('@/views/utilize/index.vue'),
                name: 'compilation',
                meta: {
                    title: '档案编研',
                    icon: 'ri-git-repository-line',
                },
            },
        ]
    };
export default utilizeRouter;
