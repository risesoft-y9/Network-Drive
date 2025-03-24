const interfaceRouter = {
    path: '/interface',
    component: () => import('@/layouts/index.vue'),
    redirect: '/interface',
    name: 'interfaceIndex',
    meta: {
        title: '接口注册',
    },
    children: [
        {
            path: '/interface',
            component: () => import('@/views/test/index.vue'),
            name: 'interface',
            meta: {
                title: '接口注册',
                icon: 'ri-links-fill',
            }
        }
    ]
};

export default interfaceRouter;
