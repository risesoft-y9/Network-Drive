/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2025-07-03 14:42:03
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-12-02 17:43:44
 * @FilePath: \y9-vue\y9vue-storage\src\router\modules\capacityRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const capacityRouter = {
    path: '/capacity',
    component: () => import('@/layouts/index.vue'),
    redirect: '/capacity/manage',
    name: 'capacity',
    meta: {
        title: '存储空间管理',
        roles: ['capacityManager', 'user']
    },
    children: [
        {
            path: '/capacity/manage',
            component: () => import('@/views/Capacity/index.vue'),
            name: 'capacityIndex',
            meta: {
                title: '存储空间管理',
                icon: 'ri-sd-card-mini-line'
            }
        }
    ]
};

export default capacityRouter;
