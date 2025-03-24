/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-09 11:08:01
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\libraryRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const libraryRouter = {
    path: '/library',
    component: () => import('@/layouts/index.vue'),
    name:'libraryIndex',
    redirect: '/managed',
    meta: {
        title: '资产库',
        icon: 'ri-archive-line',
    },
    children:[
        {
            path: '/managed',
            component: () => import('@/views/library/index.vue'),
            name: 'managed',
            meta: {
                title: '资产台账',
                icon: 'ri-device-line',
            },
        },
        {
            path: '/coding',
            component: () => import('@/views/qrcode/index.vue'),
            name: 'coding',
            meta: {
                title: '资产赋码',
                icon: 'ri-qr-code-line',
            },
        },
        {
            path: '/labelled',
            component: () => import('@/views/library/mark/index.vue'),
            name: 'labelled',
            meta: {
                title: '资产标注',
                icon: 'ri-price-tag-2-line',
            },
        },
        {
            path: '/updownshelf',
            component: () => import('@/views/library/updown.vue'),
            name: 'updownshelf',
            meta: {
                title: '资产上下架',
                icon: 'ri-arrow-up-down-line',
            },
        },
    ]
};
export default libraryRouter;
