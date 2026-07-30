const publicStorageRouter = {
    path: '/public',
    component: () => import('@/layouts/index.vue'),
    name: 'public',
    redirect: '/public/fileList/all',
    meta: {
        title: '公共文件',
        icon: 'ri-newspaper-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/public/fileList/all',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_all',
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'public' : route.query.parentId,
                roleType: 'common'
            }),
            meta: {
                title: '全部文件',
                icon: 'ri-article-line'
            }
        },
        {
            path: '/public/fileList/PICTURE',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_picture',
            props: (route) => ({ fileNodeType: 'PICTURE', roleType: 'common' }),
            meta: {
                title: '图片',
                icon: 'ri-image-2-line'
            }
        },
        {
            path: '/public/fileList/DOCUMENT',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_document',
            props: (route) => ({ fileNodeType: 'DOCUMENT', roleType: 'common' }),
            meta: {
                title: '文档',
                icon: 'ri-file-word-2-line'
            }
        },
        {
            path: '/public/fileList/VIDEO',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_video',
            props: (route) => ({ fileNodeType: 'VIDEO', roleType: 'common' }),
            meta: {
                title: '视频',
                icon: 'ri-vidicon-2-line'
            }
        },
        {
            path: '/public/fileList/AUDIO',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_audio',
            props: (route) => ({ fileNodeType: 'AUDIO', roleType: 'common' }),
            meta: {
                title: '音频',
                icon: 'ri-headphone-line'
            }
        },
        {
            path: '/public/fileList/PAKEAGE',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_pakeage',
            props: (route) => ({ fileNodeType: 'PAKEAGE', roleType: 'common' }),
            meta: {
                title: '压缩包',
                icon: 'ri-file-zip-line'
            }
        },
        {
            path: '/public/fileList/OTHERS',
            component: () => import('@/views/Public/index.vue'),
            name: 'public_others',
            props: (route) => ({ fileNodeType: 'OTHERS', roleType: 'common' }),
            meta: {
                title: '其他',
                icon: 'ri-more-line'
            }
        },
        {
            path: '/public/fileList/search',
            component: () => import('@/views/Public/index.vue'),
            hidden: true,
            props: (route) => ({
                parentId: route.query.parentId == undefined ? 'public' : route.query.parentId,
                fileNodeType: route.query.fileNodeType,
                searchName: route.query.searchName,
                roleType: 'common',
                startDate: route.query.startDate,
                endDate: route.query.endDate
            }),
            name: 'public_search',
            meta: {
                title: '查询'
            }
        }
    ]
};
export default publicStorageRouter;
