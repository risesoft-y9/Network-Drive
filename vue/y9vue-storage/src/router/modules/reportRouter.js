const reportRouter = {
    path: '/report/index',
    component: () => import('@/layouts/index.vue'),
    redirect: '/report/index',
    name: 'report',
    meta: {
        title: '文件上报',
        icon: 'ri-folder-upload-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/report/index',
            component: () => import('@/views/Report/index.vue'),
            name: 'reportIndex',
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'report' : route.query.parentId,
                listType: 'report',
                roleType: 'common'
            }),
            meta: {
                title: '文件上报',
                icon: 'ri-folder-upload-line'
            }
        }
    ]
};

export default reportRouter;
