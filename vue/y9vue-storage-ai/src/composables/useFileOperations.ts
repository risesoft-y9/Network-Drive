/**
 * 文件操作菜单配置
 * 定义各路由菜单支持的操作，用于 AI 对话中的操作引导
 */

import { getUserRoles } from '@/router/checkRouter';

export interface MenuOperation {
    key: string; // 操作标识
    label: string; // 操作名称（中文）
    description: string; // 操作描述
}

export interface FileOperationMenu {
    route: string; // Vue Router 路径
    title: string; // 菜单标题
    icon: string; // 图标 class（remix icon）
    description: string; // 简要描述
    operations: MenuOperation[]; // 支持的操作列表
    /** 哪些操作条件限制说明 */
    conditions?: string;
    /** 文件列表类型（用于上传 API 的 listType 参数） */
    listType: string;
    /** 默认父节点ID（根目录，用于上传 API 的 parentId 参数） */
    parentId: string;
    /** 访问该菜单所需的角色，未设置则所有人可见 */
    roles?: string[];
}

/**
 * 标准操作定义（复用于多个菜单）
 */
const OP = {
    UPLOAD_FILE: { key: 'uploadFile', label: '上传文件', description: '将文件上传到当前目录' },
    BIG_UPLOAD: { key: 'bigUpload', label: '大文件上传', description: '上传超过120MB的大文件' },
    CREATE_FOLDER: { key: 'createFolder', label: '新建文件夹', description: '在当前目录下创建新文件夹' },
    DELETE: { key: 'delete', label: '删除', description: '将选中文件/文件夹移至回收站' },
    PERMANENT_DELETE: { key: 'permanentDelete', label: '彻底删除', description: '从回收站中彻底删除文件，不可恢复' },
    RESTORE: { key: 'restore', label: '还原', description: '将回收站中的文件恢复到原始位置' },
    EMPTY_RECYCLE: { key: 'emptyRecycle', label: '清空回收站', description: '一次性清空回收站中的所有文件' },
    RENAME: { key: 'rename', label: '重命名', description: '修改文件或文件夹的名称' },
    DOWNLOAD: { key: 'download', label: '下载', description: '将文件下载到本地' },
    MOVE: { key: 'move', label: '移动到', description: '将文件移动到其他目录' },
    SHARE: { key: 'share', label: '共享', description: '将文件共享给其他用户' },
    CANCEL_SHARE: { key: 'cancelShare', label: '取消共享', description: '取消已共享文件的共享状态' },
    CANCEL_LINK_SHARE: { key: 'cancelLinkShare', label: '取消直链分享', description: '取消文件的直链分享' },
    COPY_LINK: { key: 'copyLink', label: '复制分享链接', description: '复制直链分享的下载链接和提取码' },
    COLLECT: { key: 'collect', label: '收藏', description: '将文件添加到收藏列表' },
    UNCOLLECT: { key: 'uncollect', label: '取消收藏', description: '将文件从收藏列表中移除' },
    BATCH_TAG: { key: 'batchTag', label: '批量标签', description: '为选中的文件批量设置标签' },
    FILE_REPORT: { key: 'fileReport', label: '文件上报', description: '将文件上报提交给管理员' },
} as const;

/**
 * 用户输入关键词 → 操作 key 的映射
 * 用于识别用户的文件操作意图
 */
const KEYWORD_TO_OP_KEYS: Record<string, string[]> = {
    '上传': ['uploadFile', 'bigUpload'],
    '新建': ['createFolder'],
    '创建': ['createFolder'],
    '删除': ['delete', 'permanentDelete', 'emptyRecycle'],
    '彻底删除': ['permanentDelete'],
    '清空回收站': ['emptyRecycle'],
    '还原': ['restore'],
    '修改': ['rename'],
    '重命名': ['rename'],
    '下载': ['download'],
    '移动': ['move'],
    '共享': ['share'],
    '取消共享': ['cancelShare'],
    '取消直链': ['cancelLinkShare'],
    '收藏': ['collect'],
    '取消收藏': ['uncollect'],
    '标签': ['batchTag'],
    '上报': ['fileReport'],
    '复制链接': ['copyLink'],
};

/**
 * 文件操作菜单列表
 */
