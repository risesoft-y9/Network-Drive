const deptStorageRouter = {
    path: '/dept',
    component: () => import('@/layouts/index.vue'),
    name: 'dept',
    redirect: '/dept/fileList/all',
    meta: {
        title: '部门文件',
        icon: 'ri-file-copy-2-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager']
    },
    children: [
        {
            path: '/dept/fileList/all',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_all',
            props: (route) => ({ parentId: route.query.parentId == undefined ? 'dept' : route.query.parentId }),
            meta: {
                title: '全部文件',
                icon: 'ri-file-copy-2-line'
            }
        },
        {
            path: '/dept/fileList/PICTURE',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_picture',
            props: (route) => ({ fileNodeType: 'PICTURE' }),
            meta: {
                title: '图片',
                icon: 'ri-image-2-line'
            }
        },
        {
            path: '/dept/fileList/DOCUMENT',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_document',
            props: (route) => ({ fileNodeType: 'DOCUMENT' }),
            meta: {
                title: '文档',
                icon: 'ri-file-word-2-line'
            }
        },
        {
            path: '/dept/fileList/VIDEO',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_video',
            props: (route) => ({ fileNodeType: 'VIDEO' }),
            meta: {
                title: '视频',
                icon: 'ri-vidicon-2-line'
            }
        },
        {
            path: '/dept/fileList/AUDIO',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_audio',
            props: (route) => ({ fileNodeType: 'AUDIO' }),
            meta: {
                title: '音频',
                icon: 'ri-headphone-line'
            }
        },
        {
            path: '/dept/fileList/PAKEAGE',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_pakeage',
            props: (route) => ({ fileNodeType: 'PAKEAGE' }),
            meta: {
                title: '压缩包',
                icon: 'ri-file-zip-line'
            }
        },
        {
            path: '/dept/fileList/OTHERS',
            component: () => import('@/views/Dept/index.vue'),
            name: 'dept_others',
            props: (route) => ({ fileNodeType: 'OTHERS' }),
            meta: {
                title: '其他',
                icon: 'ri-more-line'
            }
        },
        {
            path: '/dept/fileList/search',
            component: () => import('@/views/Dept/index.vue'),
            hidden: true,
            props: (route) => ({
                parentId: route.query.parentId,
                fileNodeType: route.query.type,
                searchName: route.query.key
            }),
            name: 'dept_search',
            meta: {
                title: '查询'
            }
        }
    ]
};
export default deptStorageRouter;
