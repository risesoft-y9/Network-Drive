const shareRouter = {
    path: '/share',
    component: () => import('@/layouts/index.vue'),
    redirect: '/share/fileList/all/shared',
    name: 'shareindex',
    meta: {
        title: '共享空间',
        icon: 'ri-box-3-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/share/fileList/all/shared',
            component: () => import('@/views/Share/index.vue'),
            name: 'sharedIndex',
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'shared' : route.query.parentId,
                listType: 'shared'
            }),
            meta: {
                title: '共享空间',
                icon: 'ri-box-3-line'
            }
        }
        // {
        //     path: '/share/fileList/search',
        //     component: () => import('@/views/FileList/index.vue'),
        //     hidden:true,
        //     props: route => ({
        //         parentId: route.query.parentId,
        //         fileNodeType: route.query.type,
        //         searchName: route.query.key
        //     }),
        //     name:'search',
        //     meta: {
        //         title: '查询',
        //     },
        // },
    ]
};

export default shareRouter;
