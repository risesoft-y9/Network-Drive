/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-09 10:05:19
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-06 17:17:44
 * @FilePath: \vue\y9vue-dataAssets\src\router\modules\collectRouter.js
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
const manageRouter = {
        path: '/pretreat',
        component: () => import('@/layouts/index.vue'),
        name:'pretreatIndex',
        redirect: '/pretreat/register',
        meta: {
            title: '资产预处理',
            icon: 'ri-stack-line',
            roles: ['user','archivesManager'],
        },
        children:[
            {
                path: '/pretreat/register',
                component: () => import('@/views/pretreat/index3.vue'),
                name: 'register',
                meta: {
                    title: '资产注册',
                    icon: 'ri-registered-line',
                },
            },
            {
                path: '/pretreat/pretreat',
                component: () => import('@/views/pretreat/index3.vue'),
                name: 'pretreat',
                meta: {
                    title: '资产挂接',
                    icon: 'ri-links-fill',
                },
            },
        ]
    };
export default manageRouter;
