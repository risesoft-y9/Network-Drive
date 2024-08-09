const myShareRouter = 
    {
        path: '/myShare',
        component: () => import('@/layouts/index.vue'),
        redirect: '/myShare/index',
        name:'myShare',
        meta: {
            title: '共享记录',
            icon: 'ri-share-line',
            roles: ['user','reportManager','capacityManager','publicManager'],
        },
        children:[
            {
                path: '/myShare/index',
                component: () => import('@/views/MyShare/index.vue'),
                name: 'mySharedIndex',
                meta: {
                    title: '共享记录',
                    icon: 'ri-share-line',
                },
            },
        ]
    };

export default myShareRouter;
