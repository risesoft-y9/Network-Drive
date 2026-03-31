const homeRouter = {
    path: '/home',
    component: () => import('@/layouts/index.vue'),
    name:'homeIndex',
    redirect: '/home',
    meta: {
        title: '总体概览',
        icon: 'ri-home-2-line',
        //roles: ['systemAdmin'],
    },
    children:[
        {
            path: '/home',
            component: () => import('@/views/home/index.vue'),
            name: 'home',
            meta: {
                title: '总体概览',
                icon: 'ri-home-2-line',
            },
        }
    ]
};
export default homeRouter;