/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2025-07-03 14:42:03
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-07-02 18:13:09
 * @FilePath: \y9-vue\y9vue-storage\src\router\modules\reportRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const reportRouter = {
    path: '/report/index',
    component: () => import('@/layouts/index.vue'),
    redirect: '/report/index',
    name: 'report',
    meta: {
        title: '文件上报',
        icon: 'ri-folder-upload-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/report/index',
            component: () => import('@/views/Report/index.vue'),
            name: 'reportIndex',
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'report' : route.query.parentId,
                roleType: 'common'
            }),
            meta: {
                title: '文件上报',
                icon: 'ri-folder-upload-line'
            }
        }
    ]
};

export default reportRouter;
