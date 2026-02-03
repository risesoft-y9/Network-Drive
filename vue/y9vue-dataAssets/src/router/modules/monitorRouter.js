const monitorRouter = {
    path: '/datamonitor',
    component: () => import('@/layouts/index.vue'),
    name:'monitorIndex',
    redirect: '/datamonitor',
    meta: {
        title: '数据监控',
        icon: 'ri-bar-chart-box-line',
    },
    children:[
        {
            path: '/datamonitor',
            component: () => import('@/views/monitor/index.vue'),
            name: 'datamonitor',
            meta: {
                title: '数据监控',
                icon: 'ri-bar-chart-box-line',
            },
        }
    ]
};
export default monitorRouter;