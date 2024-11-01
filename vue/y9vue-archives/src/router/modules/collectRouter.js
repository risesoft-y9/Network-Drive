/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-01 15:58:42
 * @FilePath: \vue\y9vue-archives\src\router\modules\collectRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const collectRouter = {
        path: '/collect',
        component: () => import('@/layouts/index.vue'),
        name:'collect',
        redirect: '/collect/automatic',
        meta: {
            title: '档案采集',
            icon: 'ri-edit-box-line',
            roles: ['user','archivesManager'],
        },
        children:[
            {
                path: '/collect/automatic',
                component: () => import('@/views/collect/index3.vue'),
                name: 'automatic',
                meta: {
                    title: '自动采集',
                    icon: 'ri-repeat-fill',
                },
            },
            {
                path: '/collect/offlineReception',
                component: () => import('@/views/collect/index3.vue'),
                name: 'offlineReception',
                meta: {
                    title: '离线接收',
                    icon: 'ri-cloud-off-line',
                },
            },
            {
                path: '/collect/cataloging',
                component: () => import('@/views/collect/record/index.vue'),
                name: 'cataloging',
                meta: {
                    title: '档案著录',
                    icon: 'ri-book-read-line',
                },
            },
            {
                path: '/collect/preAarchiving',
                component: () => import('@/views/collect/index3.vue'),
                name: 'preAarchiving',
                meta: {
                    title: '预归档库',
                    icon: 'ri-clockwise-line',
                },
            },
            {
                path: '/collect/testing',
                component: () => import('@/views/collect/index3.vue'),
                name: 'testing',
                meta: {
                    title: '档案检测',
                    icon: 'ri-menu-search-line',
                },
            },
            {
                path: '/collect/recycleBin',
                component: () => import('@/views/collect/index3.vue'),
                name: 'recycleBin',
                meta: {
                    title: '回收站',
                    icon: 'ri-recycle-line',
                },
            },
        ]
    };
export default collectRouter;
