const sourceRouter = {
    path: '/source',
    component: () => import('@/layouts/index.vue'),
    name:'source',
    redirect: '/datasource',
    meta: {
        title: '数据源管理',
        icon: 'ri-database-2-line',
    },
    children:[
        {
            path: '/datasource',
            component: () => import('@/views/datasource/index.vue'),
            name: 'datasource',
            meta: {
                title: '数据源',
                icon: 'ri-database-line',
            },
        },
        {
            path: '/interface',
            component: () => import('@/views/test/index.vue'),
            name: 'interface',
            meta: {
                title: '数据对象',
                icon: 'ri-table-line',
            },
        },
    ]
};
export default sourceRouter;
