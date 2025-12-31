const examineRouter = {
    path: '/examine',
    component: () => import('@/layouts/index.vue'),
    name:'examineIndex',
    redirect: '/examine1',
    meta: {
        title: '审核管理',
        icon: 'ri-function-line',
        roles: ['systemAdmin']
    },
    children:[
        {
            path: '/examine1',
            component: () => import('@/views/pretreat/examine.vue'),
            name: 'examine1',
            meta: {
                title: '资产审核',
                icon: 'ri-archive-line',
            },
        },
        {
            path: '/examine2',
            component: () => import('@/views/subscribe/examine.vue'),
            name: 'examine2',
            meta: {
                title: '订阅审核',
                icon: 'ri-archive-2-line',
            },
        },
    ]
};
export default examineRouter;
