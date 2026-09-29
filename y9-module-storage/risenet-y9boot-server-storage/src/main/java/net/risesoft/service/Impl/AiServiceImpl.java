package net.risesoft.service.Impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.config.AiProperties;
import net.risesoft.entity.ChatMessage;
import net.risesoft.entity.ChatSession;
import net.risesoft.entity.FileNode;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.ChatMessageRepository;
import net.risesoft.repository.ChatSessionRepository;
import net.risesoft.repository.FileNodeRepository;
import net.risesoft.repository.spec.FileNodeSpecification;
import net.risesoft.service.AiApiClient;
import net.risesoft.service.AiService;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileNodeShareService;
import net.risesoft.util.FileContentExtractor;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.service.Y9FileStoreService;

/**
 * AI 智能服务实现
 * <p>
 * 支持 OpenAI 兼容协议的大模型接入。 配置 application.yml 中的 ai.* 即可启用真实 AI 服务。
 * 
 * @author yihong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    /** 时间意图关键词 */
    private static final String[] TIME_RECENT_KEYS = {"最近", "近期", "最新", "刚刚", "刚才", "今天", "本周", "本月", "新的"};
    private static final String[] TIME_OLD_KEYS = {"最早", "很久", "以前", "历史", "旧的"};
    /** 文件类型关键词（注意："文档"是中文泛称，不加在此处以免误过滤非Word文件） */
    private static final String[] TYPE_WORD_KEYS = {"word", "doc", "文书"};

    // ==================== AI 搜索 ====================
    private static final String[] TYPE_EXCEL_KEYS = {"excel", "表格", "xls", "电子表格", "报表"};
    private static final String[] TYPE_PDF_KEYS = {"pdf", "电子书"};
    private static final String[] TYPE_IMAGE_KEYS = {"图片", "照片", "图像", "jpg", "png", "gif", "截图"};
    private static final String[] TYPE_VIDEO_KEYS = {"视频", "影片", "mp4", "录像"};
    private static final String[] TYPE_ZIP_KEYS = {"压缩包", "zip", "rar", "压缩文件"};
    /** 搜索引导词 */
    private static final String[] SEARCH_PREFIXES = {"帮我找", "帮我查", "查找", "搜索", "找一下", "找", "查询", "有没有"};
    /** 文件查询关键词 */
    private static final String[] FILE_SEARCH_KEYS = {"文件", "上传", "最近", "最新", "刚刚", "文档", "图片", "视频", "表格", "pdf",
        "word", "excel", "压缩包", "下载", "有哪些", "找", "查", "搜索", "今天", "昨天", "本周", "本月", "这周", "这月", "上周", "上月"};
    /** 文件大小查询关键词 */
    private static final String[] SIZE_KEYS =
        {"超过", "大于", "小于", "不低于", "不高于", "高于", "低于", "MB", "GB", "KB", "大文件", "小文件", "大小", "多大", "多少M", "多少G"};
    /** 存储分析查询关键词 */
    private static final String[] STORAGE_ANALYSIS_KEYS =
        {"存储使用", "空间占用", "空间使用", "存储情况", "用了多少", "还剩多少", "容量统计", "分布", "按类型", "统计", "总计", "占用最多", "使用情况"};
    /** 文件加密意图关键词 */
    private static final String[] ENCRYPTION_KEYS = {"加密", "设置密码", "加密码", "保护", "设密码"};
    /** 文件分析意图关键词 */
    private static final String[] ANALYSIS_KEYS = {"提取内容", "提取文本", "提取文字", "提取里面的", "提取里面", "提取图片", "提取出来", "识别文字",
        "识别图片", "文字识别", "OCR", "分析", "摘要", "总结", "关键词", "实体识别"};

    /** OCR 引擎安装指引（v-html 渲染，直接使用 HTML 链接确保在任何情况下都可点击） */
    private static final String OCR_INSTALL_GUIDE = "" + "<br/><br/><strong>📦 Tesseract OCR 安装指引</strong><br/>"
        + "提取图片文字需要服务器安装 Tesseract OCR 引擎，请联系管理员安装：<br/><br/>"
        + "🔹 <strong>Windows</strong>：<a href=\"https://github.com/UB-Mannheim/tesseract/wiki\" target=\"_blank\">Tesseract for Windows 下载页面</a><br/>"
        + "🔹 <strong>Linux (Ubuntu/Debian)</strong>：<code>sudo apt install tesseract-ocr tesseract-ocr-chi-sim</code><br/>"
        + "🔹 <strong>Linux (CentOS/RHEL)</strong>：<code>sudo yum install tesseract tesseract-langpack-chi-sim</code><br/>"
        + "🔹 <strong>macOS</strong>：<code>brew install tesseract</code><br/><br/>"
        + "📖 详细安装文档：<a href=\"https://tesseract-ocr.github.io/tessdoc/Installation.html\" target=\"_blank\">Tesseract 官方安装指南</a>";

    private static final String OCR_INSTALL_TIP =
        "（需安装 <a href=\"https://github.com/UB-Mannheim/tesseract/wiki\" target=\"_blank\">Tesseract OCR</a>）";
    /** 文件操作意图关键词 */
    private static final String[] CREATE_FOLDER_KEYS = {"新建文件夹", "创建文件夹", "新建目录", "创建目录", "新建一个文件夹", "创建名为", "建立一个文件夹",
        "建立个文件夹", "创建个文件夹", "建一个文件夹", "新建个文件夹", "建立文件夹", "建个文件夹", "建立一个目录", "创建新文件夹"};
    private static final String[] RENAME_KEYS = {"重命名", "改名", "修改名称", "改名为", "重命名为", "修改文件名为", "修改文件名称为", "修改名称为",
        "名称为", "名称改为", "文件名称改为", "文件重命名为", "改名称为", "名称修改为", "命名为", "更名"};
    private static final String[] DELETE_KEYS = {"删除", "移除", "彻底删除", "物理删除"};
    private static final String[] RESTORE_KEYS = {"恢复", "还原", "找回", "从回收站还原"};
    private static final String[] MOVE_KEYS = {"移动", "移到", "移入", "移动到", "移入到", "转移到", "搬移到"};
    private static final String[] SHARE_KEYS = {"分享", "共享", "生成分享链接", "创建分享"};
    private static final String[] UNSHARE_KEYS = {"取消分享", "取消共享", "关闭分享"};
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FileNodeRepository fileNodeRepository;
    private final AiApiClient aiApiClient;
    private final AiProperties aiProperties;
    private final Y9FileStoreService y9FileStoreService;
    @Autowired
    @Lazy
    private FileNodeService fileNodeService;

    @Autowired
    @Lazy
    private FileNodeShareService fileNodeShareService;

    @Value("${y9.common.storageBaseUrl}")
    private String storageBaseUrl;

    // ==================== 文件查询意图识别 ====================
    /** 缓存最近一次搜索结果，用于序号引用 */
    private List<Map<String, Object>> lastSearchResults = null;

    @Override
    public Map<String, Object> aiSearch(String query, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(query)) {
            result.put("success", false);
            result.put("msg", "请输入搜索内容");
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            return result;
        }

        String personId = Y9LoginUserHolder.getPersonId();
        LOGGER.info("AI搜索请求, personId={}, query={}", personId, query);

        // 解析自然语言搜索意图
        SearchIntent intent = parseSearchIntent(query);
        LOGGER.info("AI搜索解析结果, keyword={}, timeOrder={}, fileType={}, startDate={}, endDate={}, minSize={}, maxSize={}",
            intent.keyword, intent.timeOrder, intent.fileType, intent.startDate, intent.endDate, intent.minSize,
            intent.maxSize);

        Page<FileNode> filePage;
        Sort.Direction direction = "old".equals(intent.timeOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(direction, "createTime"));

        if (StringUtils.isNotBlank(intent.keyword)) {
            // 提取到关键字，按文件名模糊搜索
            FileNodeSpecification spec;
            if (intent.startDate != null && intent.endDate != null) {
                spec = new FileNodeSpecification(personId, (String)null, intent.keyword, false, intent.startDate,
                    intent.endDate, intent.minSize, intent.maxSize);
            } else {
                spec = new FileNodeSpecification(personId, (String)null, intent.keyword, false, null, null,
                    intent.minSize, intent.maxSize);
            }
            filePage = fileNodeRepository.findAll(spec, pageRequest);
        } else if (intent.startDate != null && intent.endDate != null) {
            // 纯日期意图（如"今天上传的文件"），查询该用户该日期范围内所有未删除文件
            FileNodeSpecification spec = new FileNodeSpecification(personId, (String)null, (String)null, false,
                intent.startDate, intent.endDate, intent.minSize, intent.maxSize);
            filePage = fileNodeRepository.findAll(spec, pageRequest);
        } else if ("recent".equals(intent.timeOrder) || "old".equals(intent.timeOrder)) {
            // 纯时间意图（如"最近上传的文件"），查询该用户所有未删除文件，按时间排序
            FileNodeSpecification spec = new FileNodeSpecification(personId, (String)null, (String)null, false, null,
                null, intent.minSize, intent.maxSize);
            filePage = fileNodeRepository.findAll(spec, pageRequest);
        } else {
            // 兜底：如果有大小/类型等结构化约束，不使用原始query当关键字；否则用原始query模糊搜索
            boolean hasConstraints =
                intent.minSize != null || intent.maxSize != null || StringUtils.isNotBlank(intent.fileType);
            String keyword = hasConstraints ? null : query;
            FileNodeSpecification spec = new FileNodeSpecification(personId, (String)null, keyword, false, null, null,
                intent.minSize, intent.maxSize);
            filePage = fileNodeRepository.findAll(spec, pageRequest);
        }

        LOGGER.info("AI搜索查询结果, total={}, page={}", filePage.getTotalElements(), filePage.getNumber());

        // 如果指定了文件类型，在结果中进行过滤
        List<Map<String, Object>> items = new ArrayList<>();
        for (FileNode fn : filePage.getContent()) {
            if (StringUtils.isNotBlank(intent.fileType) && !matchFileType(fn.getFileSuffix(), intent.fileType)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", fn.getId());
            item.put("name", fn.getName());
            item.put("fileSuffix", fn.getFileSuffix());
            item.put("fileSize", fn.getFileSize());
            item.put("createTime", fn.getCreateTime());
            item.put("parentId", fn.getParentId());
            item.put("fileStoreId", fn.getFileStoreId());
            items.add(item);
        }
        result.put("success", true);
        result.put("list", items);
        result.put("total", filePage.getTotalElements());
        result.put("totalPages", filePage.getTotalPages());
        result.put("keyword", intent.keyword);
        result.put("timeOrder", intent.timeOrder);
        result.put("fileType", intent.fileType);
        result.put("startDate", intent.startDate);
        result.put("endDate", intent.endDate);
        result.put("minSize", intent.minSize);
        result.put("maxSize", intent.maxSize);
        return result;
    }

    /**
     * 判断用户消息是否为文件查询意图
     */
    private boolean isFileSearchQuery(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        // 1. 明确搜索前缀是强信号：帮我找/查找/搜索/找一下/查询/有没有
        for (String prefix : SEARCH_PREFIXES) {
            if (message.contains(prefix)) {
                return true;
            }
        }

        // 2. 时间词 + 上传/文件 的组合（如"最近上传了什么""今天上传了什么文件"）
        String[] timeWords = {"最近", "最新", "今天", "昨天", "本周", "本月", "这周", "这月", "上周", "上月", "刚刚"};
        boolean hasTimeWord = false;
        for (String t : timeWords) {
            if (message.contains(t)) {
                hasTimeWord = true;
                break;
            }
        }
        if (hasTimeWord && (message.contains("上传") || message.contains("文件"))) {
            return true;
        }

        // 3. 明确的文件类型查询（"有哪些pdf""有没有word文件"之类——"有哪些"单独出现不算）
        String[] strongFileTypePatterns =
            {"pdf", "word", "excel", "压缩包", "图片文件", "视频文件", "有哪些文件", "有没有文件", "有没有pdf", "有没有word", "有哪些pdf", "有哪些word"};
        for (String p : strongFileTypePatterns) {
            if (message.contains(p)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断用户消息是否为文件加密意图
     */
    private boolean isEncryptionIntent(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        for (String key : ENCRYPTION_KEYS) {
            if (message.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户消息是否为文件分析意图
     * <p>
     * 除了预定义关键词的连续子串匹配，还会检测"提取/识别 + 文字/内容/文本"的组合出现， 以处理"提取图片里面的文字"这类非连续表述。
     */
    private boolean isAnalysisIntent(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        for (String key : ANALYSIS_KEYS) {
            if (message.contains(key)) {
                return true;
            }
        }
        // 组合匹配：提取/识别 与 文字/内容/文本 同时出现（不必连续）
        boolean hasExtract = message.contains("提取") || message.contains("识别");
        boolean hasTarget =
            message.contains("文字") || message.contains("内容") || message.contains("文本") || message.contains("里面");
        if (hasExtract && hasTarget) {
            return true;
        }
        return false;
    }

    /**
     * 判断用户消息是否为存储分析意图
     */
    private boolean isStorageAnalysisQuery(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        for (String key : STORAGE_ANALYSIS_KEYS) {
            if (message.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户消息是否包含文件大小比较意图
     */
    private boolean isSizeQuery(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        for (String key : SIZE_KEYS) {
            if (message.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /** 判断是否创建文件夹意图 */
    private boolean isCreateFolderIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : CREATE_FOLDER_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /** 判断是否重命名意图 */
    private boolean isRenameIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : RENAME_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /** 判断是否删除意图 */
    private boolean isDeleteIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : DELETE_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /** 判断是否恢复意图 */
    private boolean isRestoreIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : RESTORE_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /** 判断是否移动意图 */
    private boolean isMoveIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : MOVE_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /** 判断是否分享意图 */
    private boolean isShareIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : SHARE_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /** 判断是否取消分享意图 */
    private boolean isUnshareIntent(String message) {
        if (StringUtils.isBlank(message))
            return false;
        for (String key : UNSHARE_KEYS) {
            if (message.contains(key))
                return true;
        }
        return false;
    }

    /**
     * 检测消息中的文件ID引用（支持 "分析第1个" / "下载第2个" 等序号引用） 返回最近一次搜索结果的第n个文件，格式为 [fileId, fileName]
     */
    private String[] resolveFileReference(String message) {
        // 检测序号引用：第N个 / 第N / 全部
        int fileIndex = -1;
        boolean downloadAll = message.contains("全部") || message.contains("所有");
        for (int i = 1; i <= 10; i++) {
            if (message.contains("第" + i + "个") || message.contains("第" + i)) {
                fileIndex = i;
                break;
            }
        }
        if (fileIndex < 1 && !downloadAll) {
            return null;
        }

        // 从最后一次搜索结果缓存中获取
        if (lastSearchResults == null || lastSearchResults.isEmpty()) {
            return null;
        }
        if (downloadAll) {
            return new String[] {"ALL", String.valueOf(lastSearchResults.size())};
        }
        if (fileIndex > lastSearchResults.size()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> item = (Map<String, Object>)lastSearchResults.get(fileIndex - 1);
        return new String[] {(String)item.get("id"), (String)item.get("name"), (String)item.get("fileSuffix")};
    }

    /**
     * 解析消息中的文件序号（如 "第3个" → 3），-1表示未找到
     */
    private int resolveFileIndex(String message) {
        for (int i = 1; i <= 10; i++) {
            if (message.contains("第" + i + "个") || message.contains("第" + i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 在对话中处理文件查询：调用 aiSearch 获取结果并格式化为自然语言回复
     */
    private String handleFileSearchInChat(String message) {
        Map<String, Object> searchResult = aiSearch(message, 1, 10);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fileList = (List<Map<String, Object>>)searchResult.get("list");
        long total = (long)searchResult.getOrDefault("total", 0);

        // 缓存搜索结果，供后续"下载第1个"等序号引用
        lastSearchResults = fileList;

        if (fileList == null || fileList.isEmpty()) {
            StringBuilder noResult = new StringBuilder();
            noResult.append("我在您的网盘中没有找到相关文件。");
            // 如果指定了日期范围或大小约束，提示用户系统理解的条件
            Object searchedTimeOrder = searchResult.get("timeOrder");
            Object searchedStartDate = searchResult.get("startDate");
            Object searchedEndDate = searchResult.get("endDate");
            if (searchedTimeOrder != null && !"null".equals(String.valueOf(searchedTimeOrder))) {
                noResult.append("\n> 已搜索范围：").append(searchedTimeOrder).append("的文件");
            }
            if (searchedStartDate != null && searchedEndDate != null) {
                noResult.append("\n> 日期范围：").append(searchedStartDate).append(" ~ ").append(searchedEndDate);
            }
            Object minSizeObj = searchResult.get("minSize");
            Object maxSizeObj = searchResult.get("maxSize");
            if (minSizeObj != null) {
                noResult.append("\n> 文件大小 >= ").append(formatFileSize((Long)minSizeObj));
            }
            if (maxSizeObj != null) {
                noResult.append("\n> 文件大小 <= ").append(formatFileSize((Long)maxSizeObj));
            }
            noResult.append("\n\n您可以尝试：\n");
            noResult.append("1. 上传文件到网盘\n");
            noResult.append("2. 检查文件名是否正确\n");
            noResult.append("3. 输入 **\"上传文件\"** 开始上传\n");
            noResult.append("4. 输入 **\"最近上传了什么\"** 查看近期文件");
            return noResult.toString();
        }

        StringBuilder reply = new StringBuilder();
        reply.append("为您找到以下文件（共").append(total).append("个）");
        // 提示当前筛选条件
        Object minSizeObj = searchResult.get("minSize");
        Object maxSizeObj = searchResult.get("maxSize");
        if (minSizeObj != null && maxSizeObj != null) {
            reply.append("，大小 ")
                .append(formatFileSize((Long)minSizeObj))
                .append(" ~ ")
                .append(formatFileSize((Long)maxSizeObj));
        } else if (minSizeObj != null) {
            reply.append("，>= ").append(formatFileSize((Long)minSizeObj));
        } else if (maxSizeObj != null) {
            reply.append("，<= ").append(formatFileSize((Long)maxSizeObj));
        }
        reply.append("：\n\n");
        int index = 1;
        for (Map<String, Object> item : fileList) {
            String name = (String)item.getOrDefault("name", "未知文件");
            String id = (String)item.get("id");
            Object createTime = item.get("createTime");
            Object fileSize = item.get("fileSize");
            String fileSuffix = (String)item.get("fileSuffix");

            reply.append(index++).append(". **").append(name).append("**");
            if (fileSuffix != null) {
                reply.append(" (.").append(fileSuffix).append(")");
            }
            if (fileSize != null) {
                reply.append(" ").append(formatFileSize((Long)fileSize));
            }
            if (createTime != null) {
                reply.append(" - 上传于 ").append(createTime);
            }
            reply.append("\n");
            if (id != null) {
                String downloadUrl = storageBaseUrl + "/vue/fileNode/downloadFile?ids=" + id;
                reply.append("   > <a href=\"").append(downloadUrl).append("\" target=\"_blank\">下载</a>\n");
            }
            reply.append("\n");
        }
        if (total > fileList.size()) {
            reply.append("... 更多文件请前往网盘查看，或缩小搜索范围（如”今天上传的文档“）。");
        } else {
            reply.append("---\n");
            reply.append("您可以对我说：\n");
            reply.append("- **\"下载第1个\"** / **\"下载全部\"** → 获取下载链接\n");
            reply.append("- **\"分析第2个的内容\"** → AI 提取文件摘要\n");
            reply.append("- **\"加密第3个\"** → 为文件设置密码保护\n");
        }
        return reply.toString();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0)
            return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    // ==================== 新增功能处理 ====================

    /**
     * 处理加密意图
     */
    private String handleEncryptionIntent(String message) {
        // 先搜索匹配的文件
        List<Map<String, Object>> files = getEncryptableFiles("", 10);
        if (files.isEmpty()) {
            return "您的网盘中暂时没有可以加密的文件。请先上传文件。\n\n文件加密功能：" + "可以为文件设置链接密码，只有输入正确密码才能访问或下载。";
        }

        StringBuilder reply = new StringBuilder();
        reply.append("找到以下可以加密的文件：\n\n");
        int index = 1;
        for (Map<String, Object> f : files) {
            Boolean encrypted = (Boolean)f.get("encryption");
            reply.append(index++).append(". **").append(f.get("name")).append("**");
            if (f.get("fileSuffix") != null) {
                reply.append(" (.").append(f.get("fileSuffix")).append(")");
            }
            reply.append(encrypted != null && encrypted ? " [已加密]" : " [未加密]");
            reply.append("\n");
        }
        reply.append("\n您可以对我说：\n");
        reply.append("- **\"加密第1个，密码设为123456\"** → 为该文件设置访问密码\n");
        reply.append("- **\"取消加密第2个\"** → 移除密码保护\n");
        return reply.toString();
    }

    /**
     * 处理分析意图
     */
    private String handleAnalysisIntent(String message) {
        List<Map<String, Object>> files = getEncryptableFiles("", 10);
        if (files.isEmpty()) {
            return "您的网盘中暂时没有文件可供分析。请先上传文本类文件（如 .txt, .docx, .pdf 等）。\n\n" + "文件分析功能：AI 可以提取文件摘要、关键词、实体信息等。";
        }

        StringBuilder reply = new StringBuilder();
        reply.append("以下文件可供 AI 分析：\n\n");
        int index = 1;
        for (Map<String, Object> f : files) {
            reply.append(index++).append(". **").append(f.get("name")).append("**");
            if (f.get("fileSuffix") != null) {
                reply.append(" (.").append(f.get("fileSuffix")).append(")");
            }
            reply.append("\n");
        }
        reply.append("\n您可以对我说：\n");
        reply.append("- **\"分析第1个的内容\"** → 提取文件摘要\n");
        reply.append("- **\"第2个讲了什么？\"** → 文档问答\n");
        reply.append("- **\"提取第3个的关键词\"** → 提取关键词\n");
        return reply.toString();
    }

    /**
     * 处理存储使用分析查询："帮我分析最近的存储使用情况" / "空间占用" / "哪些类型文件占用最多"
     */
    private String handleStorageAnalysis(String message) {
        String personId = Y9LoginUserHolder.getPersonId();

        // 解析时间范围
        SearchIntent dateIntent = new SearchIntent();
        parseDateIntent(message, dateIntent);

        // 查询用户所有未删除文件
        FileNodeSpecification spec;
        if (dateIntent.startDate != null && dateIntent.endDate != null) {
            spec = new FileNodeSpecification(personId, (String)null, (String)null, false, dateIntent.startDate,
                dateIntent.endDate);
        } else {
            spec = new FileNodeSpecification(personId, (String)null, (String)null, false);
        }
        List<FileNode> allFiles = fileNodeRepository.findAll(spec);

        if (allFiles.isEmpty()) {
            String timeHint = dateIntent.timeOrder != null ? "（范围：" + dateIntent.timeOrder + "）" : "";
            return "您的网盘中暂未找到文件" + timeHint + "。请先上传文件。";
        }

        // 聚合统计
        long totalCount = allFiles.size();
        long totalSize = allFiles.stream().mapToLong(f -> f.getFileSize() != null ? f.getFileSize() : 0).sum();

        // 按文件类型分组统计
        Map<String, long[]> typeStats = new LinkedHashMap<>(); // [count, totalSize]
        for (FileNode f : allFiles) {
            String type = StringUtils.isNotBlank(f.getFileSuffix()) ? f.getFileSuffix().toLowerCase() : "其他";
            typeStats.computeIfAbsent(type, k -> new long[2]);
            typeStats.get(type)[0]++;
            typeStats.get(type)[1] += (f.getFileSize() != null ? f.getFileSize() : 0);
        }

        // 按占用空间排序（Top 5）
        List<Map.Entry<String, long[]>> sortedTypes = new ArrayList<>(typeStats.entrySet());
        sortedTypes.sort((a, b) -> Long.compare(b.getValue()[1], a.getValue()[1]));
        List<Map.Entry<String, long[]>> topTypes = sortedTypes.subList(0, Math.min(5, sortedTypes.size()));

        // 大文件统计（>10MB 和 >100MB）
        long largeFile10M =
            allFiles.stream().filter(f -> f.getFileSize() != null && f.getFileSize() > 10 * 1024 * 1024).count();
        long largeFile100M =
            allFiles.stream().filter(f -> f.getFileSize() != null && f.getFileSize() > 100 * 1024 * 1024).count();

        // 构建回复
        StringBuilder reply = new StringBuilder();
        reply.append("**存储使用分析**");
        if (dateIntent.timeOrder != null) {
            reply.append("（范围：").append(dateIntent.timeOrder).append("）");
        }
        reply.append("\n\n");

        reply.append("> **文件总数**：").append(totalCount).append(" 个\n");
        reply.append("> **总占用空间**：").append(formatFileSize(totalSize)).append("\n");
        reply.append("> **平均文件大小**：")
            .append(totalCount > 0 ? formatFileSize(totalSize / totalCount) : "0 B")
            .append("\n");
        reply.append("> **大文件（>10MB）**：").append(largeFile10M).append(" 个\n");
        reply.append("> **超大文件（>100MB）**：").append(largeFile100M).append(" 个\n\n");

        reply.append("**按类型分布**（Top ").append(topTypes.size()).append("）：\n");
        int rank = 1;
        for (Map.Entry<String, long[]> entry : topTypes) {
            String typeName = entry.getKey();
            long count = entry.getValue()[0];
            long size = entry.getValue()[1];
            reply.append(rank++)
                .append(". **.")
                .append(typeName)
                .append("** — ")
                .append(count)
                .append(" 个，占用 ")
                .append(formatFileSize(size));
            double pct = totalSize > 0 ? (size * 100.0 / totalSize) : 0;
            reply.append("（").append(String.format("%.1f", pct)).append("%）\n");
        }

        reply.append("\n---\n您可以对我说：\n");
        reply.append("- **\"超过100MB的文件有哪些\"** → 查看大文件\n");
        reply.append("- **\"查找包含'关键词'的文档\"** → 按文件名搜索\n");
        reply.append("- **\"下载第1个\"** → 获取下载链接");

        return reply.toString();
    }

    // ==================== 文件操作处理器 ====================

    /**
     * 处理创建文件夹："新建一个文件夹叫项目文档" / "创建文件夹：资料"
     */
    private String handleCreateFolder(String message) {
        String personId = Y9LoginUserHolder.getPersonId();
        // 提取文件夹名称：在"叫"/"名为"/"："之后的文本，或引号内容
        String folderName = null;
        // 尝试提取引号内名称
        folderName = extractQuotedKeyword(message);
        if (StringUtils.isBlank(folderName)) {
            // 匹配 "叫XXX" / "名为XXX" / "：XXX"
            Pattern pattern = Pattern.compile("(?:叫|名为|命名|：|:)\\s*(.+?)(?:$|，|。|！|？|\\s$)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                folderName = matcher.group(1).trim();
                // 去除尾部标点
                folderName = folderName.replaceAll("[，。！？、；:：\\s]+$", "");
            }
        }
        if (StringUtils.isBlank(folderName)) {
            // 去掉意图关键词后剩余当作文件夹名
            folderName = message;
            for (String key : CREATE_FOLDER_KEYS) {
                folderName = folderName.replace(key, "");
            }
            folderName = folderName.replaceAll("[，。！？、；:：\\s]+", "").trim();
        }
        if (StringUtils.isBlank(folderName) || folderName.length() > 50) {
            return "请告诉我文件夹的名称，例如：**\"新建文件夹叫项目资料\"**";
        }

        try {
            FileNode folder = new FileNode();
            folder.setName(folderName);
            folder.setUserId(personId);
            folder.setParentId("my"); // 根目录
            folder.setFileType(0); // 0=文件夹
            folder.setListType("my"); // 个人文件
            folder.setDeleted(false);
            folder.setCreateTime(new Date());
            folder.setUpdateTime(new Date());
            folder.setTabIndex(0);
            fileNodeService.saveFolder(folder);
            LOGGER.info("AI 创建文件夹成功, personId={}, folderName={}", personId, folderName);
            return "已为您创建文件夹 **" + folderName + "**，您可以在网盘根目录中找到。";
        } catch (Exception e) {
            LOGGER.error("AI 创建文件夹失败", e);
            return "创建文件夹失败：" + e.getMessage() + "。请检查是否有同名文件夹。";
        }
    }

    /**
     * 处理重命名："把第1个文件改名为测试报告"
     */
    private String handleRenameFile(String message, String[] fileRef) {
        String fileId = fileRef[0];
        String oldName = fileRef.length > 1 ? fileRef[1] : "未知文件";

        // 提取新名称：先去掉文件引用（「旧名」等），避免误取旧文件名
        String cleanMessage = message.replaceAll(
            "(?:文件|文档|图片|附件)\\s*[\u300c\u300e\u201c\"\\[]([^\u300d\u300f\u201d\"\\]]+)[\u300d\u300f\u201d\"\\]]", "")
            .replaceAll("[\u300c\u300e]([^\u300d\u300f]+)[\u300d\u300f]", "")
            .trim();
        String newName = extractQuotedKeyword(cleanMessage);
        if (StringUtils.isBlank(newName)) {
            Pattern pattern = Pattern.compile(
                "(?:修改文件名称为|修改名称为|重命名文件为|文件重命名为|文件名称改为|修改文件名为|改名为|重命名为|名称为|名称改为|命名为|改为|改成|名为|叫)\\s*(.+?)(?:$|，|。|！|？)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                newName = matcher.group(1).trim();
            }
        }
        if (StringUtils.isBlank(newName)) {
            return "请指定新的名称，例如：**\"把第1个改名为测试报告\"**";
        }
        // 兜底：去除可能残留的 HTML 实体（如 &ldquo; &rdquo; &quot; &lt; &gt;）
        newName = org.springframework.web.util.HtmlUtils.htmlUnescape(newName);
        // 去掉引号本身（中文引号、直角引号、英文引号），确保文件名干净
        newName = newName.replaceAll("[\u201c\u201d\u2018\u2019\u300c\u300d\u300e\u300f\u300a\u300b\"']", "").trim();
        if (StringUtils.isBlank(newName)) {
            return "请指定新的名称，例如：**\"把第1个改名为测试报告\"**";
        }
        if (newName.length() > 100) {
            return "文件名过长，请控制在100个字符以内。";
        }

        try {
            FileNode fileNode = fileNodeRepository.findById(fileId).orElse(null);
            if (fileNode == null) {
                return "文件不存在，请重新搜索后再试。";
            }
            fileNode.setName(newName);
            fileNode.setUpdateTime(new Date());
            if (fileNode.getFileType() != null && fileNode.getFileType() == 0) {
                fileNodeService.saveFolder(fileNode);
            } else {
                fileNodeService.saveNode(fileNode);
            }
            LOGGER.info("AI 重命名成功, fileId={}, oldName={}, newName={}", fileId, oldName, newName);
            return "已将 **" + oldName + "** 重命名为 **" + newName + "**。";
        } catch (Exception e) {
            LOGGER.error("AI 重命名失败", e);
            return "重命名失败：" + e.getMessage();
        }
    }

    /**
     * 处理删除文件（逻辑删除）："删除第2个文件"
     */
    private String handleDeleteFiles(String message) {
        // 先检查是否有搜索结果缓存
        String[] fileRef = resolveFileReference(message);
        if (fileRef != null) {
            List<String> ids = new ArrayList<>();
            ids.add(fileRef[0]);
            String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";
            try {
                fileNodeService.logicDelete(ids);
                LOGGER.info("AI 逻辑删除文件, fileId={}, fileName={}", fileRef[0], fileName);
                return "已将 **" + fileName + "** 移入回收站。如需恢复，可以对我说\"恢复最近删除的文件\"。";
            } catch (Exception e) {
                LOGGER.error("AI 删除文件失败", e);
                return "删除失败：" + e.getMessage();
            }
        }
        // 没有序号引用，提示用户先搜索
        return "请先搜索文件，然后对我说：**\"删除第1个\"**。\n\n" + "您也可以对我说：\n" + "- **\"帮我找文件\"** → 先搜索文件\n"
            + "- **\"查看回收站\"** → 查看已删除文件";
    }

    /**
     * 处理 @ 提及文件的删除：直接通过文件ID逻辑删除
     */
    private String handleMentionedFileDelete(List<Map<String, Object>> mentionedFiles) {
        List<String> ids = new ArrayList<>();
        StringBuilder nameBuilder = new StringBuilder();
        for (Map<String, Object> fileInfo : mentionedFiles) {
            String fileId = (String)fileInfo.get("id");
            // 前端可能用 "name" 或 "fileName" 传递文件名，都尝试获取
            String fileName = (String)fileInfo.get("name");
            if (fileName == null) {
                fileName = (String)fileInfo.get("fileName");
            }
            // 如果仍为空，从数据库查询文件名
            if (fileName == null && StringUtils.isNotBlank(fileId)) {
                try {
                    FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                    if (fn != null) {
                        fileName = fn.getName();
                    }
                } catch (Exception e) {
                    LOGGER.debug("查询@提及文件名失败: fileId={}", fileId, e);
                }
            }
            if (StringUtils.isNotBlank(fileId)) {
                ids.add(fileId);
                if (nameBuilder.length() > 0) {
                    nameBuilder.append("、");
                }
                nameBuilder.append(fileName != null ? fileName : "未知文件");
            }
        }
        if (ids.isEmpty()) {
            return "未找到可删除的文件信息。";
        }
        try {
            fileNodeService.logicDelete(ids);
            LOGGER.info("AI 通过@提及删除文件, fileIds={}", ids);
            return "已将 **" + nameBuilder.toString() + "** 移入回收站。如需恢复，可以对我说“恢复最近删除的文件”。";
        } catch (Exception e) {
            LOGGER.error("AI 通过@提及删除文件失败", e);
            return "删除失败：" + e.getMessage();
        }
    }

    /**
     * 处理 @删除文件夹/文件 名称 命令：按名称搜索并删除匹配的文件/文件夹
     * 例如 @删除文件夹 888 → 搜索名称含"888"的文件夹，执行逻辑删除
     */
    private String handleDeleteByName(String message) {
        String personId = Y9LoginUserHolder.getPersonId();
        // 尝试提取引号名称 "删除文件夹\"888\""
        String keyword = extractQuotedKeyword(message);
        if (StringUtils.isBlank(keyword)) {
            // 去掉意图关键词后剩余作为搜索关键词
            keyword = message;
            for (String key : DELETE_KEYS) {
                keyword = keyword.replace(key, "");
            }
            // 去除 "文件" "夹" 等修饰词
            keyword = keyword.replace("文件夹", "").replace("文件", "").trim();
            keyword = keyword.replaceAll("[，。！？、；:：\\s]+", "").trim();
        }
        if (StringUtils.isBlank(keyword) || keyword.length() > 50) {
            return "请输入要删除的文件或文件夹名称，例如：**\"@删除文件夹 888\"** 或 **\"@删除文件 报告\"**";
        }

        // 按名称搜索
        LOGGER.info("AI 按名称删除, personId={}, keyword={}", personId, keyword);
        FileNodeSpecification spec =
            new FileNodeSpecification(personId, (String)null, keyword, false, null, null, null, null);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FileNode> page = fileNodeRepository.findAll(spec, pageRequest);
        List<FileNode> items = page.getContent();

        if (items.isEmpty()) {
            return "未找到包含 **\"" + keyword + "\"** 的文件或文件夹。\n\n您可以尝试：\n"
                + "- **\"@帮我找 " + keyword + "\"** → 先搜索确认文件\n"
                + "- **\"@新建文件夹 " + keyword + "\"** → 创建新文件夹";
        }

        // 执行删除
        List<String> ids = new ArrayList<>();
        StringBuilder nameBuilder = new StringBuilder();
        for (FileNode fn : items) {
            ids.add(fn.getId());
            if (nameBuilder.length() > 0) {
                nameBuilder.append("、");
            }
            nameBuilder.append(fn.getName());
        }
        try {
            fileNodeService.logicDelete(ids);
            String fileOrFolder = items.get(0).getFileType() != null && items.get(0).getFileType() == 0 ? "文件夹" : "文件";
            LOGGER.info("AI 按名称删除成功, personId={}, keyword={}, count={}", personId, keyword, ids.size());
            return "已将匹配 **\"" + keyword + "\"** 的 " + ids.size() + " 个" + fileOrFolder + "（**" + nameBuilder.toString()
                + "**）移入回收站。如需恢复，可以对我说\"恢复最近删除的文件\"。";
        } catch (Exception e) {
            LOGGER.error("AI 按名称删除失败", e);
            return "删除失败：" + e.getMessage();
        }
    }

    /**
     * 处理 @ 提及文件的内容提取/分析："提取里面的内容" / "分析这个文件"
     */
    @SuppressWarnings("unchecked")
    private String handleMentionedFileAnalysis(List<Map<String, Object>> mentionedFiles, String message) {
        if (mentionedFiles == null || mentionedFiles.isEmpty()) {
            return "未找到可分析的文件。";
        }

        StringBuilder result = new StringBuilder();
        int successCount = 0;
        int failCount = 0;

        for (Map<String, Object> fileInfo : mentionedFiles) {
            String fileId = (String)fileInfo.get("id");
            String fileName = (String)fileInfo.get("name");
            if (fileName == null) {
                fileName = (String)fileInfo.get("fileName");
            }

            // 从数据库查询文件信息
            FileNode fn = null;
            if (StringUtils.isNotBlank(fileId)) {
                try {
                    fn = fileNodeRepository.findById(fileId).orElse(null);
                    if (fn != null && fileName == null) {
                        fileName = fn.getName();
                    }
                } catch (Exception e) {
                    LOGGER.debug("查询@提及文件详情失败: fileId={}", fileId, e);
                }
            }
            if (fileName == null) {
                fileName = "未知文件";
            }

            if (fn == null) {
                result.append("**").append(fileName).append("**: 未找到文件信息。\n\n");
                failCount++;
                continue;
            }

            String suffix = fn.getFileSuffix();
            if (!FileContentExtractor.isSupported(suffix)) {
                result.append("**")
                    .append(fileName)
                    .append("**: 无法提取内容，")
                    .append(StringUtils.defaultIfBlank(FileContentExtractor.getTypeName(suffix), suffix))
                    .append("类型暂不支持提取。\n\n");
                failCount++;
                continue;
            }

            String content = extractFileContent(fn);
            if (StringUtils.isNotBlank(content)) {
                result.append("## ").append(fileName).append("\n\n");
                int maxShow = Math.min(content.length(), 5000);
                result.append(content.substring(0, maxShow));
                if (content.length() > maxShow) {
                    result.append("\n\n*(内容已截断，共 ").append(content.length()).append(" 字符)*");
                }
                result.append("\n\n---\n\n");
                successCount++;
            } else {
                String typeName = FileContentExtractor.getTypeName(suffix);
                if ("图片".equals(typeName)) {
                    result.append("**").append(fileName).append("**: 图片文字识别失败，请检查 Tesseract OCR 环境是否正常。</br>\n\n");
                } else {
                    result.append("**").append(fileName).append("**: 提取内容失败，文件可能为空或格式异常。\n\n");
                }
                failCount++;
            }
        }

        if (successCount == 0 && failCount > 0) {
            result.append("> 提示：目前支持提取以下类型文件的内容：\n")
                .append("> - 纯文本：txt, md, csv, json, xml, log 等\n")
                .append("> - Word 文档：docx, doc\n")
                .append("> - Excel 表格：xlsx, xls\n")
                .append("> - 图片：jpg, png 等")
                .append(OCR_INSTALL_TIP)
                .append("\n");
            // 图片识别失败时追加 OCR 安装指引
            if (result.indexOf("图片文字识别失败") != -1) {
                result.append(OCR_INSTALL_GUIDE);
            }
        }

        return result.toString().trim();
    }

    /**
     * 处理恢复文件："恢复已删除的文件" / "还原第1个"
     */
    private String handleRestoreFiles(String message) {
        String personId = Y9LoginUserHolder.getPersonId();
        String[] fileRef = resolveFileReference(message);
        if (fileRef != null) {
            List<String> ids = new ArrayList<>();
            ids.add(fileRef[0]);
            String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";
            try {
                fileNodeService.restore(ids);
                LOGGER.info("AI 恢复文件, fileId={}, fileName={}", fileRef[0], fileName);
                return "已将 **" + fileName + "** 从回收站恢复。";
            } catch (Exception e) {
                LOGGER.error("AI 恢复文件失败", e);
                return "恢复失败：" + e.getMessage();
            }
        }
        // 列出回收站文件供用户选择
        try {
            List<FileNode> deletedFiles = fileNodeService.deletedList(personId);
            if (deletedFiles.isEmpty()) {
                return "回收站中没有文件。";
            }
            // 缓存到搜索结果供序号引用
            lastSearchResults = new ArrayList<>();
            StringBuilder reply = new StringBuilder();
            reply.append("回收站中的文件（共").append(deletedFiles.size()).append("个）：\n\n");
            int index = 1;
            for (FileNode fn : deletedFiles) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", fn.getId());
                item.put("name", fn.getName());
                item.put("fileSuffix", fn.getFileSuffix());
                item.put("fileSize", fn.getFileSize());
                item.put("createTime", fn.getCreateTime());
                lastSearchResults.add(item);
                reply.append(index++).append(". **").append(fn.getName()).append("**");
                if (StringUtils.isNotBlank(fn.getFileSuffix())) {
                    reply.append(" (.").append(fn.getFileSuffix()).append(")");
                }
                reply.append("\n");
            }
            reply.append("\n对我说：**\"恢复第1个\"** 即可还原对应文件。");
            return reply.toString();
        } catch (Exception e) {
            LOGGER.error("AI 获取回收站列表失败", e);
            return "获取回收站列表失败：" + e.getMessage();
        }
    }

    /**
     * 处理移动文件："把第1个文件移到项目资料文件夹"
     */
    private String handleMoveFile(String message, String[] fileRef) {
        String fileId = fileRef[0];
        String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";
        String personId = Y9LoginUserHolder.getPersonId();

        // 提取目标文件夹名称
        String targetFolderName = extractQuotedKeyword(message);
        if (StringUtils.isBlank(targetFolderName)) {
            Pattern pattern = Pattern.compile("(?:移到|移入|移动到|移入到|转移到|搬移到|到)\\s*(.+?)(?:$|，|。|！|？|\\s$)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                targetFolderName = matcher.group(1).trim();
                targetFolderName = targetFolderName.replaceAll("[，。！？、；:：\\s]+$", "");
            }
        }
        if (StringUtils.isBlank(targetFolderName)) {
            return "请指定目标文件夹，例如：**\"把第1个移到项目资料\"**";
        }

        try {
            // 搜索目标文件夹
            FileNodeSpecification spec = new FileNodeSpecification(personId, (String)null, targetFolderName, false);
            List<FileNode> folders = fileNodeRepository.findAll(spec);
            // 过滤出文件夹类型
            FileNode targetFolder = null;
            for (FileNode fn : folders) {
                if (fn.getFileType() != null && fn.getFileType() == 0 && targetFolderName.equals(fn.getName())) {
                    targetFolder = fn;
                    break;
                }
            }
            if (targetFolder == null && !folders.isEmpty()) {
                // 没找到精确匹配的文件夹名，找第一个文件夹类型的
                for (FileNode fn : folders) {
                    if (fn.getFileType() != null && fn.getFileType() == 0) {
                        targetFolder = fn;
                        break;
                    }
                }
            }
            if (targetFolder == null) {
                return "没有找到名为 **" + targetFolderName + "** 的文件夹。请先创建该文件夹，或确认文件夹名称是否正确。\n\n" + "对我说：**\"新建文件夹叫"
                    + targetFolderName + "\"** 即可创建。";
            }

            List<String> ids = new ArrayList<>();
            ids.add(fileId);
            fileNodeService.move(ids, targetFolder.getId());
            LOGGER.info("AI 移动文件成功, fileId={}, targetFolder={}", fileId, targetFolderName);
            return "已将 **" + fileName + "** 移动到 **" + targetFolderName + "** 文件夹中。";
        } catch (Exception e) {
            LOGGER.error("AI 移动文件失败", e);
            return "移动文件失败：" + e.getMessage();
        }
    }

    /**
     * 处理分享文件："分享第1个文件" / "生成第2个的分享链接"
     */
    private String handleShareFile(String message, String[] fileRef) {
        String fileId = fileRef[0];
        String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";

        try {
            List<FileNode> fileNodes = fileNodeRepository.findAllById(java.util.Collections.singletonList(fileId));
            if (fileNodes.isEmpty()) {
                return "文件不存在，请重新搜索后再试。";
            }
            FileNode fileNode = fileNodes.get(0);

            // 构建下载/预览链接
            String fileUrl = storageBaseUrl + "/vue/fileNode/downloadFile?ids=" + fileId;
            String previewUrl = storageBaseUrl + "/vue/fileNode/preview?id=" + fileId;

            // 如果有加密，提示
            StringBuilder reply = new StringBuilder();
            reply.append("文件 **").append(fileName).append("** 的分享信息：\n\n");
            reply.append("- <a href=\"").append(fileUrl).append("\" target=\"_blank\"><strong>下载文件</strong></a>\n");
            if (StringUtils.isNotBlank(fileNode.getFileSuffix())) {
                String ext = fileNode.getFileSuffix().toLowerCase();
                if (ext.matches("(jpg|jpeg|png|gif|bmp|webp|pdf|txt|mp4|mp3)")) {
                    reply.append("- <a href=\"")
                        .append(previewUrl)
                        .append("\" target=\"_blank\"><strong>预览文件</strong></a>\n");
                }
            }
            if (fileNode.getFileSize() != null) {
                reply.append("- **文件大小**：").append(formatFileSize(fileNode.getFileSize())).append("\n");
            }
            if (fileNode.isEncryption()) {
                reply.append("\n⚠ 此文件已设置密码保护，访问时需要输入密码。\n");
            }
            reply.append("\n您也可以对我说：\n");
            reply.append("- **\"加密第1个\"** → 为文件设置密码保护\n");
            reply.append("- **\"取消分享第1个\"** → 移除分享状态");
            return reply.toString();
        } catch (Exception e) {
            LOGGER.error("AI 分享文件失败", e);
            return "获取分享信息失败：" + e.getMessage();
        }
    }

    /**
     * 处理移动文件（无序号引用）：提示用户先搜索
     */
    private String handleMoveFilePrompt(String message) {
        return "请先搜索文件，然后对我说：**\"把第1个移到目标文件夹\"**。\n\n" + "您也可以对我说：\n" + "- **\"帮我找XXX文件\"** → 先搜索文件\n"
            + "- **\"新建文件夹叫XXX\"** → 创建新文件夹";
    }

    /**
     * 处理分享意图（无序号引用）：提示用户先搜索
     */
    private String handleSharePrompt(String message) {
        return "请先搜索文件，然后对我说：**\"分享第1个文件\"** 即可获取分享链接。\n\n" + "您也可以对我说：\n" + "- **\"帮我找文件\"** → 搜索需要分享的文件\n"
            + "- **\"加密文件\"** → 为文件设置密码保护";
    }

    /**
     * 处理文件下载（支持"下载第1个"和"下载全部"）
     */
    private String handleDownloadFiles(String message, String[] fileRef) {
        String fileId = fileRef[0];
        if ("ALL".equals(fileId)) {
            int count = Integer.parseInt(fileRef[1]);
            if (lastSearchResults == null || lastSearchResults.isEmpty()) {
                return "没有找到可下载的文件，请先搜索文件。";
            }
            StringBuilder reply = new StringBuilder();
            reply.append("以下文件的下载链接：\n\n");
            int index = 1;
            for (Map<String, Object> item : lastSearchResults) {
                String id = (String)item.get("id");
                String name = (String)item.getOrDefault("name", "未知");
                reply.append(index++).append(". **").append(name).append("**\n");
                String dlUrl = storageBaseUrl + "/vue/fileNode/downloadFile?ids=" + id;
                reply.append("   <a href=\"").append(dlUrl).append("\" target=\"_blank\">下载</a>\n\n");
            }
            return reply.toString();
        }

        String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";
        String dlUrl = storageBaseUrl + "/vue/fileNode/downloadFile?ids=" + fileId;
        return "文件 **" + fileName + "** 的下载链接：\n\n<a href=\"" + dlUrl + "\" target=\"_blank\">" + dlUrl
            + "</a>\n\n您也可以直接在网盘中右键文件选择下载。";
    }

    /**
     * 通过序号引用分析文件
     */
    private String handleFileAnalysisByIndex(String message, String[] fileRef) {
        String fileId = fileRef[0];
        String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";

        // 判断分析类型
        String analysisType = "summary";
        if (message.contains("关键词")) {
            analysisType = "keywords";
        } else if (message.contains("实体")) {
            analysisType = "entity";
        }

        Map<String, Object> analysisResult = analyzeFile(fileId, analysisType);
        if (!(Boolean)analysisResult.getOrDefault("success", false)) {
            return "分析失败：" + analysisResult.getOrDefault("msg", "未知错误");
        }
        return "**" + fileName + "** 的分析结果：\n\n" + analysisResult.getOrDefault("analysis", "无法完成分析");
    }

    /**
     * 通过序号引用加密文件
     */
    private String handleEncryptionByIndex(String message, String[] fileRef) {
        String fileId = fileRef[0];
        String fileName = fileRef.length > 1 ? fileRef[1] : "未知文件";

        // 检测是否取消加密
        if (message.contains("取消")) {
            Map<String, Object> result = setFileEncryption(fileId, "");
            return (Boolean)result.getOrDefault("success", false) ? "已取消 **" + fileName + "** 的密码保护。"
                : "操作失败：" + result.getOrDefault("msg", "");
        }

        // 从消息中提取密码（简单提取，如"密码设为123456"）
        String password = "auto_" + System.currentTimeMillis() % 100000;
        if (message.contains("密码") || message.contains("设为")) {
            String[] parts = message.split("[密码设为]+");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                if (part.length() >= 4 && part.length() <= 20 && part.matches(".*\\d+.*")) {
                    password = part.replaceAll("[^a-zA-Z0-9]", "");
                    if (password.length() >= 4) {
                        break;
                    }
                }
            }
        }

        Map<String, Object> result = setFileEncryption(fileId, password);
        return (Boolean)result.getOrDefault("success", false)
            ? "已为 **" + fileName + "** 设置密码保护。\n密码：`" + password + "`\n\n其他人访问此文件时需要输入该密码。"
            : "操作失败：" + result.getOrDefault("msg", "");
    }

    // ==================== 接口实现：文件提及搜索 ====================

    @Override
    public List<Map<String, Object>> searchFilesForMention(String keyword, int limit) {
        String personId = Y9LoginUserHolder.getPersonId();
        List<Map<String, Object>> result = new ArrayList<>();

        // 按文件名模糊搜索用户文件
        FileNodeSpecification spec = new FileNodeSpecification(personId, (String)null, keyword, false);
        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 20), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FileNode> filePage = fileNodeRepository.findAll(spec, pageRequest);

        for (FileNode fn : filePage.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fn.getId());
            item.put("name", fn.getName());
            item.put("fileSuffix", fn.getFileSuffix());
            item.put("fileSize", fn.getFileSize());
            item.put("createTime", fn.getCreateTime());
            result.add(item);
        }
        return result;
    }

    /**
     * 构建 @ 文件提及的上下文提示词
     * <p>
     * 从 context 中提取 mentionedFiles 列表，查询文件详情并构建系统提示， 使 AI 可以在回复中引用具体的文件信息。
     */
    @SuppressWarnings("unchecked")
    private String buildMentionedFilesPrompt(Map<String, Object> context, String userMessage) {
        if (context == null) {
            return null;
        }
        Object mentionedFiles = context.get("mentionedFiles");
        if (!(mentionedFiles instanceof List)) {
            return null;
        }
        List<Map<String, Object>> fileList = (List<Map<String, Object>>)mentionedFiles;
        if (fileList.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("用户通过 @ 提及了以下文件，请结合这些文件的内容来回答用户的问题：\n\n");
        sb.append("## 提及的文件\n");

        int idx = 1;
        for (Map<String, Object> fileInfo : fileList) {
            String fileId = (String)fileInfo.get("id");
            // 前端可能用 "name" 或 "fileName" 传递文件名，都尝试获取
            String fileName = (String)fileInfo.get("name");
            if (fileName == null) {
                fileName = (String)fileInfo.get("fileName");
            }

            // 如果仍为空，从数据库查询文件名
            if (fileName == null && StringUtils.isNotBlank(fileId)) {
                try {
                    FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                    if (fn != null) {
                        fileName = fn.getName();
                    }
                } catch (Exception e) {
                    LOGGER.debug("查询@提及文件名失败: fileId={}", fileId, e);
                }
            }
            // 最终兜底
            if (fileName == null) {
                fileName = "未知文件";
            }

            sb.append(idx).append(". **").append(fileName).append("**");

            // 尝试从数据库获取更详细的文件信息，并提取文件内容
            if (StringUtils.isNotBlank(fileId)) {
                String downloadLink = storageBaseUrl + "/vue/fileNode/downloadFile?ids=" + fileId;
                try {
                    FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                    if (fn != null) {
                        sb.append(" (大小: ")
                            .append(formatFileSize(fn.getFileSize()))
                            .append(", 类型: ")
                            .append(org.apache.commons.lang3.StringUtils.defaultString(fn.getFileSuffix(), "未知"))
                            .append(", 创建时间: ")
                            .append(fn.getCreateTime() != null
                                ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(fn.getCreateTime()) : "未知")
                            .append(")");
                        sb.append("\n   下载链接: [").append(fileName).append("](").append(downloadLink).append(")");

                        // 自动提取文件内容注入给 AI
                        String suffix = fn.getFileSuffix();
                        if (FileContentExtractor.isSupported(suffix)) {
                            String content = extractFileContent(fn);
                            if (StringUtils.isNotBlank(content)) {
                                int contentMax = aiProperties.getMaxContentLength() / 2;
                                String truncated = content.length() > contentMax
                                    ? content.substring(0, contentMax) + "\n...(内容已截断)" : content;
                                sb.append("\n\n   **文件内容**:\n```\n").append(truncated).append("\n```\n");
                            } else {
                                sb.append("\n   *（无法提取此文件内容，类型: ")
                                    .append(FileContentExtractor.getTypeName(suffix))
                                    .append("）*\n");
                            }
                        } else if (StringUtils.isNotBlank(suffix)) {
                            sb.append("\n   *（")
                                .append(StringUtils.defaultIfBlank(FileContentExtractor.getTypeName(suffix), suffix))
                                .append("类型暂不支持提取内容）*\n");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.debug("查询提及文件详情失败: {}", fileId, e);
                }
            }
            sb.append("\n");
            idx++;
        }
        sb.append("请在回答中引用这些文件时，直接使用上面提供的下载链接，方便用户点击下载。\n");
        return sb.toString();
    }

    // ==================== 接口实现：文件加密 ====================

    @Override
    public Map<String, Object> setFileEncryption(String fileId, String password) {
        Map<String, Object> result = new HashMap<>();
        try {
            FileNode fileNode = fileNodeRepository.findById(fileId).orElse(null);
            if (fileNode == null) {
                result.put("success", false);
                result.put("msg", "文件不存在");
                return result;
            }
            boolean enableEncryption = StringUtils.isNotBlank(password);
            net.risesoft.pojo.Y9Result<Object> opResult =
                fileNodeService.setLinkPwd(fileId, enableEncryption, enableEncryption ? password : "");
            result.put("success", opResult.isSuccess());
            result.put("msg", opResult.getMsg());
            result.put("fileName", fileNode.getName());
        } catch (Exception e) {
            LOGGER.error("设置文件加密失败", e);
            result.put("success", false);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getEncryptableFiles(String keyword, int limit) {
        String personId = Y9LoginUserHolder.getPersonId();
        List<Map<String, Object>> result = new ArrayList<>();

        FileNodeSpecification spec = new FileNodeSpecification(personId, (String)null, keyword, false);
        PageRequest pageRequest = PageRequest.of(0, Math.min(limit, 20), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FileNode> filePage = fileNodeRepository.findAll(spec, pageRequest);

        for (FileNode fn : filePage.getContent()) {
            if (fn.getFileType() != null && fn.getFileType() == 0) {
                continue; // 跳过文件夹
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", fn.getId());
            item.put("name", fn.getName());
            item.put("fileSuffix", fn.getFileSuffix());
            item.put("fileSize", fn.getFileSize());
            item.put("createTime", fn.getCreateTime());
            item.put("encryption", fn.isEncryption());
            result.add(item);
        }
        return result;
    }

    /**
     * 解析文件大小约束："超过100MB" → minSize=100MB，"小于500KB" → maxSize=500KB
     */
    private void parseSizeConstraint(String content, SearchIntent intent) {
        // 匹配模式：比较词 + 数字 + 可选空格 + 单位
        Pattern pattern = Pattern.compile("(超过|大于|高于|不低于|不小于|>)\\s*(\\d+(\\.\\d+)?)\\s*(MB|GB|KB|M|G|K|mb|gb|kb)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            double value = Double.parseDouble(matcher.group(2));
            String unit = matcher.group(4).toUpperCase();
            long bytes = sizeToBytes(value, unit);
            intent.minSize = bytes;
        }

        // 小于/低于/不高于
        Pattern patternMax = Pattern.compile(
            "(小于|低于|不高于|不大于|<|不超过|最多|最大)\\s*(\\d+(\\.\\d+)?)\\s*(MB|GB|KB|M|G|K|mb|gb|kb)", Pattern.CASE_INSENSITIVE);
        Matcher matcherMax = patternMax.matcher(content);
        if (matcherMax.find()) {
            double value = Double.parseDouble(matcherMax.group(2));
            String unit = matcherMax.group(4).toUpperCase();
            long bytes = sizeToBytes(value, unit);
            intent.maxSize = bytes;
        }
    }

    /**
     * 将数值+单位换算为字节
     */
    private long sizeToBytes(double value, String unit) {
        switch (unit) {
            case "GB":
            case "G":
                return (long)(value * 1024 * 1024 * 1024);
            case "MB":
            case "M":
                return (long)(value * 1024 * 1024);
            case "KB":
            case "K":
                return (long)(value * 1024);
            default:
                return (long)value;
        }
    }

    /**
     * 提取引号内的精确关键词（支持中文引号 "" ''、英文双引号 "" 和单引号 ''） "包含'方案'的文档" → "方案" "包含\"方案\"的文档" → "方案"
     */
    private String extractQuotedKeyword(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        // 匹配中文引号：'...' 或 "..."
        Pattern pattern = Pattern.compile("[\u2018\u201c]([^\u2019\u201d]{1,50})[\u2019\u201d]");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // 匹配ASCII英文双引号（中文用户最常输入的类型，优先于直角引号，避免「旧名」"新名"时取错）
        Pattern patternDbl = Pattern.compile("\"([^\"]{1,50})\"");
        Matcher matcherDbl = patternDbl.matcher(content);
        if (matcherDbl.find()) {
            return matcherDbl.group(1).trim();
        }
        // 匹配直角引号：「...」『...』《...》
        Pattern patternCorner =
            Pattern.compile("[\u300c\u300e\u300a]([^\u300d\u300f\u300b]{1,50})[\u300d\u300f\u300b]");
        Matcher matcherCorner = patternCorner.matcher(content);
        if (matcherCorner.find()) {
            return matcherCorner.group(1).trim();
        }
        // 匹配ASCII英文单引号
        Pattern patternEn = Pattern.compile("'([^']{1,50})'");
        Matcher matcherEn = patternEn.matcher(content);
        if (matcherEn.find()) {
            return matcherEn.group(1).trim();
        }
        return null;
    }

    /**
     * 解析自然语言搜索意图（基于规则匹配，不依赖外部 AI）
     */
    private SearchIntent parseSearchIntent(String query) {
        SearchIntent intent = new SearchIntent();

        // 1. 去掉搜索引导词，提取核心内容
        String content = query.trim();
        for (String prefix : SEARCH_PREFIXES) {
            if (content.startsWith(prefix)) {
                content = content.substring(prefix.length()).trim();
                break;
            }
        }

        // 2. 检测时间意图（日期范围 / 排序方向）
        parseDateIntent(content, intent);

        // 2.5 检测文件大小约束（"超过100MB"等）
        parseSizeConstraint(content, intent);

        // 3. 检测文件类型意图
        intent.fileType = detectFileType(content);

        // 3.5 优先提取引号内的精确关键词："包含'方案'" → keyword="方案"
        String quotedKeyword = extractQuotedKeyword(content);

        // 4. 提取关键字：去掉时间词、类型词、上传/文件等通用词
        String keyword = content;
        String[] dateWords = {"今天", "昨天", "本周", "本月", "这周", "这月", "上周", "上月", "最近", "近期", "最新", "刚刚", "刚才"};
        for (String w : dateWords) {
            keyword = keyword.replace(w, "");
        }
        for (String[] group : new String[][] {TIME_RECENT_KEYS, TIME_OLD_KEYS, TYPE_WORD_KEYS, TYPE_EXCEL_KEYS,
            TYPE_PDF_KEYS, TYPE_IMAGE_KEYS, TYPE_VIDEO_KEYS, TYPE_ZIP_KEYS}) {
            for (String key : group) {
                keyword = keyword.replace(key, "");
            }
        }
        // 去掉通用填充词（包含查询结构词、代词、语气词和引号等）
        String[] fillers = {"上传", "文件", "文档", "的", "一个", "一些", "几个", "那个", "这个", "一下", "到", "和", "与", "或", "所有", "全部",
            "有没有", "有", "什么", "哪些", "包含", "含有", "包括", "带", "查找", "查找所有", "帮我", "请问", "？", "?", "！", "!", "。", "，", ",",
            "：", "\u201c", "\u201d", "\"", "'", "\u2018", "\u2019", "\u300a", "\u300b", "\u300c", "\u300d",
            // 中文代词/语气词/助词——剩余这类词不能作为文件搜索关键字
            "我", "你", "他", "她", "它", "我们", "你们", "他们", "了", "吗", "呢", "啊", "吧", "是", "在", "的", "还", "就", "也", "都", "很",
            "要", "会", "把", "被", "给", "从", "对", "向", "跟", "让", "叫", "可以", "可能", "需要", "应该"};
        for (String f : fillers) {
            keyword = keyword.replace(f, "");
        }
        keyword = keyword.trim();
        // 优先使用引号中提取的精确关键词（如 "包含'方案'" → 方案）
        if (StringUtils.isNotBlank(quotedKeyword)) {
            intent.keyword = quotedKeyword;
        } else if (StringUtils.isNotBlank(keyword) && keyword.length() >= 2) {
            // 额外校验：如果剩下的"关键字"是纯数字或纯标点，也当作无效关键字，避免干扰日期查询
            if (!keyword.matches("^[\\d\\p{Punct}\\s]+$")) {
                // 如果关键字本质上是大小描述（如"超过50MB"）且已有minSize/maxSize约束，清空关键字
                String cleaned = keyword.replaceAll("[\\d.]+\\s*(MB|GB|KB|M|G|K)", "");
                if (StringUtils.isNotBlank(cleaned.replaceAll("[超过大于小于高于低于不低于不高于不大于不超过最多最大]", "").trim())) {
                    intent.keyword = keyword;
                }
                // 否则：关键字是纯大小描述，且已有大小约束，不设keyword
            }
        }

        return intent;
    }

    /**
     * 解析日期意图：今天/昨天/本周/本月/上周/上月 等
     */
    private void parseDateIntent(String content, SearchIntent intent) {
        Calendar cal = Calendar.getInstance();

        // 今天
        if (content.contains("今天")) {
            intent.timeOrder = "recent";
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            intent.startDate = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intent.endDate = cal.getTime();
            return;
        }

        // 昨天
        if (content.contains("昨天")) {
            intent.timeOrder = "recent";
            cal.add(Calendar.DAY_OF_MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            intent.startDate = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intent.endDate = cal.getTime();
            return;
        }

        // 本周 / 这周
        if (content.contains("本周") || content.contains("这周")) {
            intent.timeOrder = "recent";
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            intent.startDate = cal.getTime();
            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intent.endDate = cal.getTime();
            return;
        }

        // 上周
        if (content.contains("上周")) {
            intent.timeOrder = "recent";
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            intent.startDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 6);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intent.endDate = cal.getTime();
            return;
        }

        // 本月 / 这月
        if (content.contains("本月") || content.contains("这月")) {
            intent.timeOrder = "recent";
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            intent.startDate = cal.getTime();
            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intent.endDate = cal.getTime();
            return;
        }

        // 上月
        if (content.contains("上月")) {
            intent.timeOrder = "recent";
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            intent.startDate = cal.getTime();
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intent.endDate = cal.getTime();
            return;
        }

        // 遗留的 relative 时间意图（最近/最新 → recent, 最早/旧的 → old）
        for (String key : TIME_RECENT_KEYS) {
            if (content.contains(key)) {
                intent.timeOrder = "recent";
                return;
            }
        }
        for (String key : TIME_OLD_KEYS) {
            if (content.contains(key)) {
                intent.timeOrder = "old";
                return;
            }
        }
    }

    /**
     * 检测查询中的文件类型
     */
    private String detectFileType(String content) {
        String lower = content.toLowerCase();
        for (String key : TYPE_WORD_KEYS) {
            if (lower.contains(key.toLowerCase()))
                return "word";
        }
        for (String key : TYPE_EXCEL_KEYS) {
            if (lower.contains(key.toLowerCase()))
                return "excel";
        }
        for (String key : TYPE_PDF_KEYS) {
            if (lower.contains(key.toLowerCase()))
                return "pdf";
        }
        for (String key : TYPE_IMAGE_KEYS) {
            if (lower.contains(key.toLowerCase()))
                return "image";
        }
        for (String key : TYPE_VIDEO_KEYS) {
            if (lower.contains(key.toLowerCase()))
                return "video";
        }
        for (String key : TYPE_ZIP_KEYS) {
            if (lower.contains(key.toLowerCase()))
                return "zip";
        }
        return null;
    }

    /**
     * 判断文件后缀是否匹配指定类型
     */
    private boolean matchFileType(String suffix, String fileType) {
        if (StringUtils.isBlank(suffix))
            return false;
        String ext = suffix.toLowerCase().replace(".", "");
        switch (fileType) {
            case "word":
                return "doc".equals(ext) || "docx".equals(ext);
            case "excel":
                return "xls".equals(ext) || "xlsx".equals(ext) || "csv".equals(ext);
            case "pdf":
                return "pdf".equals(ext);
            case "image":
                return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "gif".equals(ext)
                    || "bmp".equals(ext) || "webp".equals(ext);
            case "video":
                return "mp4".equals(ext) || "avi".equals(ext) || "mov".equals(ext) || "wmv".equals(ext)
                    || "flv".equals(ext) || "mkv".equals(ext);
            case "zip":
                return "zip".equals(ext) || "rar".equals(ext) || "7z".equals(ext) || "tar".equals(ext)
                    || "gz".equals(ext);
            default:
                return true;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> aiChat(String sessionId, String message, Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId();
        String personId = userInfo.getPersonId();

        // 获取或创建会话
        ChatSession session;
        if (StringUtils.isNotBlank(sessionId)) {
            session = chatSessionRepository.findById(sessionId).orElse(null);
            if (session == null) {
                result.put("success", false);
                result.put("msg", "会话不存在");
                return result;
            }
        } else {
            session = new ChatSession();
            session.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            session.setPersonId(personId);
            session.setTenantId(tenantId);
            session.setTitle(StringUtils.abbreviate(message, 50));
            session.setCreateTime(new Date());
            session.setUpdateTime(new Date());
            chatSessionRepository.save(session);
        }

        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(new Date());
        chatMessageRepository.save(userMsg);

        // 构建对话历史上下文
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(session.getId());
        List<Map<String, String>> messages = new ArrayList<>();

        // 注入 @文件 提及的上下文为系统消息
        String fileContextPrompt = buildMentionedFilesPrompt(context, message);
        if (StringUtils.isNotBlank(fileContextPrompt)) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", fileContextPrompt);
            messages.add(systemMsg);
        }

        for (ChatMessage msg : history) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messages.add(m);
        }

        // 调用 AI API（消息路由：仅 @ 了文件时才能操作网盘文件，否则全部交给 AI 通用对话）
        String aiReply;

        // ===== 0. 检测用户是否通过 @ 提及了网盘文件 =====
        boolean hasMentionedFiles = context != null && context.containsKey("mentionedFiles");
        LOGGER.debug("[AI Chat] hasMentionedFiles={}, context={}, message={}", hasMentionedFiles,
            context != null ? context.keySet() : "null", StringUtils.abbreviate(message, 80));

        if (!hasMentionedFiles) {
            // 判断消息是否以 @ 开头（@后不是已解析的文件名，而是命令）
            // 例如 @新建文件夹 888 → 创建文件夹； @帮我找方案 → 搜索文件
            String trimmedMsg = message != null ? message.trim() : "";
            if (trimmedMsg.startsWith("@")) {
                // @ 开头 = 文件系统命令，执行意图路由
                String cmdMsg = trimmedMsg.substring(1).trim(); // 去掉 @ 前缀
                if (isCreateFolderIntent(cmdMsg)) {
                    aiReply = handleCreateFolder(cmdMsg);
                } else if (isDeleteIntent(cmdMsg)) {
                    aiReply = handleDeleteByName(cmdMsg);
                } else if (isRestoreIntent(cmdMsg)) {
                    aiReply = handleRestoreFiles(cmdMsg);
                } else if (isStorageAnalysisQuery(cmdMsg)) {
                    aiReply = handleStorageAnalysis(cmdMsg);
                } else if (isFileSearchQuery(cmdMsg)) {
                    aiReply = handleFileSearchInChat(cmdMsg);
                    if (isAnalysisIntent(cmdMsg) && lastSearchResults != null && !lastSearchResults.isEmpty()) {
                        String extractedContent = extractContentFromSearchResults(lastSearchResults, cmdMsg);
                        if (StringUtils.isNotBlank(extractedContent)) {
                            aiReply += "\n\n" + extractedContent;
                        }
                    }
                } else {
                    // @ 开头的其他命令，交给 AI 通用对话（如 @帮我写个代码）
                    aiReply = callAiApi(messages, context);
                }
            } else {
                // 不带 @ 的普通消息：全部交给 AI 通用对话
                aiReply = callAiApi(messages, context);
            }
        } else {
            // @ 文件提及：优先检测操作意图（删除/下载/重命名/移动/加密/分享），再交给 AI 分析
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mentionedFiles = (List<Map<String, Object>>)context.get("mentionedFiles");
            if (mentionedFiles != null && !mentionedFiles.isEmpty()) {
                String fileId = (String)mentionedFiles.get(0).get("id");
                // 前端可能用 "name" 或 "fileName" 传递文件名，都尝试获取
                String fileName = (String)mentionedFiles.get(0).get("name");
                if (fileName == null) {
                    fileName = (String)mentionedFiles.get(0).get("fileName");
                }
                // 如果仍为空，从数据库查询文件名
                if (fileName == null && StringUtils.isNotBlank(fileId)) {
                    try {
                        FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                        if (fn != null) {
                            fileName = fn.getName();
                        }
                    } catch (Exception e) {
                        LOGGER.debug("查询@提及文件名失败: fileId={}", fileId, e);
                    }
                }
                if (fileName == null)
                    fileName = "未知文件";
                String[] fileRef = new String[] {fileId, fileName};

                if (isDeleteIntent(message)) {
                    aiReply = handleMentionedFileDelete(mentionedFiles);
                } else if (message.contains("下载")) {
                    aiReply = handleDownloadFiles(message, fileRef);
                } else if (isRenameIntent(message)) {
                    aiReply = handleRenameFile(message, fileRef);
                } else if (isMoveIntent(message)) {
                    aiReply = handleMoveFile(message, fileRef);
                } else if (isShareIntent(message)) {
                    aiReply = handleShareFile(message, fileRef);
                } else if (isEncryptionIntent(message)) {
                    aiReply = handleEncryptionByIndex(message, fileRef);
                } else if (isAnalysisIntent(message)) {
                    aiReply = handleMentionedFileAnalysis(mentionedFiles, message);
                } else {
                    aiReply = callAiApi(messages, context);
                }
            } else {
                aiReply = callAiApi(messages, context);
            }
        }

        // 保存 AI 回复
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiReply);
        assistantMsg.setCreateTime(new Date());
        chatMessageRepository.save(assistantMsg);

        // 更新会话时间
        session.setUpdateTime(new Date());
        chatSessionRepository.save(session);

        result.put("success", true);
        result.put("sessionId", session.getId());
        result.put("reply", aiReply);
        result.put("title", session.getTitle());
        return result;
    }

    // ==================== AI 对话 ====================

    @Override
    @Transactional
    public void aiChatStream(String sessionId, String message, Map<String, Object> context,
        HttpServletResponse response) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId();
        String personId = userInfo.getPersonId();

        // 获取或创建会话
        ChatSession session;
        if (StringUtils.isNotBlank(sessionId)) {
            session = chatSessionRepository.findById(sessionId).orElse(null);
            if (session == null) {
                writeSseError(response, "会话不存在");
                return;
            }
        } else {
            session = new ChatSession();
            session.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            session.setPersonId(personId);
            session.setTenantId(tenantId);
            session.setTitle(StringUtils.abbreviate(message, 50));
            session.setCreateTime(new Date());
            session.setUpdateTime(new Date());
            chatSessionRepository.save(session);
        }

        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(new Date());
        chatMessageRepository.save(userMsg);

        // 构建对话历史上下文
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(session.getId());
        List<Map<String, String>> messages = new ArrayList<>();

        // 注入 @文件 提及的上下文为系统消息
        String fileContextPrompt = buildMentionedFilesPrompt(context, message);
        if (StringUtils.isNotBlank(fileContextPrompt)) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", fileContextPrompt);
            messages.add(systemMsg);
        }

        for (ChatMessage msg : history) {
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            messages.add(m);
        }

        // 调用 AI 流式 API
        String fullReply = callAiStreamApi(messages, context, response);

        // 保存完整的 AI 回复
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(fullReply);
        assistantMsg.setCreateTime(new Date());
        chatMessageRepository.save(assistantMsg);

        // 更新会话时间
        session.setUpdateTime(new Date());
        chatSessionRepository.save(session);
    }

    @Override
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
    }

    @Override
    public List<Map<String, Object>> getSessionList() {
        String personId = Y9LoginUserHolder.getPersonId();
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<ChatSession> sessions =
            chatSessionRepository.findByPersonIdAndTenantIdOrderByUpdateTimeDesc(personId, tenantId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatSession s : sessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("title", s.getTitle());
            item.put("createTime", s.getCreateTime());
            item.put("updateTime", s.getUpdateTime());
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
        chatSessionRepository.deleteById(sessionId);
    }

    @Override
    public Map<String, Object> analyzeFile(String fileId, String analysisType) {
        Map<String, Object> result = new HashMap<>();
        FileNode fileNode = fileNodeRepository.findById(fileId).orElse(null);
        if (fileNode == null) {
            result.put("success", false);
            result.put("msg", "文件不存在");
            return result;
        }
        // TODO: 调用 AI 服务对文件内容进行分析
        String analysis = callAiFileAnalysis(fileNode, analysisType);
        result.put("success", true);
        result.put("fileId", fileId);
        result.put("fileName", fileNode.getName());
        result.put("analysisType", analysisType);
        result.put("analysis", analysis);
        return result;
    }

    // ==================== 文件分析 ====================

    @Override
    public List<Map<String, Object>> aiRecommend(int limit) {
        // TODO: 调用 AI 服务根据用户行为进行智能推荐
        // 当前 stub: 返回最近上传的文件
        String personId = Y9LoginUserHolder.getPersonId();
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<FileNode> filePage =
            fileNodeRepository.findByUserIdStartingWithAndDeletedFalseOrderByCreateTimeDesc(personId, pageRequest);
        List<Map<String, Object>> items = new ArrayList<>();
        for (FileNode fn : filePage.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fn.getId());
            item.put("name", fn.getName());
            item.put("fileSuffix", fn.getFileSuffix());
            item.put("fileSize", fn.getFileSize());
            item.put("createTime", fn.getCreateTime());
            item.put("reason", "您最近上传的文件");
            items.add(item);
        }
        return items;
    }

    // ==================== 智能推荐 ====================

    @Override
    public Map<String, Object> autoTag(String fileId) {
        Map<String, Object> result = new HashMap<>();
        FileNode fileNode = fileNodeRepository.findById(fileId).orElse(null);
        if (fileNode == null) {
            result.put("success", false);
            result.put("msg", "文件不存在");
            return result;
        }
        // TODO: 调用 AI 服务根据文件内容/文件名生成标签
        List<String> tags = generateTagsFromFileInfo(fileNode);
        result.put("success", true);
        result.put("fileId", fileId);
        result.put("tags", tags);
        return result;
    }

    // ==================== 智能标签 ====================

    @Override
    public Map<String, Object> docQuestion(String fileId, String question) {
        Map<String, Object> result = new HashMap<>();
        FileNode fileNode = fileNodeRepository.findById(fileId).orElse(null);
        if (fileNode == null) {
            result.put("success", false);
            result.put("msg", "文件不存在");
            return result;
        }
        // TODO: 调用 AI 服务基于文件内容回答用户问题
        String answer = callAiDocumentQa(fileNode, question);
        result.put("success", true);
        result.put("fileId", fileId);
        result.put("fileName", fileNode.getName());
        result.put("question", question);
        result.put("answer", answer);
        return result;
    }

    // ==================== 文档问答 ====================

    // ==================== AI 调用方法（统一入口） ====================

    /**
     * 调用 AI API（非流式）
     * <p>
     * 接入真实大模型后，通过 AiApiClient 调用 OpenAI 兼容 API。 未配置 ai.enabled=true 时回退到 stub 模式。
     *
     * @param messages 对话消息列表 [{"role":"user"/"assistant", "content":"..."}]
     * @param context 上下文信息
     * @return AI 回复内容
     */
    private String callAiApi(List<Map<String, String>> messages, Map<String, Object> context) {
        LOGGER.info("AI 对话请求, 消息数: {}, 上下文: {}", messages.size(), context);

        // ========== 真实 AI 调用 ==========
        if (aiProperties.isEnabled()) {
            String reply = aiApiClient.chat(messages);
            if (reply != null) {
                return reply;
            }
            LOGGER.warn("AI API 返回 null，回退到 stub 模式");
        }

        // ========== stub 回退 ==========
        // 检查是否有 @ 提及的文件上下文
        List<Map<String, Object>> mentionedFiles = extractMentionedFilesFromContext(context);
        if (mentionedFiles != null && !mentionedFiles.isEmpty()) {
            return buildStubReplyWithFiles(messages, mentionedFiles);
        }

        String lastMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1).get("content");
        return "您好，我已经收到您的问题：「" + StringUtils.abbreviate(lastMessage, 100)
            + "」。\n\n当前为 AI 服务 stub 模式，请在 application.yml 中配置 ai.* 接入实际的大模型服务。\n\n" + "配置示例（以 DeepSeek 为例）：\n"
            + "  ai:\n" + "    enabled: true\n" + "    base-url: https://api.deepseek.com\n"
            + "    api-key: sk-xxxxxxxxxxxxx\n" + "    model: deepseek-chat";
    }

    /**
     * 从 context 中提取 @ 提及的文件列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractMentionedFilesFromContext(Map<String, Object> context) {
        if (context == null) {
            return null;
        }
        Object obj = context.get("mentionedFiles");
        if (obj instanceof List) {
            return (List<Map<String, Object>>)obj;
        }
        return null;
    }

    /**
     * stub 模式：基于 @ 提及的文件构建有意义的回复
     * <p>
     * 从数据库中查询文件详情并提取文件内容预览，生成文件分析回复。
     */
    private String buildStubReplyWithFiles(List<Map<String, String>> messages,
        List<Map<String, Object>> mentionedFiles) {
        String lastMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1).get("content");

        StringBuilder sb = new StringBuilder();
        sb.append("关于您 @ 的文件，以下是基本信息：\n\n");

        for (Map<String, Object> fileInfo : mentionedFiles) {
            String fileId = (String)fileInfo.get("id");
            // 前端可能用 "name" 或 "fileName" 传递文件名，都尝试获取
            String fileName = (String)fileInfo.get("name");
            if (fileName == null) {
                fileName = (String)fileInfo.get("fileName");
            }
            // 如果仍为空，从数据库查询文件名
            if (fileName == null && StringUtils.isNotBlank(fileId)) {
                try {
                    FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                    if (fn != null) {
                        fileName = fn.getName();
                    }
                } catch (Exception e) {
                    LOGGER.debug("查询@提及文件名失败: fileId={}", fileId, e);
                }
            }

            sb.append("### ").append(fileName != null ? fileName : "未知文件").append("\n");

            if (StringUtils.isNotBlank(fileId)) {
                try {
                    FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                    if (fn != null) {
                        sb.append("- **大小**: ").append(formatFileSize(fn.getFileSize())).append("\n");
                        sb.append("- **类型**: ")
                            .append(org.apache.commons.lang3.StringUtils.defaultString(fn.getFileSuffix(), "未知"))
                            .append("\n");
                        sb.append("- **上传时间**: ")
                            .append(fn.getCreateTime() != null
                                ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fn.getCreateTime())
                                : "未知")
                            .append("\n");
                        sb.append("- **下载**: [点击下载](/downloadFile?ids=").append(fileId).append(")\n");

                        // 尝试提取文件内容预览
                        String fileContent = extractFileContent(fn);
                        if (StringUtils.isNotBlank(fileContent)) {
                            String preview = org.apache.commons.lang3.StringUtils.abbreviate(fileContent, 1500);
                            sb.append("\n**文件内容预览**:\n```\n").append(preview).append("\n```\n");
                        } else {
                            sb.append("\n*（该文件为二进制格式，无法直接提取文本内容预览）*\n");
                        }
                    } else {
                        sb.append("*文件未在数据库中找到*\n");
                    }
                } catch (Exception e) {
                    LOGGER.warn("构建文件信息失败, fileId={}", fileId, e);
                    sb.append("*获取文件信息失败*\n");
                }
            }
            sb.append("\n");
        }

        sb.append("---\n");
        sb.append("> **提示**: 当前为 AI 服务 stub 模式（未接入大模型）。如需 AI 智能分析和问答，请在 `application.yml` 中配置 `ai.*` 参数接入大模型服务。\n");

        return sb.toString();
    }

    // ==================== AI 调用方法 ====================

    /**
     * 调用 AI 流式 API（SSE）
     * <p>
     * 接入真实大模型后，通过 AiApiClient 调用流式 API 并写回 SSE。 未启用时回退到 stub 模式（模拟逐字输出）。
     */
    private String callAiStreamApi(List<Map<String, String>> messages, Map<String, Object> context,
        HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        // ========== 真实 AI 流式调用 ==========
        if (aiProperties.isEnabled()) {
            StringBuilder fullReply = new StringBuilder();
            try (PrintWriter writer = response.getWriter()) {
                writer.write("data: {\"sessionId\":\"new\"}\n\n");
                writer.flush();

                String reply = aiApiClient.chatStream(messages, token -> {
                    try {
                        writer.write("data: " + token + "\n\n");
                        writer.flush();
                    } catch (Exception e) {
                        LOGGER.error("SSE 写入失败", e);
                    }
                });

                if (reply != null) {
                    fullReply.append(reply);
                }
                writer.write("data: [DONE]\n\n");
                writer.flush();
                return fullReply.toString();
            } catch (IOException e) {
                LOGGER.error("AI 流式响应写入失败", e);
            }
            return fullReply.toString();
        }

        // ========== stub 回退 ==========
        String stubReply = "您好，这是 AI 流式响应的演示。\n\n当前为 stub 模式，请在 application.yml 中配置 ai.* 接入实际的大模型服务。";
        StringBuilder fullReply = new StringBuilder();

        try (PrintWriter writer = response.getWriter()) {
            writer.write("data: {\"sessionId\":\"new\"}\n\n");
            writer.flush();

            for (char c : stubReply.toCharArray()) {
                String chunk = String.valueOf(c);
                fullReply.append(chunk);
                writer.write("data: " + chunk + "\n\n");
                writer.flush();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            writer.write("data: [DONE]\n\n");
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("AI 流式响应写入失败", e);
        }
        return fullReply.toString();
    }

    /**
     * 调用 AI 对文件进行智能分析
     * <p>
     * 先从文件存储中提取文本内容，再提交给大模型分析。
     */
    private String callAiFileAnalysis(FileNode fileNode, String analysisType) {
        LOGGER.info("AI 文件分析, fileId={}, fileName={}, type={}", fileNode.getId(), fileNode.getName(), analysisType);

        // ========== 提取文件内容 ==========
        String fileContent = extractFileContent(fileNode);

        // ========== 真实 AI 分析 ==========
        if (aiProperties.isEnabled()) {
            String result = aiApiClient.analyzeFileContent(fileNode.getName(), fileContent, analysisType);
            if (result != null) {
                return result;
            }
        }

        // ========== stub 回退 ==========
        String contentNote = fileContent != null ? "（已提取 " + fileContent.length() + " 字符）" : "（无法提取文件内容）";
        switch (analysisType) {
            case "summary":
                return "【摘要】文件「" + fileNode.getName() + "」" + contentNote + "\n"
                    + (fileContent != null ? "文件内容预览：\n" + StringUtils.abbreviate(fileContent, 500) : "")
                    + "\n\nAI 服务未启用，以上为本地提取的内容。";
            case "keywords":
                return "【关键词】文件类型: " + fileNode.getFileSuffix() + ", 大小: " + formatFileSize(fileNode.getFileSize())
                    + contentNote + "\n（AI 服务未启用）";
            case "entity":
                return "【实体识别】" + contentNote + "\n（AI 服务未启用，请配置 ai.enabled=true）";
            default:
                return "未知分析类型: " + analysisType;
        }
    }

    /**
     * 根据文件信息生成标签（优先 AI 生成，失败时规则兜底）
     */
    private List<String> generateTagsFromFileInfo(FileNode fileNode) {
        // ========== AI 生成标签 ==========
        if (aiProperties.isEnabled()) {
            String fileContent = extractFileContent(fileNode);
            List<String> aiTags = aiApiClient.generateTags(fileNode.getName(), fileContent, fileNode.getFileSuffix());
            if (aiTags != null && !aiTags.isEmpty()) {
                return aiTags;
            }
        }

        // ========== 规则兜底 ==========
        List<String> tags = new ArrayList<>();
        String ext = fileNode.getFileSuffix();
        if (StringUtils.isNotBlank(ext)) {
            switch (ext.toLowerCase()) {
                case "pdf":
                    tags.add("PDF文档");
                    tags.add("电子文档");
                    break;
                case "doc":
                case "docx":
                    tags.add("Word文档");
                    tags.add("办公文档");
                    break;
                case "xls":
                case "xlsx":
                    tags.add("Excel表格");
                    tags.add("数据报表");
                    break;
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                    tags.add("图片");
                    tags.add("图像文件");
                    break;
                case "mp4":
                case "avi":
                    tags.add("视频");
                    tags.add("多媒体");
                    break;
                case "zip":
                case "rar":
                    tags.add("压缩包");
                    break;
                default:
                    tags.add(ext.toUpperCase() + "文件");
                    break;
            }
        }
        return tags;
    }

    /**
     * 调用 AI 进行文档问答
     * <p>
     * 提取文件内容后，将文件内容作为上下文提交给大模型，基于内容回答用户问题。
     */
    private String callAiDocumentQa(FileNode fileNode, String question) {
        LOGGER.info("AI 文档问答, fileId={}, fileName={}, question={}", fileNode.getId(), fileNode.getName(), question);

        // ========== 提取文件内容 ==========
        String fileContent = extractFileContent(fileNode);
        if (fileContent == null) {
            return "文件「" + fileNode.getName() + "」为二进制格式（." + fileNode.getFileSuffix()
                + "），无法直接提取文本内容。\n建议上传 .txt/.csv/.json/.md/.log 等文本格式文件进行问答。";
        }

        // ========== 真实 AI 问答 ==========
        if (aiProperties.isEnabled()) {
            String answer = aiApiClient.documentQa(fileNode.getName(), fileContent, question);
            if (answer != null) {
                return answer;
            }
        }

        // ========== stub 回退 ==========
        return "关于「" + fileNode.getName() + "」文件的提问：「" + question + "」\n\n" + "已提取文件内容（" + fileContent.length()
            + " 字符），但 AI 服务未启用。\n" + "文件内容预览：\n```\n" + StringUtils.abbreviate(fileContent, 800) + "\n```";
    }

    // ==================== 文件内容提取 ====================

    /**
     * 从文件存储中提取文本内容
     */
    private String extractFileContent(FileNode fileNode) {
        try {
            if (StringUtils.isBlank(fileNode.getFileStoreId())) {
                return null;
            }
            byte[] fileBytes = y9FileStoreService.downloadFileToBytes(fileNode.getFileStoreId());
            if (fileBytes == null || fileBytes.length == 0) {
                return null;
            }
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                return FileContentExtractor.extract(is, fileNode.getFileSuffix(), aiProperties.getMaxContentLength());
            }
        } catch (Throwable t) {
            LOGGER.warn("提取文件内容失败, fileId={}, fileName={}", fileNode.getId(), fileNode.getName(), t);
            return null;
        }
    }

    /**
     * 从搜索结果中提取文件内容（用于"文件Xxx 提取文字"这类组合意图）
     */
    @SuppressWarnings("unchecked")
    private String extractContentFromSearchResults(List<Map<String, Object>> searchResults, String message) {
        StringBuilder result = new StringBuilder();
        result.append("---\n");
        int extractedCount = 0;

        for (Map<String, Object> item : searchResults) {
            String fileId = (String)item.get("id");
            if (StringUtils.isBlank(fileId))
                continue;

            try {
                FileNode fn = fileNodeRepository.findById(fileId).orElse(null);
                if (fn == null)
                    continue;

                String content = extractFileContent(fn);
                String fileName = fn.getName();

                if (StringUtils.isNotBlank(content)) {
                    if (extractedCount > 0)
                        result.append("\n---\n");
                    result.append("**").append(fileName).append("** 的内容：\n");
                    int maxShow = Math.min(content.length(), 5000);
                    result.append(content.substring(0, maxShow));
                    if (content.length() > maxShow) {
                        result.append("\n*(内容已截断，共 ").append(content.length()).append(" 字符)*");
                    }
                    result.append("\n");
                    extractedCount++;
                } else {
                    String suffix = fn.getFileSuffix();
                    if (FileContentExtractor.isSupported(suffix)) {
                        String typeName = FileContentExtractor.getTypeName(suffix);
                        if ("图片".equals(typeName)) {
                            result.append("**").append(fileName).append("**: 图片文字识别失败，服务器未安装 Tesseract OCR 引擎。\n");
                        } else {
                            result.append("**").append(fileName).append("**: 提取内容失败。\n");
                        }
                    } else {
                        result.append("**")
                            .append(fileName)
                            .append("**: ")
                            .append(StringUtils.defaultIfBlank(FileContentExtractor.getTypeName(suffix), suffix))
                            .append("类型暂不支持提取内容。\n");
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("从搜索结果提取文件内容失败: fileId={}", fileId, e);
            }
        }

        if (extractedCount == 0 && searchResults.size() > 0) {
            // 图片识别失败时追加 OCR 安装指引
            if (result.indexOf("图片文字识别失败") != -1) {
                result.append(OCR_INSTALL_GUIDE);
                return result.toString();
            }
            return null; // 全部提取失败且不是 OCR 问题，返回 null
        }
        return result.toString();
    }

    // ==================== 上传时建立 AI 索引 ====================

    @Override
    public void indexUploadedFile(FileNode fileNode) {
        if (!aiProperties.isEnabled()) {
            return;
        }
        if (!aiProperties.isIndexOnUpload()) {
            return;
        }
        // 检查是否为可索引的文件类型
        String ext = fileNode.getFileSuffix();
        if (StringUtils.isBlank(ext)) {
            return;
        }
        String indexable = aiProperties.getIndexableExtensions();
        if (StringUtils.isNotBlank(indexable) && !indexable.contains(ext.toLowerCase())) {
            return;
        }

        // 异步提取文件内容并喂给 AI
        new Thread(() -> {
            try {
                String content = extractFileContent(fileNode);
                if (content != null) {
                    LOGGER.info("AI 索引完成, fileId={}, fileName={}, contentLength={}", fileNode.getId(),
                        fileNode.getName(), content.length());

                    // 调用 AI 对文件内容进行摘要，作为索引信息
                    if (aiProperties.isEnabled()) {
                        String summary = aiApiClient.analyzeFileContent(fileNode.getName(), content, "summary");
                        LOGGER.info("AI 文件摘要, fileId={}, summary={}", fileNode.getId(), summary);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("AI 索引失败, fileId={}, fileName={}", fileNode.getId(), fileNode.getName(), e);
            }
        }, "ai-index-" + fileNode.getId().substring(0, 8)).start();
    }

    /**
     * 写入 SSE 错误信息
     */
    private void writeSseError(HttpServletResponse response, String errorMsg) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("data: {\"error\":\"" + errorMsg + "\"}\n\n");
            writer.write("data: [DONE]\n\n");
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("写入 SSE 错误失败", e);
        }
    }

    /**
     * 搜索意图解析结果
     */
    private static class SearchIntent {
        String keyword; // 提取的关键字
        String timeOrder; // recent / old / null
        String fileType; // word / excel / pdf / image / video / zip / null
        Date startDate; // 日期范围起始（例如"今天"的 00:00:00）
        Date endDate; // 日期范围结束（例如"今天"的 23:59:59）
        Long minSize; // 文件最小大小（字节），null表示不限制
        Long maxSize; // 文件最大大小（字节），null表示不限制
    }
}
