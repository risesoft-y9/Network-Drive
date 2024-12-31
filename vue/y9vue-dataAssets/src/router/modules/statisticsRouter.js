/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-10-11 15:54:08
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\collectRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const statisticsRouter = {
        path: '/statistics',
        component: () => import('@/layouts/index.vue'),
        name:'statistics',
        redirect: '/statistics/annualReport',
        meta: {
            title: '档案统计',
            icon: 'ri-line-chart-line',
            roles: ['user','archivesManager'],
        },
        children:[
            {
                path: '/statistics/annualReport',
                component: () => import('@/views/statistics/index.vue'),
                name: 'annualReport',
                meta: {
                    title: '档案综合年报',
                    icon: 'ri-table-line',
                },
            },
            {
                path: '/statistics/collection',
                component: () => import('@/views/statistics/index.vue'),
                name: 'collection',
                meta: {
                    title: '档案采集统计',
                    icon: 'ri-bar-chart-2-line',
                },
            },
            {
                path: '/statistics/manageTotal',
                component: () => import('@/views/statistics/index.vue'),
                name: 'manageTotalIndex',
                meta: {
                    title: '档案管理统计',
                    icon: 'ri-bar-chart-box-line',
                },
            },
            {
                path: '/statistics/utilize',
                component: () => import('@/views/statistics/index.vue'),
                name: 'utilizeIndex',
                meta: {
                    title: '档案利用统计',
                    icon: 'ri-pie-chart-line',
                },
            },
        ]
    };
export default statisticsRouter;