export const fileOperationMenus: FileOperationMenu[] = [
    {
        route: '/my/fileList/all',
        title: '我的文件',
        icon: 'ri-file-text-line',
        description: '个人的文件存储空间，所有文件归你所有',
        operations: [
            OP.UPLOAD_FILE, OP.BIG_UPLOAD, OP.CREATE_FOLDER,
            OP.DOWNLOAD, OP.DELETE, OP.RENAME, OP.MOVE,
            OP.SHARE, OP.BATCH_TAG, OP.COLLECT,
        ],
        conditions: '上传、新建文件夹、删除、重命名仅限自己的文件',
        listType: 'my',
        parentId: 'my',
    },
    {
        route: '/dept/fileList/all',
        title: '部门文件',
        icon: 'ri-file-copy-2-line',
        description: '本部门共享的文件存储空间',
        operations: [
            OP.UPLOAD_FILE, OP.BIG_UPLOAD, OP.CREATE_FOLDER,
            OP.DOWNLOAD, OP.DELETE, OP.RENAME, OP.MOVE, OP.BATCH_TAG,
        ],
        conditions: '上传、删除、重命名仅限自己的文件',
        listType: 'dept',
        parentId: 'dept',
    },
    {
        route: '/manage/index',
        title: '公共文件管理',
        icon: 'ri-folder-settings-line',
        description: '管理公共文件（需管理员权限）',
        operations: [
            OP.UPLOAD_FILE, OP.BIG_UPLOAD, OP.CREATE_FOLDER,
            OP.DOWNLOAD, OP.DELETE, OP.RENAME, OP.MOVE,
        ],
        conditions: '需公共文件管理员权限',
        listType: 'public',
        parentId: 'public',
        roles: ['publicManager', 'systemManager'],
    },
    {
        route: '/report/index',
        title: '文件上报',
        icon: 'ri-folder-upload-line',
        description: '向管理员上报文件的入口',
        operations: [
            OP.UPLOAD_FILE, OP.BIG_UPLOAD, OP.CREATE_FOLDER,
            OP.DOWNLOAD, OP.DELETE, OP.RENAME, OP.MOVE, OP.BATCH_TAG,
        ],
        conditions: '根目录不可上传，需进入子文件夹操作；删除/重命名仅限自己的文件',
        listType: 'report',
        parentId: 'report',
    },
    {
        route: '/reportManage/index',
        title: '文件上报管理',
        icon: 'ri-folder-settings-line',
        description: '管理上报文件（需管理员权限）',
        operations: [
            OP.UPLOAD_FILE, OP.BIG_UPLOAD, OP.CREATE_FOLDER,
            OP.DOWNLOAD, OP.DELETE, OP.RENAME, OP.MOVE,
        ],
        conditions: '需文件上报管理员权限；根目录不可上传',
        listType: 'report',
        parentId: 'report',
    },
    {
        route: '/share/fileList/all/shared',
        title: '共享空间',
        icon: 'ri-box-3-line',
        description: '查看其他用户共享给你的文件',
        operations: [OP.DOWNLOAD, OP.COLLECT],
        conditions: '只能下载和收藏；如有所有者权限可重命名或删除',
        listType: 'shared',
        parentId: 'shared',
    },
    {
        route: '/myShare/index',
        title: '共享记录',
        icon: 'ri-share-line',
        description: '查看你共享给别人的文件记录',
        operations: [OP.CANCEL_SHARE],
        conditions: '只能取消共享，不能操作文件本身',
        listType: '',
        parentId: '',
    },
    {
        route: '/linkShare/index',
        title: '直链分享',
        icon: 'ri-links-line',
        description: '管理已创建的直链分享链接',
        operations: [OP.COPY_LINK, OP.CANCEL_LINK_SHARE],
        conditions: '复制链接仅限非文件夹；可取消分享',
        listType: '',
        parentId: '',
    },
    {
        route: '/collect/index',
        title: '我的收藏',
        icon: 'ri-star-line',
        description: '查看你收藏的文件列表',
        operations: [OP.DOWNLOAD, OP.UNCOLLECT],
        conditions: '下载仅限文件（不含文件夹）；可取消收藏',
        listType: '',
        parentId: '',
    },
    {
        route: '/recycle/recycleBin',
        title: '回收站',
        icon: 'ri-delete-bin-5-line',
        description: '已删除的文件临时存放处，可还原或彻底删除',
        operations: [OP.RESTORE, OP.PERMANENT_DELETE, OP.EMPTY_RECYCLE],
        conditions: '还原恢复到原始位置；彻底删除不可恢复',
        listType: '',
        parentId: '',
    },
];

