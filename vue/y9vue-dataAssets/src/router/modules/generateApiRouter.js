const generateApiRouter = {
    path: '/generate',
    component: () => import('@/layouts/index.vue'),
    name:'generateIndex',
    redirect: '/api',
    meta: {
        title: 'API服务',
        icon: 'ri-terminal-box-line',
        //roles: ['systemAdmin']
    },
    children:[
        {
            path: '/dataApiTable',
            component: () => import('@/views/dataApiTable/index.vue'),
            name: 'dataApiTable',
            meta: {
                title: '接口资源申请',
                icon: 'ri-git-pull-request-line',
            },
        },
        {
            path: '/api',
            component: () => import('@/views/api/index.vue'),
            name: 'api',
            meta: {
                title: 'API管理',
                icon: 'ri-device-line',
                roles: ['systemAdmin']
            },
        },
        {
            path: '/authority',
            component: () => import('@/views/api/role/index.vue'),
            name: 'authority',
            meta: {
                title: 'API权限',
                icon: 'ri-bring-to-front',
                roles: ['systemAdmin']
            },
        },
        {
            path: '/monitor',
            component: () => import('@/views/api/log/index.vue'),
            name: 'monitor',
            meta: {
                title: 'API监控',
                icon: 'ri-bar-chart-box-line',
                roles: ['systemAdmin']
            },
        }
    ]
};
export default generateApiRouter;
