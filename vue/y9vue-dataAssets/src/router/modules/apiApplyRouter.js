const apiApplyRouter = {
    path: '/demand',
    component: () => import('@/layouts/index.vue'),
    name:'demandIndex',
    redirect: '/demand',
    meta: {
        title: '资源申请',
        icon: 'ri-git-pull-request-line',
        //roles: ['systemAdmin']
    },
    children:[
        {
            path: '/demand',
            component: () => import('@/views/demand/index.vue'),
            name: 'demand',
            meta: {
                title: '资源申请',
                icon: 'ri-git-pull-request-line',
            },
        }
    ]
};
export default apiApplyRouter;
