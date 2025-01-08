/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-01 17:47:37
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-06 16:54:18
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\homeRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */

const homeRouter = {
    path: '/index',
    component: () => import('@/layouts/index.vue'),
    redirect: '/homeIndex',
    name: 'home',
    meta: {
        title: '资产门户',
        roles: ['user','archivesManager']
    },
    children: [
        {
            path: '/homeIndex',
            component: () => import('@/views/home/index3.vue'),
            name: 'homeIndex',
            meta: {
                title: '资产门户',
                icon: 'ri-home-3-line',
                roles: ['user','archivesManager']
            }
        }
    ]
};

export default homeRouter;
