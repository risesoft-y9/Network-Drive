const storageRouter = {
        path: '/my',
        component: () => import('@/layouts/index.vue'),
        name:'my',
        redirect: '/my/fileList/all',
        meta: {
            title: '我的文件',
            icon: 'ri-file-text-line',
            roles: ['user','reportManager','capacityManager','publicManager'],
        },
        children:[
            {
                path: '/my/fileList/all',
                component: () => import('@/views/FileList/index.vue'),
                name: 'all',
                props: route => ({parentId: route.query.parentId==undefined?'my':route.query.parentId,listType:'my'}),
                meta: {
                    title: '全部文件',
                    icon: 'ri-file-copy-2-line',
                },
            },
            {
                path: '/my/fileList/PICTURE',
                component: () => import('@/views/FileList/index.vue'),
                name: 'picture',
                props: route => ({fileNodeType: 'PICTURE',listType:'my'}),
                meta: {
                    title: '图片',
                    icon: 'ri-image-2-line',
                },
            },
            {
                path: '/my/fileList/DOCUMENT',
                component: () => import('@/views/FileList/index.vue'),
                name: 'document',
                props: route => ({fileNodeType: 'DOCUMENT',listType:'my'}),
                meta: {
                    title: '文档',
                    icon: 'ri-file-word-2-line',
                },
            },
            {
                path: '/my/fileList/VIDEO',
                component: () => import('@/views/FileList/index.vue'),
                name: 'video',
                props: route => ({fileNodeType: 'VIDEO',listType:'my'}),
                meta: {
                    title: '视频',
                    icon: 'ri-vidicon-2-line',
                },
            },
            {
                path: '/my/fileList/AUDIO',
                component: () => import('@/views/FileList/index.vue'),
                name: 'audio',
                props: route => ({fileNodeType: 'AUDIO',listType:'my'}),
                meta: {
                    title: '音频',
                    icon: 'ri-headphone-line',
                },
            },
            {
                path: '/my/fileList/PAKEAGE',
                component: () => import('@/views/FileList/index.vue'),
                name: 'pakeage',
                props: route => ({fileNodeType: 'PAKEAGE',listType:'my'}),
                meta: {
                    title: '压缩包',
                    icon: 'ri-file-zip-line',
                },
            },
            {
                path: '/my/fileList/OTHERS',
                component: () => import('@/views/FileList/index.vue'),
                name: 'others',
                props: route => ({fileNodeType: 'OTHERS',listType:'my'}),
                meta: {
                    title: '其他',
                    icon: 'ri-more-line',
                },
            },
            {
                path: '/my/fileList/search', 
                component: () => import('@/views/FileList/index.vue'),
                hidden:true, 
                props: route => ({
                    parentId: route.query.parentId,
                    fileNodeType: route.query.type,
                    searchName: route.query.key
                }),
                name:'search',
                meta: {
                    title: '查询',
                },
            },
            // {
            //     path: '/my/fileList/all/:parentId/:listType', 
            //     component: () => import('@/views/FileList/index.vue'),
            //     hidden:true, 
            //     props: true,
            //     name:'parent',
            // },
            // {
            //     path: '/my/fileList/:fileNodeType',
            //     component: () => import('@/views/FileList/index.vue'),
            //     props: true,
            //     name:'type',
            // },
        ]
    };
export default storageRouter;
