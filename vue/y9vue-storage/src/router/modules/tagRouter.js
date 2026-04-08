/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2025-12-02 17:26:08
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-04-03 10:33:36
 * @FilePath: \y9-vue\y9vue-storage\src\router\modules\tagRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE   
 */
const tagRouter = {
    path: '/tag',
    component: () => import('@/layouts/index.vue'),
    redirect: '/tag/manage',
    name: 'tag',
    meta: {
        title: '标签字典管理',
        roles: ['tagManager', 'user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/tag/manage',
            component: () => import('@/views/Tag/index.vue'),
            name: 'tagIndex',
            meta: {
                title: '标签字典管理',
                icon: 'ri-price-tag-3-line'
            }
        }
    ]
};

export default tagRouter;
