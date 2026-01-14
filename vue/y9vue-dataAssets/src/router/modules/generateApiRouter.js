const generateApiRouter = {
    path: '/generate',
    component: () => import('@/layouts/index.vue'),
    name:'generateIndex',
    redirect: '/api',
    meta: {
        title: '接口工具',
        icon: 'ri-terminal-box-line',
        roles: ['systemAdmin']
    },
    children:[
        {
            path: '/api',
            component: () => import('@/views/api/index.vue'),
            name: 'api',
            meta: {
                title: '接口管理',
                icon: 'ri-device-line',
                roles: ['systemAdmin']
            },
        },
        {
            path: '/authority',
            component: () => import('@/views/api/role/index.vue'),
            name: 'authority',
            meta: {
                title: '接口权限',
                icon: 'ri-bring-to-front',
                roles: ['systemAdmin']
            },
        },
        {
            path: '/monitor',
            component: () => import('@/views/api/log/index.vue'),
            name: 'monitor',
            meta: {
                title: '接口监控',
                icon: 'ri-bar-chart-box-line',
                roles: ['systemAdmin']
            },
        }
    ]
};
export default generateApiRouter;
