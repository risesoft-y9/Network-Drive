const subscriptionRouter = {
    path: '/subscribe',
    component: () => import('@/layouts/index.vue'),
    name:'subscribeIndex',
    redirect: '/subscribe',
    meta: {
        title: '数据订阅',
        icon: 'ri-rss-line',
    },
    children:[
        {
            path: '/subscribe',
            component: () => import('@/views/subscribe/index.vue'),
            name: 'subscribe',
            meta: {
                title: '数据订阅',
                icon: 'ri-rss-line',
            },
        }
    ]
};
export default subscriptionRouter;
