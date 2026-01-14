const dictionaryRouter = {
    path: '/dictionary',
    component: () => import('@/layouts/index.vue'),
    name:'dictionary',
    redirect: '/label',
    meta: {
        title: '资产字典',
        icon: 'ri-book-marked-line',
        roles: ['systemAdmin']
    },
    children:[
        {
            path: '/label',
            component: () => import('@/views/dictionary/label/index.vue'),
            name: 'label',
            meta: {
                title: '标签管理',
                icon: 'ri-price-tag-3-line',
            },
        },
        {
            path: '/dataDictionary',
            component: () => import('@/views/dictionary/dataDict/index.vue'),
            name: 'dataDictIndex',
            meta: {
                title: '字典管理',
                icon: 'ri-book-2-line',
            },
        },
    ]
};
export default dictionaryRouter;
