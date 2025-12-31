const myRouter = {
    path: '/mysubscribe',
    component: () => import('@/layouts/index.vue'),
    name:'mysubscribeIndex',
    redirect: '/mysubscribe',
    meta: {
        title: '我的订阅',
        icon: 'ri-user-4-line',
    },
    children:[
        {
            path: '/mysubscribe',
            component: () => import('@/views/mysubscribe/index.vue'),
            name: 'mysubscribe',
            meta: {
                title: '我的订阅',
                icon: 'ri-user-4-line',
            },
        }
    ]
};
export default myRouter;
