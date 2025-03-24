const manageRouter = {
    path: '/register',
    component: () => import('@/layouts/index.vue'),
    name:'pretreatIndex',
    redirect: '/register',
    meta: {
        title: '资产登记',
        icon: 'ri-stack-line',
    },
    children:[
        {
            path: '/register',
            component: () => import('@/views/pretreat/index.vue'),
            name: 'register',
            meta: {
                title: '资产登记',
                icon: 'ri-stack-line',
            },
        },
        // {
        //     path: '/mount',
        //     component: () => import('@/views/pretreat/mount.vue'),
        //     name: 'mount',
        //     meta: {
        //         title: '资产挂接',
        //         icon: 'ri-links-fill',
        //     },
        // },
    ]
};
export default manageRouter;
