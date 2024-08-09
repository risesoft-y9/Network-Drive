const managerRouter = [
    {
        path: '/manage/index',
        component: () => import('@/layouts/index.vue'),
        redirect: '/manage/index',
        name:'manage',
        meta: {
            title: '公共文件管理',
            icon: 'ri-folder-settings-line',
            roles: ['publicManager'],
        },
        children:[
            {
                path: '/manage/index',
                component: () => import('@/views/Public/index.vue'),
                name: 'manageIndex',
                props: route => ({parentId: route.query.parentId==undefined?'public':route.query.parentId,roleType: 'manage'}),
                meta: {
                    title: '公共文件管理',
                    icon: 'ri-folder-settings-line',
                },
            },
            {
                path: '/manage/search', 
                component: () => import('@/views/Public/index.vue'),
                hidden:true, 
                props: route => ({
                    parentId: route.query.parentId==undefined?'public':route.query.parentId,
                    fileNodeType: route.query.fileNodeType,
                    searchName: route.query.searchName,
                    roleType: 'manage',
                    startDate: route.query.startDate,
                    endDate: route.query.endDate
                }),
                name:'manage_search',
                meta: {
                    title: '查询',
                }
            }
        ]
    },
];

export default managerRouter;