/**
 * 检测用户输入中包含哪些文件操作意图，返回对应的操作 key 列表
 * @param message 用户输入的消息文本
 * @returns 匹配到的操作 key 列表；若无匹配则返回空数组（代表"未识别到明确的操作意图"）
 */
export function detectOperationKeys(message: string): string[] {
    const keys = new Set<string>();
    // 按关键词长度降序排列，避免短词误匹配（如"彻底删除"优于"删除"）
    const sorted = Object.entries(KEYWORD_TO_OP_KEYS).sort((a, b) => b[0].length - a[0].length);
    for (const [keyword, opKeys] of sorted) {
        if (message.includes(keyword)) {
            opKeys.forEach((k) => keys.add(k));
        }
    }
    return Array.from(keys);
}

/**
 * 生成发送给 AI 的菜单上下文（用于注入到 aiChat context 中）
 */
export function buildMenuContext(): string {
    const menuList = fileOperationMenus.map((menu) => {
        const ops = menu.operations.map((op) => op.label).join('、');
        return {
            title: menu.title,
            route: menu.route,
            description: menu.description,
            operations: ops,
        };
    });
    return JSON.stringify(menuList);
}

/**
 * 根据检测到的操作意图，生成前置提示文字
 * @param operationKeys 匹配到的操作 key 列表
 * @returns 适合放在菜单列表前的提示文字
 */
export function getMenuPrompt(operationKeys: string[]): string {
    if (operationKeys.includes('uploadFile') || operationKeys.includes('bigUpload')) {
        return '请问您要上传到哪个位置？点击下方菜单即可自动弹出上传窗口：';
    }
    if (operationKeys.includes('createFolder')) {
        return '请问您要在哪个位置新建文件夹？点击下方菜单即可跳转：';
    }
    if (operationKeys.includes('delete') || operationKeys.includes('permanentDelete')) {
        return '您可以在以下位置执行删除操作：';
    }
    if (operationKeys.includes('rename')) {
        return '您可以在以下位置执行重命名操作：';
    }
    if (operationKeys.includes('download')) {
        return '您可以在以下位置下载文件：';
    }
    return '您可以在以下位置执行该操作：';
}

/**
 * 生成操作菜单 HTML 片段（含前置提示文字）
 * @param operationKeys 可选，按操作 key 过滤菜单；不传则显示全部菜单
 * @param promptText 可选，前置提示文字；不传则根据 operationKeys 自动生成
 *
 * 说明：title 属性仅作为鼠标悬停提示（菜单名），
 *      功能列表只显示一次（位于可见文本中），避免重复
 */
export function buildMenuHTML(operationKeys?: string[], promptText?: string): string {
    let menus = fileOperationMenus;

    // 按操作类型过滤：只保留包含任一目标操作的菜单
    if (operationKeys && operationKeys.length > 0) {
        menus = menus.filter((menu) =>
            menu.operations.some((op) => operationKeys.includes(op.key))
        );
    }

    // 按角色过滤：如果菜单定义了 roles，则用户必须拥有其中至少一个角色
    const userRoles = getUserRoles();
    menus = menus.filter((menu) => {
        if (!menu.roles || menu.roles.length === 0) return true;
        return menu.roles.some((role) => userRoles.includes(role));
    });

    if (menus.length === 0) return '';

    const prompt = promptText ?? (operationKeys ? getMenuPrompt(operationKeys) : '');

    const items = menus.map((menu) => {
        const ops = menu.operations.map((op) => op.label).join('、');
        return `<span class="operation-menu-item" data-route="${menu.route}" data-title="${menu.title}" data-listtype="${menu.listType}" data-parentid="${menu.parentId}" title="${menu.title}">📁 <strong>${menu.title}</strong>：${ops}</span>`;
    });

    const menuBlock = `<div class="operation-menus">${items.join('')}</div>`;
    return prompt ? `<p class="menu-prompt">${prompt}</p>${menuBlock}` : menuBlock;
}
