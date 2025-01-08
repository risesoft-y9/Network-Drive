/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-07 10:10:44
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\libraryRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const libraryRouter = {
        path: '/library',
        component: () => import('@/layouts/index.vue'),
        name:'libraryIndex',
        redirect: '/library/managed',
        meta: {
            title: '资产库',
            icon: 'ri-archive-line',
            roles: ['user','archivesManager'],
        },
        children:[
            {
                path: '/library/managed',
                component: () => import('@/views/common/index.vue'),
                name: 'library_manageIndex',
                meta: {
                    title: '资产管理',
                    icon: 'ri-folder-user-line',
                },
            },
            {
                path: '/library/coding',
                component: () => import('@/views/library/index3.vue'),
                name: 'coding',
                meta: {
                    title: '资产赋码',
                    icon: 'ri-qr-code-line',
                },
            },
            {
                path: '/library/labelled',
                component: () => import('@/views/library/index.vue'),
                name: 'labelled',
                props: route => ({menuType: 'labelled'}),
                meta: {
                    title: '资产标注',
                    icon: 'ri-price-tag-2-line',
                },
            },
            {
                path: '/library/upDownShelf',
                component: () => import('@/views/library/index.vue'),
                name: 'upDownShelf',
                props: route => ({menuType: 'upDownShelf'}),
                meta: {
                    title: '资产上下架',
                    icon: 'ri-arrow-up-down-line',
                },
            },
        ]
    };
export default libraryRouter;
