/*
 * @Author: yihong Yh599598!@#
 * @Date: 2025-07-03 14:42:03
 * @LastEditors: yihong Yh599598!@#
 * @LastEditTime: 2025-08-01 17:20:46
 * @FilePath: \y9vue-app\y9-vue\y9vue-storage\src\router\modules\managerRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const managerRouter = [
    {
        path: '/manage/index',
        component: () => import('@/layouts/index.vue'),
        redirect: '/manage/index',
        name: 'manage',
        meta: {
            title: '公共文件管理',
            icon: 'ri-folder-settings-line',
            roles: ['publicManager']
        },
        children: [
            {
                path: '/manage/index',
                component: () => import('@/views/Public/index.vue'),
                name: 'manageIndex',
                props: (route) => ({
                    parentId: route.query.parentId == undefined ? 'public' : route.query.parentId,
                    roleType: 'manage'
                }),
                meta: {
                    title: '公共文件管理',
                    icon: 'ri-folder-settings-line'
                }
            },
            {
                path: '/manage/search',
                component: () => import('@/views/Public/index.vue'),
                hidden: true,
                props: (route) => ({
                    parentId: route.query.parentId == undefined ? 'public' : route.query.parentId,
                    fileNodeType: route.query.fileNodeType,
                    searchName: route.query.searchName,
                    roleType: 'manage',
                    startDate: route.query.startDate,
                    endDate: route.query.endDate
                }),
                name: 'manage_search',
                meta: {
                    title: '查询'
                }
            }
        ]
    }
];

export default managerRouter;
