const recycleRouter = {
    path: '/recycle',
    component: () => import('@/layouts/index.vue'),
    redirect: '/recycle/recycleBin',
    name: 'recycle',
    meta: {
        title: '回收站',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/recycle/recycleBin',
            component: () => import('@/views/RecycleBin/index.vue'),
            name: 'recycleIndex',
            meta: {
                title: '回收站',
                icon: 'ri-delete-bin-5-line'
            }
        }
    ]
};

export default recycleRouter;
