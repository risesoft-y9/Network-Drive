<template>
    <div class="ai-chat-container">
        <!-- 聊天头部 -->
        <div class="chat-header">
            <div class="chat-header-left">
                <el-icon :size="20"><ChatDotRound /></el-icon>
                <span class="chat-title">AI 智能助手</span>
            </div>
            <div class="chat-header-right">
                <el-button text circle @click="handleNewSession" title="新建会话">
                    <el-icon :size="18"><Plus /></el-icon>
                </el-button>
                <el-button text circle @click="$emit('close')" title="关闭">
                    <el-icon :size="18"><Close /></el-icon>
                </el-button>
            </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesContainer" @click="handleMessageClick">
            <div v-if="messages.length === 0" class="chat-welcome">
                <div class="welcome-icon">
                    <el-icon :size="48"><MagicStick /></el-icon>
                </div>
                <p class="welcome-title">你好，我是 AI 智能助手</p>
                <p class="welcome-desc">我可以帮你智能搜索文件、分析文档内容、回答关于文件的问题</p>
                <div class="quick-actions">
                    <el-button
                        v-for="qa in quickQuestions"
                        :key="qa"
                        plain
                        size="small"
                        @click="sendMessage(qa)"
                    >
                        {{ qa }}
                    </el-button>
                </div>
            </div>

            <div
                v-for="(msg, index) in messages"
                :key="index"
                class="message-item"
                :class="{ 'message-user': msg.role === 'user', 'message-ai': msg.role === 'ai' }"
            >
                <div class="message-avatar">
                    <el-avatar v-if="msg.role === 'ai'" :size="32" :icon="ChatDotRound" />
                    <el-avatar v-else :size="32" :icon="UserFilled" />
                </div>
                <div class="message-content">
                    <div class="message-text" v-html="formatMsg(msg.content)"></div>
                    <!-- 如果消息关联了文件结果 -->
                    <div v-if="msg.files && msg.files.length > 0" class="message-files">
                        <div
                            v-for="file in msg.files"
                            :key="file.id"
                            class="file-card"
                            @click="openFile(file)"
                        >
                            <el-icon :size="20"><Document /></el-icon>
                            <span class="file-name">{{ file.name }}</span>
                            <span class="file-action">查看</span>
                        </div>
                    </div>
                    <div class="message-time">{{ formatTime(msg.time) }}</div>
                </div>
            </div>

            <!-- 加载动画 -->
            <div v-if="loading" class="message-item message-ai">
                <div class="message-avatar">
                    <el-avatar :size="32" :icon="ChatDotRound" />
                </div>
                <div class="message-content">
                    <div class="typing-indicator">
                        <span></span><span></span><span></span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
            <!-- 文件提及下拉框 -->
            <div v-if="showMention" class="mention-dropdown" ref="mentionDropdown">
                <div class="mention-header">选择文件</div>
                <div v-if="mentionLoading" class="mention-loading">搜索中...</div>
                <div v-else-if="mentionFiles.length === 0" class="mention-empty">
                    <span>未找到匹配的文件</span>
                    <span class="mention-hint">输入关键词搜索你的文件</span>
                </div>
                <div
                    v-for="(file, idx) in mentionFiles"
                    :key="file.id"
                    class="mention-item"
                    :class="{ 'mention-active': mentionActiveIdx === idx }"
                    @mousedown.prevent="selectMentionFile(file)"
                >
                    <el-icon :size="16"><Document /></el-icon>
                    <span class="mention-name" :title="file.name">{{ file.name }}</span>
                    <span class="mention-size">{{ formatFileSize(file.fileSize) }}</span>
                </div>
            </div>

            <el-input
                ref="inputRef"
                v-model="inputText"
                type="textarea"
                :rows="2"
                placeholder="输入你的问题，输入 @ 可以引用“我的文件”列表的文件，输入 @我的文件上传 可以上传文件..."
                resize="none"
                @keydown.enter.exact.prevent="onEnterKey"
                @keydown.esc="showMention = false"
                @keydown.up.prevent="mentionMoveUp"
                @keydown.down.prevent="mentionMoveDown"
                @input="handleInput"
            />
            <div class="input-actions">
                <span class="input-hint">
                    {{ isStreaming ? 'AI 正在回答...' : (showMention ? '按 ↑↓ 选择，Enter 确认，Esc 关闭' : '按 Enter 发送，输入 @ 引用文件，@我的文件上传 上传') }}
                </span>
                <el-button
                    type="primary"
                    :icon="Promotion"
                    :loading="loading"
                    :disabled="!inputText.trim()"
                    @click="handleSend"
                >
                    发送
                </el-button>
            </div>
        </div>

        <!-- 上传文件弹窗（上传目标可动态切换） -->
        <el-dialog
            v-model="showUploadDialog"
            :title="uploadDialogTitle"
            width="580px"
            :close-on-click-modal="false"
            destroy-on-close
        >
            <AddFile
                :dialog-config="uploadDialogConfig"
                :parent-id="uploadParentId"
                :list-type="uploadListType"
                :reload-table="onUploadSuccess"
            />
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
    import { ref, nextTick, inject, watch, computed } from 'vue';
    import { ElMessage } from 'element-plus';
    import {
        ChatDotRound,
        Close,
        Plus,
        MagicStick,
        UserFilled,
        Document,
        Promotion
    } from '@element-plus/icons-vue';
    import aiApi from '@/api/storage/ai';
    import y9_storage from '@/utils/storage';
    import settings from '@/settings';
    import AddFile from '@/components/file/AddFile.vue';

    const fontSizeObj: any = inject('sizeObjInfo') || {};

    interface Message {
        role: 'user' | 'ai';
        content: string;
        time: number;
        files?: any[];
    }

    const emit = defineEmits(['close', 'fileClick']);

    // 快捷问题
    const quickQuestions = [
        '@帮我找到最近上传的文件',
        '@有哪些文件超过100MB？',
        '@查找包含方案的文件',
        '@帮我分析最近的存储使用情况'
    ];

    const messages = ref<Message[]>([]);
    const inputText = ref('');
    const loading = ref(false);
    const isStreaming = ref(false);
    const sessionId = ref<string | null>(null);
    const messagesContainer = ref<HTMLElement>();
    const inputRef = ref<any>();
    const mentionDropdown = ref<HTMLElement>();

    // ==================== @ 文件提及相关状态 ====================
    const showMention = ref(false);
    const mentionKeyword = ref('');
    const mentionFiles = ref<any[]>([]);
    const mentionLoading = ref(false);
    const mentionActiveIdx = ref(0);
    // 存储已提及的文件信息，发送时用于构建 context
    const mentionedFileMap = ref<Map<string, { id: string; name: string }>>(new Map());
    let mentionDebounceTimer: any = null;

    // ==================== 上传弹窗状态（支持动态切换上传目标） ====================
    const showUploadDialog = ref(false);
    const uploadDialogConfig = ref({ show: false });
    const uploadParentId = ref('my');
    const uploadListType = ref('my');
    const uploadTargetTitle = ref('我的文件'); // 用于弹窗标题

    const uploadDialogTitle = computed(() => `上传文件至${uploadTargetTitle.value}`);

    // 监听 showUploadDialog 同步到 dialogConfig，兼容 AddFile 组件通过 dialogConfig.show 控制关闭
    watch(showUploadDialog, (val) => {
        uploadDialogConfig.value = { show: val };
    });

    /**
     * 文件上传成功后的回调，以 AI 助手身份提示上传结果
     */
    function onUploadSuccess(file?: any) {
        const target = uploadTargetTitle.value;
        ElMessage.success(`文件已上传至${target}`);
        const tip = file?.name
            ? `文件「${file.name}」已上传至${target}，你可以继续向我提问关于这个文件的问题。`
            : `文件已上传至${target}，你可以继续向我提问。`;
        messages.value.push({
            role: 'ai',
            content: tip,
            time: Date.now()
        });
        scrollToBottom();
        // AddFile 内部会延迟关闭 dialogConfig.show，这里同步关闭外层的 el-dialog
        setTimeout(() => {
            showUploadDialog.value = false;
        }, 1600);
    }

    /**
     * 处理消息列表中的操作菜单点击（事件委托）
     * 点击菜单项 → 回显 @菜单名 到输入框 → 自动弹出上传窗口
     */
    function handleMessageClick(event: MouseEvent) {
        const target = (event.target as HTMLElement).closest('.operation-menu-item') as HTMLElement;
        if (!target) return;

        // 阻止事件继续冒泡到 AI/index.vue 的 handlePageClick
        event.stopPropagation();

        const title = target.dataset.title || '';
        const listType = target.dataset.listtype || '';
        const parentId = target.dataset.parentid || '';

        if (!title) return;

        // 回显 @菜单名 到输入框
        inputText.value = `@${title} `;

        // 动态设置上传目标（如果有 listType 信息说明支持上传）
        if (listType && parentId) {
            uploadListType.value = listType;
            uploadParentId.value = parentId;
            uploadTargetTitle.value = title;
        }

        // 弹出上传窗口
        showUploadDialog.value = true;
    }

    // ==================== @ 文件提及核心逻辑 ====================

    /**
     * 从输入文本中解析当前的 @ 提及关键词
     * 返回 { keyword, startIdx } 其中 startIdx 是 @ 在文本中的位置
     * 如果当前没有正在输入的 @ 提及则返回 null
     */
    function parseCurrentMention(): { keyword: string; startIdx: number } | null {
        const text = inputText.value;
        // 匹配最后一个 @ 后面跟非空白字符（且不是已完成的 @[xxx](yyy) 格式）
        const mentionRegex = /@(?!\[[\s\S]*?\]\([\s\S]*?\))([^\s@]*)$/;
        const match = text.match(mentionRegex);
        if (match) {
            return {
                keyword: match[1],
                startIdx: match.index!
            };
        }
        return null;
    }

    /**
     * 输入事件处理：检测 @ 符号并触发文件搜索，
     * 同时检测"@我的文件上传   "触发上传弹窗
     */
    function handleInput() {
        // 优先检测 "@我的文件上传" 快捷上传指令
        const uploadTrigger = '我的文件上传';
        const text = inputText.value;
        // 匹配最后一个 @ 后面跟 "我的文件上传"（未完成的 @ 提及格式）
        const uploadRegex = new RegExp('@' + uploadTrigger.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '$');
        if (uploadRegex.test(text)) {
            // 清除关键词
            inputText.value = text.substring(0, text.length - uploadTrigger.length - 1);
            // 打开上传弹窗
            showUploadDialog.value = true;
            showMention.value = false;
            return;
        }

        const mention = parseCurrentMention();
        if (mention) {
            mentionKeyword.value = mention.keyword;
            mentionActiveIdx.value = 0;
            showMention.value = true;
            fetchMentionFiles(mention.keyword);
        } else {
            showMention.value = false;
            mentionKeyword.value = '';
        }
    }

    /**
     * 防抖搜索文件
     */
    function fetchMentionFiles(keyword: string) {
        if (mentionDebounceTimer) clearTimeout(mentionDebounceTimer);
        mentionDebounceTimer = setTimeout(async () => {
            mentionLoading.value = true;
            try {
                const res = await aiApi.searchFilesForMention(keyword || '', 8);
                if (res?.success || res?.code === 200 || res?.code === 0 || res?.code === '200') {
                    mentionFiles.value = res.data || res.list || [];
                } else {
                    mentionFiles.value = [];
                }
            } catch {
                mentionFiles.value = [];
            } finally {
                mentionLoading.value = false;
            }
        }, 100);
    }

    /**
     * 选择提及的文件：将 @keyword 替换为 @[文件名](文件ID)
     */
    function selectMentionFile(file: any) {
        const mention = parseCurrentMention();
        if (!mention) return;

        const text = inputText.value;
        const before = text.substring(0, mention.startIdx);
        const after = text.substring(mention.startIdx + 1 + mention.keyword.length);
        const mentionTag = `@[${file.name}](${file.id})`;
        inputText.value = before + mentionTag + ' ' + after;

        // 记录提及的文件信息
        mentionedFileMap.value.set(file.id, { id: file.id, name: file.name });

        showMention.value = false;
        mentionKeyword.value = '';
        // 恢复焦点
        nextTick(() => {
            const textarea = inputRef.value?.$el?.querySelector('textarea');
            if (textarea) {
                const cursorPos = before.length + mentionTag.length + 1;
                textarea.focus();
                textarea.setSelectionRange(cursorPos, cursorPos);
            }
        });
    }

    /**
     * 键盘 ↑ 选择上一项
     */
    function mentionMoveUp() {
        if (!showMention.value) return;
        mentionActiveIdx.value = Math.max(0, mentionActiveIdx.value - 1);
        scrollMentionItemIntoView();
    }

    /**
     * 键盘 ↓ 选择下一项
     */
    function mentionMoveDown() {
        if (!showMention.value) return;
        mentionActiveIdx.value = Math.min(mentionFiles.value.length - 1, mentionActiveIdx.value + 1);
        scrollMentionItemIntoView();
    }

    /**
     * 滚动使当前选中项可见
     */
    function scrollMentionItemIntoView() {
        nextTick(() => {
            const activeItem = mentionDropdown.value?.querySelector('.mention-active') as HTMLElement;
            if (activeItem) {
                activeItem.scrollIntoView({ block: 'nearest' });
            }
        });
    }

    /**
     * Enter 键处理：优先完成 @ 文件选择，再决定是否发送消息
     *
     * 流程：
     * 1. 下拉框可见 → 立即搜索当前关键词（跳过防抖）
     * 2. 有匹配文件 → 自动选中第一个（解决打字快防抖未完成的问题）
     * 3. 无匹配文件 → 关闭下拉框，但【不发送】，让用户修改输入
     * 4. 下拉框不可见 → 正常发送消息
     */
    async function onEnterKey() {
        if (showMention.value) {
            const keyword = mentionKeyword.value;
            // 保存当前键盘高亮的索引（搜索后列表可能变化，需在搜索前保存）
            const activeIdx = mentionActiveIdx.value;
            if (keyword) {
                // 立即搜索，不等防抖
                try {
                    const res = await aiApi.searchFilesForMention(keyword, 8);
                    if (res?.success || res?.code === 200 || res?.code === 0 || res?.code === '200') {
                        mentionFiles.value = res.data || res.list || [];
                    } else {
                        mentionFiles.value = [];
                    }
                } catch {
                    mentionFiles.value = [];
                }
            }
            if (mentionFiles.value.length > 0) {
                // 选中高亮的文件插入输入框，不自动发送
                const idx = Math.min(activeIdx, mentionFiles.value.length - 1);
                selectMentionFile(mentionFiles.value[idx]);
                return;
            }
            // 无匹配文件：关闭下拉框，不发送，让用户检查输入
            showMention.value = false;
            ElMessage.warning('未在网盘中找到 "' + (keyword || '') + '"，请检查文件名');
            return;
        }
        handleSend();
    }

    /**
     * 格式化文件大小
     */
    function formatFileSize(bytes: number | string | undefined): string {
        if (!bytes && bytes !== 0) return '';
        const b = typeof bytes === 'string' ? parseInt(bytes, 10) : bytes;
        if (isNaN(b)) return '';
        if (b < 1024) return b + ' B';
        if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB';
        if (b < 1024 * 1024 * 1024) return (b / (1024 * 1024)).toFixed(1) + ' MB';
        return (b / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    }

    /**
     * 从消息文本中提取 @[文件名](文件ID) 提及信息
     */
    function extractMentionsFromText(text: string): Array<{ id: string; name: string }> {
        const mentions: Array<{ id: string; name: string }> = [];
        const regex = /@\[(.*?)\]\(([^)]+)\)/g;
        let match;
        while ((match = regex.exec(text)) !== null) {
            mentions.push({ name: match[1], id: match[2] });
        }
        return mentions;
    }

    // 滚动到底部
    function scrollToBottom() {
        nextTick(() => {
            const el = messagesContainer.value;
            if (el) {
                el.scrollTop = el.scrollHeight;
            }
        });
    }

    // 格式化消息内容（简单markdown转html + @提及高亮）
    function formatMsg(text: string): string {
        if (!text) return '';

        // 获取 access_token
        let accessToken = '';
        try {
            accessToken = y9_storage.getObjectItem(settings.siteTokenKey, 'access_token') || '';
        } catch (e) {}

        let result = text
            // @[文件名](文件ID) → 带样式的文件引用标签
            .replace(
                /@\[(.*?)\]\(([^)]+)\)/g,
                '<span class="mention-file-tag" data-file-id="$2">@$1</span>'
            )
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            .replace(/\n/g, '<br/>')
            .replace(/`([^`]+)`/g, '<code>$1</code>');

        // 自动为下载/预览链接拼接 access_token
        if (accessToken) {
            // 1. 处理已有的 <a> 标签中的下载/预览链接
            result = result.replace(/href="([^"]*(?:\/downloadFile\?|\/preview\?|\/s\/)[^"]*)"/g, (_match, url) => {
                if (url.indexOf('access_token=') === -1) {
                    return 'href="' + url + '&access_token=' + accessToken + '"';
                }
                return _match;
            });
            // 2. 自动链接明文 URL（不在 href 中的）
            result = result.replace(/(?<!["'>=])(https?:\/\/[^\s<>"')\]]+)/g, (match) => {
                let url = match;
                if (url.indexOf('/downloadFile?') !== -1 || url.indexOf('/preview?') !== -1 || url.indexOf('/s/') !== -1) {
                    if (url.indexOf('access_token=') === -1) {
                        url += '&access_token=' + accessToken;
                    }
                }
                return '<a href="' + url + '" target="_blank">' + match + '</a>';
            });
        }

        return result;
    }

    // 格式化时间
    function formatTime(timestamp: number): string {
        const date = new Date(timestamp);
        const h = date.getHours().toString().padStart(2, '0');
        const m = date.getMinutes().toString().padStart(2, '0');
        return `${h}:${m}`;
    }

    // 从响应中提取回复文本
    function extractReply(res: any): string {
        if (!res) return '';
        // 格式1: { success: true, data: { reply: "..." } }
        if (res.data?.reply) return res.data.reply;
        if (res.data?.content) return res.data.content;
        if (res.data?.answer) return res.data.answer;
        if (typeof res.data === 'string') return res.data;
        // 格式2: { code: 200, data: "..." } 或 { code: 200, msg: "..." }
        if (res.reply) return res.reply;
        if (res.content) return res.content;
        if (res.answer) return res.answer;
        // 格式3: res 本身就是字符串
        if (typeof res === 'string') return res;
        // 格式4: { success: true, msg: "..." }
        if (res.success && res.msg) return res.msg;
        // 格式5: 直接在 data 上
        if (res.data && typeof res.data === 'object') {
            return res.data.reply || res.data.content || res.data.answer || JSON.stringify(res.data);
        }
        return '';
    }

    // 从响应中提取文件列表
    function extractFiles(res: any): any[] {
        if (!res) return [];
        if (res.data?.files) return res.data.files;
        if (res.data?.list) return res.data.list;
        if (res.files) return res.files;
        if (res.list) return res.list;
        return [];
    }

    // 发送消息
    async function sendMessage(text?: string) {
        const msg = text || inputText.value.trim();
        if (!msg || loading.value) return;

        // 解析消息中的 @ 文件提及
        const mentions = extractMentionsFromText(msg);
        // 构建发给 AI 的纯净文本：去掉 @[xxx](yyy) 语法糖，保留文件名引用
        const cleanMsg = msg.replace(/@\[(.*?)\]\(([^)]+)\)/g, '文件「$1」');

        // 添加用户消息（用纯净文本展示，AI 回复也用纯净文本）
        messages.value.push({
            role: 'user',
            content: cleanMsg,
            time: Date.now()
        });
        inputText.value = '';
        showMention.value = false;
        mentionedFileMap.value.clear();
        scrollToBottom();

        // 构建 context：包含提及的文件信息
        const context: any = {
            source: 'storage',
            currentPath: window.location.href
        };
        if (mentions.length > 0) {
            context.mentionedFiles = mentions;
        }

        // 调用 AI 接口（发送纯净文本，文件引用通过 context + mentionedFileIds 传递）
        loading.value = true;
        isStreaming.value = true;
        try {
            // 额外的文件ID参数，作为 context.mentionedFiles 的备份通道，避免 JSON 嵌套编码丢失
            const mentionedFileIds = mentions.length > 0 ? mentions.map((m: any) => m.id).join(',') : '';
            const res = await aiApi.aiChat(sessionId.value, cleanMsg, context, mentionedFileIds);

            // 开发环境打印响应，方便调试对接
            if (import.meta.env.DEV) {
                console.log('[AI Chat] 后端响应:', res);
            }

            // 兼容多种响应格式
            const isSuccess =
                res?.success === true ||
                res?.code === 200 ||
                res?.code === '200' ||
                res?.code === 0 ||
                (res !== undefined && res !== null && !res?.error);

            if (isSuccess) {
                // 保存会话ID
                if (!sessionId.value && (res.data?.sessionId || res.sessionId)) {
                    sessionId.value = res.data?.sessionId || res.sessionId;
                }

                const replyText = extractReply(res);
                const files = extractFiles(res);

                messages.value.push({
                    role: 'ai',
                    content: replyText || '收到你的消息，但我暂时无法提供有效回复。',
                    time: Date.now(),
                    files: files
                });

                // 上传窗口由用户点击菜单项后触发（handleMessageClick），这里不再自动弹出
            } else {
                // 请求失败的处理
                const errorMsg = res?.msg || res?.message || '请求失败，请稍后重试';
                console.warn('[AI Chat] 请求失败:', errorMsg, res);
                messages.value.push({
                    role: 'ai',
                    content: errorMsg,
                    time: Date.now()
                });
            }
        } catch (error: any) {
            console.error('[AI Chat] 异常:', error);
            // 检查是否有响应信息
            let errorMsg = 'AI 服务请求失败，请稍后重试';
            if (error?.response?.data) {
                const errData = error.response.data;
                errorMsg = extractReply(errData) || errData.msg || errData.message || errorMsg;
            } else if (error?.message) {
                errorMsg = error.message;
            }
            messages.value.push({
                role: 'ai',
                content: errorMsg,
                time: Date.now()
            });
        } finally {
            loading.value = false;
            isStreaming.value = false;
            scrollToBottom();
        }
    }

    /**
     * 发送按钮点击：先解析未完成的 @ 提及，再发送
     */
    async function handleSend() {
        // 如果下拉框可见，先解析 @ 提及插入输入框，不自动发送
        if (showMention.value) {
            const keyword = mentionKeyword.value;
            // 保存当前键盘高亮的索引
            const activeIdx = mentionActiveIdx.value;
            if (keyword) {
                try {
                    const res = await aiApi.searchFilesForMention(keyword, 8);
                    if (res?.success || res?.code === 200 || res?.code === 0 || res?.code === '200') {
                        mentionFiles.value = res.data || res.list || [];
                    }
                } catch {
                    mentionFiles.value = [];
                }
                if (mentionFiles.value.length > 0) {
                    const idx = Math.min(activeIdx, mentionFiles.value.length - 1);
                    selectMentionFile(mentionFiles.value[idx]);
                    return;
                }
                showMention.value = false;
                ElMessage.warning('未在网盘中找到 "' + keyword + '"，请检查文件名');
                return;
            }
        }
        // 强制关闭下拉框并发送
        showMention.value = false;
        sendMessage();
    }

    // 新建会话
    function handleNewSession() {
        sessionId.value = null;
        messages.value = [];
        mentionedFileMap.value.clear();
    }

    // 打开文件
    function openFile(file: any) {
        emit('fileClick', file);
    }
</script>

<style lang="scss" scoped>
    .ai-chat-container {
        display: flex;
        flex-direction: column;
        height: 100%;
        background: var(--el-bg-color);
        border-radius: 8px;
        overflow: hidden;
    }

    .chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px 16px;
        border-bottom: 1px solid var(--el-border-color-light);
        background: var(--el-bg-color);

        .chat-header-left {
            display: flex;
            align-items: center;
            gap: 8px;
            color: var(--el-color-primary);

            .chat-title {
                font-size: 16px;
                font-weight: 600;
                color: var(--el-text-color-primary);
            }
        }
    }

    .chat-messages {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 16px;

        .chat-welcome {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
            text-align: center;

            .welcome-icon {
                color: var(--el-color-primary-light-3);
                margin-bottom: 16px;
            }

            .welcome-title {
                font-size: 18px;
                font-weight: 600;
                color: var(--el-text-color-primary);
                margin: 0 0 8px 0;
            }

            .welcome-desc {
                font-size: 13px;
                color: var(--el-text-color-secondary);
                margin: 0 0 20px 0;
                max-width: 320px;
            }

            .quick-actions {
                display: flex;
                flex-wrap: wrap;
                gap: 8px;
                justify-content: center;
            }
        }

        .message-item {
            display: flex;
            gap: 10px;
            max-width: 85%;

            &.message-user {
                align-self: flex-end;
                flex-direction: row-reverse;

                .message-content {
                    .message-text {
                        background: var(--el-color-primary);
                        color: #fff;
                        border-radius: 12px 12px 4px 12px;
                    }
                }
            }

            &.message-ai {
                align-self: flex-start;

                .message-content {
                    .message-text {
                        background: var(--el-fill-color-light);
                        border-radius: 12px 12px 12px 4px;

                        :deep(code) {
                            background: var(--el-color-primary-light-9);
                            color: var(--el-color-primary);
                            padding: 2px 6px;
                            border-radius: 3px;
                            font-size: 12px;
                        }

                        :deep(strong) {
                            color: var(--el-color-primary);
                        }
                    }
                }
            }

            .message-content {
                display: flex;
                flex-direction: column;
                gap: 4px;

                .message-text {
                    padding: 10px 14px;
                    font-size: 14px;
                    line-height: 1.6;
                    word-break: break-word;
                    color: var(--el-text-color-primary);

                    :deep(a) {
                        color: var(--el-color-primary);
                        text-decoration: underline;
                        &:hover {
                            color: var(--el-color-primary-light-3);
                        }
                    }
                }

                .message-files {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 6px;
                    margin-top: 4px;

                    .file-card {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        padding: 6px 10px;
                        background: var(--el-color-primary-light-9);
                        border: 1px solid var(--el-color-primary-light-5);
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 12px;
                        color: var(--el-color-primary);
                        transition: all 0.2s;

                        &:hover {
                            background: var(--el-color-primary-light-7);
                        }

                        .file-name {
                            max-width: 120px;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }
                    }
                }

                .message-time {
                    font-size: 11px;
                    color: var(--el-text-color-placeholder);
                    padding: 0 4px;
                }
            }
        }
    }

    .typing-indicator {
        display: flex;
        gap: 4px;
        padding: 12px 14px;

        span {
            width: 6px;
            height: 6px;
            background: var(--el-color-primary);
            border-radius: 50%;
            animation: typing 1.4s infinite ease-in-out both;

            &:nth-child(1) { animation-delay: -0.32s; }
            &:nth-child(2) { animation-delay: -0.16s; }
            &:nth-child(3) { animation-delay: 0s; }
        }
    }

    @keyframes typing {
        0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
        40% { opacity: 1; transform: scale(1); }
    }

    .chat-input-area {
        flex-shrink: 0;
        border-top: 1px solid var(--el-border-color-light);
        padding: 12px 16px;
        background: var(--el-bg-color);
        position: relative;

        :deep(.el-textarea__inner) {
            border-radius: 8px;
            background: var(--el-fill-color-light);
            font-size: 14px;
        }

        .input-actions {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 8px;

            .input-hint {
                font-size: 12px;
                color: var(--el-text-color-placeholder);
            }
        }

        // ==================== @ 提及文件下拉框 ====================
        .mention-dropdown {
            position: absolute;
            bottom: calc(100% - 4px);
            left: 16px;
            right: 16px;
            max-height: 240px;
            overflow-y: auto;
            background: var(--el-bg-color);
            border: 1px solid var(--el-border-color);
            border-radius: 8px;
            box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
            z-index: 1000;

            .mention-header {
                padding: 8px 12px;
                font-size: 12px;
                color: var(--el-text-color-secondary);
                border-bottom: 1px solid var(--el-border-color-light);
                background: var(--el-fill-color-lighter);
            }

            .mention-loading,
            .mention-empty {
                padding: 16px;
                text-align: center;
                font-size: 13px;
                color: var(--el-text-color-secondary);

                .mention-hint {
                    display: block;
                    margin-top: 4px;
                    font-size: 11px;
                    color: var(--el-text-color-placeholder);
                }
            }

            .mention-item {
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 8px 12px;
                cursor: pointer;
                transition: background 0.15s;
                border-bottom: 1px solid var(--el-border-color-extra-light);

                &:last-child {
                    border-bottom: none;
                }

                &:hover,
                &.mention-active {
                    background: var(--el-color-primary-light-9);
                }

                .mention-name {
                    flex: 1;
                    font-size: 13px;
                    color: var(--el-text-color-primary);
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }

                .mention-size {
                    font-size: 11px;
                    color: var(--el-text-color-placeholder);
                    white-space: nowrap;
                }
            }
        }
    }

    // ==================== @ 提及文件标签（消息中） ====================
    :deep(.mention-file-tag) {
        display: inline-block;
        padding: 1px 6px;
        margin: 0 2px;
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        border-radius: 4px;
        font-size: 13px;
        cursor: pointer;
        font-weight: 500;
        transition: background 0.2s;

        &:hover {
            background: var(--el-color-primary-light-7);
        }
    }
</style>
