const homeRouter = {
    path: '/index',
    component: () => import('@/layouts/index.vue'),
    redirect: '/homeIndex',
    name: 'home',
    meta: {
        title: '首页',
        roles: ['systemAdmin', 'systemManager']
    },
    children: [
        {
            path: '/homeIndex',
            component: () => import('@/views/home/index3.vue'),
            name: 'homeIndex',
            meta: {
                title: '首页',
                icon: 'ri-home-3-line',
                roles: ['systemAdmin', 'systemManager']
            }
        }
    ]
};

export default homeRouter;
