const aiRouter = {
    path: '/ai',
    component: () => import('@/layouts/index.vue'),
    name: 'ai',
    redirect: '/ai/index',
    meta: {
        title: 'AI智能助手',
        icon: 'ri-magic-line',
        roles: ['user', 'reportManager', 'capacityManager', 'publicManager', 'systemAdmin', 'systemManager', 'tagManager']
    },
    children: [
        {
            path: '/ai/index',
            component: () => import('@/views/AI/index.vue'),
            name: 'aiIndex',
            meta: {
                title: 'AI智能助手',
                icon: 'ri-magic-line'
            }
        }
    ]
};
export default aiRouter;
