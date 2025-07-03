const capacityRouter = {
    path: '/capacity',
    component: () => import('@/layouts/index.vue'),
    redirect: '/capacity/manage',
    name: 'capacity',
    meta: {
        title: '存储空间管理',
        roles: ['capacityManager']
    },
    children: [
        {
            path: '/capacity/manage',
            component: () => import('@/views/Capacity/index.vue'),
            name: 'capacityIndex',
            meta: {
                title: '存储空间管理',
                icon: 'ri-sd-card-mini-line'
            }
        }
    ]
};

export default capacityRouter;
