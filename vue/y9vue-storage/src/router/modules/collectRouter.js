const escalationRouter = 
    {
        path: '/collect/index',
        component: () => import('@/layouts/index.vue'),
        redirect: '/collect/index',
        name:'collect',
        meta: {
            title: '我的收藏',
            icon: 'ri-star-line',
            roles: ['user','reportManager','capacityManager','publicManager'],
        },
        children:[
            {
                path: '/collect/index',
                component: () => import('@/views/Collect/index.vue'),
                name: 'collectIndex',
                props: route => ({parentId: route.query.parentId==undefined?'':route.query.parentId,listType:route.query.listType}),
                meta: {
                    title: '我的收藏',
                    icon: 'ri-star-line',
                },
            },
        ]
    };

export default escalationRouter;
