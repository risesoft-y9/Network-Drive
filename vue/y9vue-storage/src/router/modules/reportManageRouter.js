const escalationRouter = {
    path: '/reportManage/index',
    component: () => import('@/layouts/index.vue'),
    redirect: '/reportManage/index',
    name: 'reportManage',
    meta: {
        title: '文件上报管理',
        icon: '"ri-folder-settings-line',
        roles: ['reportManager']
    },
    children: [
        {
            path: '/reportManage/index',
            component: () => import('@/views/Report/manage.vue'),
            name: 'reportManageIndex',
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'report' : route.query.parentId,
                listType: 'reportManage',
                roleType: 'manage'
            }),
            meta: {
                title: '文件上报管理',
                icon: 'ri-folder-settings-line'
            }
        }
    ]
};

export default escalationRouter;
