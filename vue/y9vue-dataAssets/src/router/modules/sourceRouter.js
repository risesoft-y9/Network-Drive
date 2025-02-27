/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2025-02-25 11:12:52
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-02-25 15:03:05
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\sourceRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const sourceRouter = {
    path: '/source',
    component: () => import('@/layouts/index.vue'),
    name:'source',
    redirect: '/datasource',
    meta: {
        title: '数据源管理',
        icon: 'ri-database-2-line',
    },
    children:[
        {
            path: '/datasource',
            component: () => import('@/views/datasource/index.vue'),
            name: 'datasource',
            meta: {
                title: '数据源',
                icon: 'ri-database-line',
            },
        },
        {
            path: '/dataobject',
            component: () => import('@/views/dataobject/index.vue'),
            name: 'dataobject',
            meta: {
                title: '数据对象',
                icon: 'ri-table-line',
            },
        },
    ]
};
export default sourceRouter;
