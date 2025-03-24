const examineRouter = {
    path: '/examine',
    component: () => import('@/layouts/index.vue'),
    name:'examineIndex',
    redirect: '/examine',
    meta: {
        title: '资产审核',
        icon: 'ri-function-line',
    },
    children:[
        {
            path: '/examine',
            component: () => import('@/views/pretreat/examine.vue'),
            name: 'examine',
            meta: {
                title: '资产审核',
                icon: 'ri-function-line',
            },
        },
    ]
};
export default examineRouter;
