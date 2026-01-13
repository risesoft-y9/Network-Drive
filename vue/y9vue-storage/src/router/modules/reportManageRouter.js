/*
 * @Author: yihong Yh599598!@#
 * @Date: 2025-07-03 14:42:03
 * @LastEditors: yihong Yh599598!@#
 * @LastEditTime: 2025-08-01 17:20:34
 * @FilePath: \y9vue-app\y9-vue\y9vue-storage\src\router\modules\reportManageRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const escalationRouter = {
    path: '/reportManage/index',
    component: () => import('@/layouts/index.vue'),
    redirect: '/reportManage/index',
    name: 'reportManage',
    meta: {
        title: '文件上报管理',
        icon: '"ri-folder-settings-line',
        roles: ['reportManager']
    },
    children: [
        {
            path: '/reportManage/index',
            component: () => import('@/views/Report/manage.vue'),
            name: 'reportManageIndex',
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'report' : route.query.parentId,
                listType: 'reportManage',
                roleType: 'manage'
            }),
            meta: {
                title: '文件上报管理',
                icon: 'ri-folder-settings-line'
            }
        }
    ]
};

export default escalationRouter;
