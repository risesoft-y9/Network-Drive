const generateApiRouter = {
    path: '/generate',
    component: () => import('@/layouts/index.vue'),
    name:'generateIndex',
    redirect: '/api',
    meta: {
        title: 'API服务',
        icon: 'ri-terminal-box-line',
        roles: ['systemAdmin']
    },
    children:[
        {
            path: '/api',
            component: () => import('@/views/api/index.vue'),
            name: 'api',
            meta: {
                title: 'API管理',
                icon: 'ri-device-line',
            },
        },
        {
            path: '/authority',
            component: () => import('@/views/api/role/index.vue'),
            name: 'authority',
            meta: {
                title: 'API权限',
                icon: 'ri-bring-to-front',
            },
        },
        {
            path: '/monitor',
            component: () => import('@/views/api/log/index.vue'),
            name: 'monitor',
            meta: {
                title: 'API监控',
                icon: 'ri-bar-chart-box-line',
            },
        }
    ]
};
export default generateApiRouter;
