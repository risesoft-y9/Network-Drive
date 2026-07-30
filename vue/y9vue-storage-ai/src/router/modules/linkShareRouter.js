/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2026-07-14 10:01:55
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-07-15 14:05:38
 * @FilePath: \y9-vue\y9vue-storage\src\router\modules\linkShareRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const shareRouter = {
    path: '/linkShare',
    component: () => import('@/layouts/index.vue'),
    redirect: '/linkShare/index',
    name: 'linkShareIndex',
    meta: {
        title: '直链分享',
        icon: 'ri-links-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/linkShare/index',
            component: () => import('@/views/linkShare/index.vue'),
            name: 'linkShareIndex',
            props: (route) => ({
            }),
            meta: {
                title: '直链分享',
                icon: 'ri-links-line'
            }
        }
    ]
};

export default shareRouter;
