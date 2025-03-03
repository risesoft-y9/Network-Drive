/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-07 10:44:56
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\collectRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const dictionaryRouter = {
    path: '/dictionary',
    component: () => import('@/layouts/index.vue'),
    name:'dictionary',
    redirect: '/category',
    meta: {
        title: '资产字典',
        icon: 'ri-book-marked-line',
    },
    children:[
        {
            path: '/category',
            component: () => import('@/views/dictionary/category/index.vue'),
            name: 'categoryIndex',
            meta: {
                title: '目录管理',
                icon: 'ri-archive-stack-line',
            },
        },
        {
            path: '/metadata',
            component: () => import('@/views/dictionary/metadata/manage.vue'),
            name: 'metadata',
            meta: {
                title: '元数据管理',
                icon: 'ri-database-2-line',
            },
        },
        {
            path: '/label',
            component: () => import('@/views/dictionary/label/index.vue'),
            name: 'label',
            meta: {
                title: '标签管理',
                icon: 'ri-price-tag-3-line',
            },
        },
        {
            path: '/dataDictionary',
            component: () => import('@/views/dictionary/dataDict/index.vue'),
            name: 'dataDictIndex',
            meta: {
                title: '数据字典管理',
                icon: 'ri-book-2-line',
            },
        },
    ]
};
export default dictionaryRouter;
