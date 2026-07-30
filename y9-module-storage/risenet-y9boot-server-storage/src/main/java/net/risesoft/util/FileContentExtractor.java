package net.risesoft.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.config.TesseractOcrProperties;
import net.risesoft.service.Impl.TencentOcrService;

/**
 * 文件文本内容提取器
 * <p>
 * 支持从常见文件格式中提取纯文本内容，用于 AI 分析、搜索索引等场景。
 * <p>
 * 支持的格式：
 * <ul>
 * <li>纯文本：txt, csv, json, xml, md, log, java, py, js, ts, html, css, sql, yml, yaml, properties, sh, bat, ini, cfg,
 * conf, toml, rst, tex, c, cpp, h, go, rs, kt, swift, scala, rb, php, pl, lua, r, m, mm, markdown</li>
 * <li>Office 文档：docx, doc</li>
 * <li>Office 表格：xlsx, xls</li>
 * <li>图片 OCR：jpg, jpeg, png, gif, bmp, tiff, tif, webp</li>
 * </ul>
 */
@Slf4j
public class FileContentExtractor {

    /** 纯文本文件扩展名集合 */
    private static final Set<String> TEXT_EXTENSIONS =
        new HashSet<>(Arrays.asList("txt", "csv", "json", "xml", "md", "markdown", "log", "java", "py", "js", "ts",
            "html", "css", "sql", "yml", "yaml", "properties", "sh", "bat", "ini", "cfg", "conf", "toml", "rst", "tex",
            "c", "cpp", "h", "go", "rs", "kt", "swift", "scala", "rb", "php", "pl", "lua", "r", "m", "mm"));

    /** Word 文档扩展名 */
    private static final Set<String> WORD_EXTENSIONS = new HashSet<>(Arrays.asList("docx", "doc"));

    /** Excel 表格扩展名 */
    private static final Set<String> EXCEL_EXTENSIONS = new HashSet<>(Arrays.asList("xlsx", "xls"));

    /** 图片扩展名（支持 OCR） */
    private static final Set<String> IMAGE_EXTENSIONS =
        new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "webp"));
    private static final Object TESSERACT_LOCK = new Object();
    /** 腾讯云 OCR 服务桥接（由 Spring 注入） */
    private static volatile TencentOcrService tencentOcrService;
    /** Tesseract 实例（懒加载，复用）。null=尚未初始化或初始化失败，下次调用会重新尝试 */
    private static volatile Object tesseractInstance = null;
    /** JNA DLL 路径是否已注册成功（static 块可能因 Spring 未初始化而失败，运行时补充注册） */
    private static volatile boolean jnaPathRegistered = false;
    /** 自动检测到的 Tesseract 安装目录（缓存，避免重复执行 where 命令） */
    private static volatile String detectedTesseractHome = null;
    private static volatile boolean tesseractHomeDetectionAttempted = false;

    // ==================== 纯文本提取 ====================

    static {
        // 静态初始化时注册 JNA 搜索路径，确保在 tess4j 加载 JNA 之前生效
        registerJnaLibraryPathEarly();
    }

    /**
     * Spring 注入腾讯云 OCR 服务（由 Spring 容器启动后调用）
     */
    public static void setTencentOcrService(TencentOcrService service) {
        tencentOcrService = service;
        LOGGER.info("FileContentExtractor 已绑定腾讯云 OCR 服务: {}", service != null ? "可用" : "未注入");
    }

    // ==================== Word 文档提取 ====================

    /**
     * 提取文件文本内容
     *
     * @param inputStream 文件输入流
     * @param fileExtension 文件扩展名（不含点号）
     * @param maxLength 最大返回字符数
     * @return 提取的文本内容；如果无法提取则返回 null
     */
    public static String extract(InputStream inputStream, String fileExtension, int maxLength) {
        if (inputStream == null || StringUtils.isBlank(fileExtension)) {
            return null;
        }

        String ext = fileExtension.toLowerCase().trim();

        // 纯文本
        if (TEXT_EXTENSIONS.contains(ext)) {
            return extractTextFile(inputStream, maxLength);
        }

        // Word 文档
        if (WORD_EXTENSIONS.contains(ext)) {
            return extractWord(inputStream, ext, maxLength);
        }

        // Excel 表格
        if (EXCEL_EXTENSIONS.contains(ext)) {
            return extractExcel(inputStream, ext, maxLength);
        }

        // 图片 OCR
        if (IMAGE_EXTENSIONS.contains(ext)) {
            return extractImageOcr(inputStream, maxLength);
        }

        LOGGER.debug("不支持提取文本的文件格式: {}", ext);
        return null;
    }

    /**
     * 判断文件扩展名是否属于可提取文本的类型
     */
    public static boolean isSupported(String fileExtension) {
        if (StringUtils.isBlank(fileExtension)) {
            return false;
        }
        String ext = fileExtension.toLowerCase().trim();
        return TEXT_EXTENSIONS.contains(ext) || WORD_EXTENSIONS.contains(ext) || EXCEL_EXTENSIONS.contains(ext)
            || IMAGE_EXTENSIONS.contains(ext);
    }

    /**
     * 获取文件类型的友好名称（用于提示用户）
     */
    public static String getTypeName(String fileExtension) {
        if (StringUtils.isBlank(fileExtension)) {
            return "未知";
        }
        String ext = fileExtension.toLowerCase().trim();
        if (TEXT_EXTENSIONS.contains(ext))
            return "纯文本";
        if (WORD_EXTENSIONS.contains(ext))
            return "Word文档";
        if (EXCEL_EXTENSIONS.contains(ext))
            return "Excel表格";
        if (IMAGE_EXTENSIONS.contains(ext))
            return "图片";
        return "不支持的类型";
    }

    // ==================== Excel 表格提取 ====================

    /**
     * 提取纯文本文件内容
     */
    private static String extractTextFile(InputStream inputStream, int maxLength) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1 && sb.length() < maxLength) {
                int remaining = maxLength - sb.length();
                sb.append(buffer, 0, Math.min(read, remaining));
            }
            return sb.toString();
        } catch (IOException e) {
            LOGGER.warn("读取文本文件失败", e);
            return null;
        }
    }

    /**
     * 提取 Word 文档文本内容（支持 .docx 和 .doc）
     */
    private static String extractWord(InputStream inputStream, String ext, int maxLength) {
        try {
            if ("docx".equals(ext)) {
                return extractDocx(inputStream, maxLength);
            } else if ("doc".equals(ext)) {
                return extractDoc(inputStream, maxLength);
            }
        } catch (Exception e) {
            LOGGER.warn("提取Word文档内容失败: {}", ext, e);
        }
        return null;
    }

    /**
     * 提取 .docx 文本（Office Open XML 格式）
     */
    private static String extractDocx(InputStream inputStream, int maxLength) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            if (StringUtils.isBlank(text)) {
                return "";
            }
            return text.length() > maxLength ? text.substring(0, maxLength) : text;
        }
    }

    // ==================== 图片 OCR ====================

    /**
     * 提取 .doc 文本（旧版二进制格式，通过 HWPF）
     */
    private static String extractDoc(InputStream inputStream, int maxLength) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            String text = document.getDocumentText();
            if (StringUtils.isBlank(text)) {
                return "";
            }
            // HWPF 会返回大量控制字符，清理一下
            // text = text.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
            return text.length() > maxLength ? text.substring(0, maxLength) : text;
        }
    }

    /**
     * 提取 Excel 表格文本内容（支持 .xlsx 和 .xls）
     */
    private static String extractExcel(InputStream inputStream, String ext, int maxLength) {
        try {
            Workbook workbook;
            if ("xlsx".equals(ext)) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            return extractWorkbookText(workbook, maxLength);
        } catch (Exception e) {
            LOGGER.warn("提取Excel表格内容失败: {}", ext, e);
            return null;
        }
    }

    /**
     * 遍历 Excel 工作表提取文本
     */
    private static String extractWorkbookText(Workbook workbook, int maxLength) {
        StringBuilder sb = new StringBuilder();
        int numberOfSheets = workbook.getNumberOfSheets();

        for (int i = 0; i < numberOfSheets && sb.length() < maxLength; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            if (StringUtils.isNotBlank(sheetName)) {
                sb.append("【").append(sheetName).append("】\n");
            }

            // 限制最多处理的行数，避免超大文件
            int maxRows = Math.min(sheet.getLastRowNum() + 1, 5000);
            for (int r = 0; r < maxRows && sb.length() < maxLength; r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                int maxCols = Math.min(row.getLastCellNum(), 50); // 限制列数
                for (int c = 0; c < maxCols && sb.length() < maxLength; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null && StringUtils.isNotBlank(getCellString(cell))) {
                        sb.append(getCellString(cell));
                        if (c < maxCols - 1)
                            sb.append("\t");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        try {
            workbook.close();
        } catch (IOException ignored) {
        }

        String result = sb.toString().trim();
        return result.length() > maxLength ? result.substring(0, maxLength) : result;
    }

    /**
     * 读取单元格文本值
     */
    private static String getCellString(Cell cell) {
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    double num = cell.getNumericCellValue();
                    // 判断是否为整数
                    if (num == Math.floor(num) && !Double.isInfinite(num)) {
                        return String.valueOf((long)num);
                    }
                    return String.valueOf(num);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue().trim();
                    } catch (Exception e) {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /** 通过系统命令自动检测 Tesseract 安装目录（缓存结果，仅检测一次） */
    private static String autoDetectTesseractHome() {
        if (tesseractHomeDetectionAttempted)
            return detectedTesseractHome;
        tesseractHomeDetectionAttempted = true;

        if (!System.getProperty("os.name", "").toLowerCase().contains("win"))
            return null;

        // 方式1: 执行 where tesseract 命令查找
        try {
            Process p = Runtime.getRuntime().exec(new String[] {"where", "tesseract"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty())
                        continue;
                    java.io.File exe = new java.io.File(line);
                    java.io.File home = exe.getParentFile();
                    if (home != null && home.exists()) {
                        LOGGER.info("通过 where 命令检测到 Tesseract 安装目录: {}", home.getAbsolutePath());
                        detectedTesseractHome = home.getAbsolutePath();
                        return detectedTesseractHome;
                    }
                }
            }
            p.waitFor();
        } catch (Exception e) {
            LOGGER.debug("通过 where tesseract 查找安装目录失败: {}", e.getMessage());
        }

        // 方式2: 搜索 PATH 环境变量中的 tesseract.exe
        try {
            String pathEnv = System.getenv("PATH");
            if (pathEnv != null) {
                for (String dir : pathEnv.split(java.io.File.pathSeparator)) {
                    if (dir.trim().isEmpty())
                        continue;
                    java.io.File tessExe = new java.io.File(dir.trim(), "tesseract.exe");
                    if (tessExe.exists() && tessExe.isFile()) {
                        LOGGER.info("通过 PATH 环境变量检测到 Tesseract 安装目录: {}", dir.trim());
                        detectedTesseractHome = dir.trim();
                        return detectedTesseractHome;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("搜索 PATH 查找 tesseract.exe 失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 尽早注册 Tesseract DLL 路径到 JNA。
     * <p>
     * 使用 {@link com.sun.jna.NativeLibrary#addSearchPath} 而非 {@code System.setProperty("jna.library.path")}， 因为后者在 JNA
     * 初始化之后设置无效。addSearchPath 支持运行时动态添加搜索路径。
     */
    private static void registerJnaLibraryPathEarly() {
        if (jnaPathRegistered)
            return;
        try {
            // 1. 优先使用 application.yml 中配置的路径
            String configPath = getInstallPath();
            if (registerSearchPathIfValid(configPath)) {
                jnaPathRegistered = true;
                return;
            }

            // 2. 回退：Windows 常见安装目录
            String osName = System.getProperty("os.name", "").toLowerCase();
            if (osName.contains("win")) {
                String[] fallbackPaths = {"C:\\Program Files\\Tesseract-OCR", "C:\\Program Files (x86)\\Tesseract-OCR",
                    "D:\\Program Files\\Tesseract-OCR"};
                for (String p : fallbackPaths) {
                    if (registerSearchPathIfValid(p)) {
                        jnaPathRegistered = true;
                        return;
                    }
                }
            }

            // 3. 通过系统命令自动检测 Tesseract 安装目录
            String autoPath = autoDetectTesseractHome();
            if (registerSearchPathIfValid(autoPath)) {
                jnaPathRegistered = true;
                return;
            }
        } catch (Throwable e) {
            LOGGER.debug("注册 JNA 库路径时出错（忽略）", e);
        }
    }

    /**
     * 验证路径有效并注册到 JNA NativeLibrary 搜索路径
     *
     * @return true=已成功注册
     */
    private static boolean registerSearchPathIfValid(String path) {
        if (StringUtils.isBlank(path))
            return false;
        java.io.File dir = new java.io.File(path);
        if (!dir.exists() || !dir.isDirectory())
            return false;

        // 查找所有 tesseract 相关 DLL（libtesseract-*.dll 或 tesseract*.dll）
        java.io.File[] dlls = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return (lower.startsWith("libtesseract") || lower.startsWith("tesseract")) && lower.endsWith(".dll");
        });
        if (dlls == null || dlls.length == 0) {
            LOGGER.debug("路径 {} 下未找到 tesseract 相关 DLL", path);
            return false;
        }

        // JNA 加载 tesseract 库时查找名为 "tesseract.dll" 的文件。
        // 某些 Tesseract 版本（如 5.5.x）只生成 libtesseract-5.dll，没有 tesseract.dll，
        // 需要创建别名。
        java.io.File tessDll = new java.io.File(dir, "tesseract.dll");
        if (!tessDll.exists()) {
            for (java.io.File dll : dlls) {
                if ("tesseract.dll".equalsIgnoreCase(dll.getName()))
                    continue;
                try {
                    java.nio.file.Files.copy(dll.toPath(), tessDll.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("已创建 JNA DLL 别名: {} → tesseract.dll", dll.getName());
                    break;
                } catch (Exception e) {
                    LOGGER.warn("无法创建 tesseract.dll 副本 (将尝试直接用原名注册): {}", e.getMessage());
                }
            }
        }

        // 注册 JNA 搜索路径（无论复制是否成功都尝试注册，可能通过其他方式找到 DLL）
        try {
            com.sun.jna.NativeLibrary.addSearchPath("tesseract", path);
            LOGGER.info("已注册 JNA tesseract 搜索路径: {}", path);
            return true;
        } catch (Throwable e) {
            LOGGER.debug("注册 JNA 搜索路径失败: {}", path, e);
            return false;
        }
    }

    /**
     * 初始化 Tesseract 实例（线程安全，失败后不再重试）
     *
     * @return Tesseract 实例，不可用时返回 null
     */
    private static net.sourceforge.tess4j.ITesseract getTesseract() {
        // 已成功初始化，直接返回缓存实例
        if (tesseractInstance != null) {
            return (net.sourceforge.tess4j.ITesseract)tesseractInstance;
        }
        synchronized (TESSERACT_LOCK) {
            if (tesseractInstance != null)
                return (net.sourceforge.tess4j.ITesseract)tesseractInstance;
            try {
                // 若 static 块因 Spring 未初始化导致 JNA 路径注册失败，运行时补充注册
                registerJnaLibraryPathEarly();

                // 检测 tessdata 路径并验证语言文件存在
                String datapath = resolveTessDataPath();
                if (datapath == null || !validateTrainedData(datapath)) {
                    String configPath = getInstallPath();
                    LOGGER.warn(
                        "[Tesseract诊断] tessdata 未找到或无效。"
                            + " configurePath={}, resolvedPath={}, env.TESSDATA_PREFIX={}, "
                            + "sys.TESSDATA_PREFIX={}, detectedHome={}, java.library.path={}",
                        StringUtils.defaultIfBlank(configPath, "(未配置)"), datapath,
                        StringUtils.defaultIfBlank(System.getenv("TESSDATA_PREFIX"), "(未设置)"),
                        StringUtils.defaultIfBlank(System.getProperty("TESSDATA_PREFIX"), "(未设置)"),
                        StringUtils.defaultIfBlank(detectedTesseractHome, "(未检测到)"),
                        System.getProperty("java.library.path", "(未知)"));
                    return null;
                }

                net.sourceforge.tess4j.Tesseract tesseract = new net.sourceforge.tess4j.Tesseract();
                tesseract.setDatapath(datapath);
                LOGGER.info("Tesseract datapath 已设置为: {}", datapath);
                tesseract.setLanguage("chi_sim+eng");
                tesseract.setOcrEngineMode(1); // LSTM 模式
                // PSM 3 = 自动版面分割（适合标题+表单+表格混合布局）
                tesseract.setTessVariable("tessedit_pageseg_mode", "3");
                tesseractInstance = tesseract;
                LOGGER.info("Tesseract OCR 引擎初始化成功（PSM=3, 语言={}）", "chi_sim+eng");
                return tesseract;
            } catch (Throwable t) {
                LOGGER.warn(
                    "[Tesseract诊断] 原生库初始化失败。jnaPathRegistered={}, detectedHome={}, java.library.path={}, 异常: {}",
                    jnaPathRegistered, StringUtils.defaultIfBlank(detectedTesseractHome, "(未检测到)"),
                    System.getProperty("java.library.path", "(未知)"), t.toString(), t);
                LOGGER.warn("请确认以下条件:\n" + "  1) Tesseract-OCR 已安装（执行 where tesseract 查看路径）\n"
                    + "  2) 安装目录下有 tessdata 子目录且包含 chi_sim.traineddata 和 eng.traineddata\n"
                    + "  3) 安装目录下有 tesseract.dll 或 libtesseract-5.dll（64位）\n" + "  4) JVM 和 Tesseract 位数一致（都是64位）");
                return null;
            }
        }
    }

    /**
     * 解析 Tesseract tessdata 数据目录路径。
     * <p>
     * <b>注意：</b>对于 Tesseract 5.x / tess4j 5.x，{@code setDatapath()} 需要直接指向 {@code tessdata} 目录本身（即包含 .traineddata
     * 文件的目录），而非其父目录。
     * <p>
     * 优先级：application.yml 配置 > 环境变量 TESSDATA_PREFIX > 系统属性 > Windows 默认路径
     */
    private static String resolveTessDataPath() {
        // 1. 优先使用 application.yml 中配置的路径
        String configPath = getInstallPath();
        if (StringUtils.isNotBlank(configPath)) {
            String resolved = resolveToTessdataDir(configPath);
            if (resolved != null)
                return resolved;
        }

        // 2. 检查环境变量 TESSDATA_PREFIX
        String envPath = System.getenv("TESSDATA_PREFIX");
        if (StringUtils.isNotBlank(envPath)) {
            java.io.File dir = new java.io.File(envPath);
            if (dir.exists() && dir.isDirectory()) {
                return envPath;
            }
        }

        // 3. 检查系统属性
        String sysProp = System.getProperty("TESSDATA_PREFIX");
        if (StringUtils.isNotBlank(sysProp)) {
            java.io.File dir = new java.io.File(sysProp);
            if (dir.exists() && dir.isDirectory()) {
                return sysProp;
            }
        }

        // 4. Windows 常见安装路径
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            String[] winPaths = {"C:\\Program Files\\Tesseract-OCR", "C:\\Program Files (x86)\\Tesseract-OCR",
                "D:\\Program Files\\Tesseract-OCR"};
            for (String p : winPaths) {
                String resolved = resolveToTessdataDir(p);
                if (resolved != null)
                    return resolved;
            }

            // 5. 通过系统命令自动检测 Tesseract 安装目录
            String autoPath = autoDetectTesseractHome();
            if (StringUtils.isNotBlank(autoPath)) {
                String resolved = resolveToTessdataDir(autoPath);
                if (resolved != null)
                    return resolved;
            }
        }

        return null;
    }

    /**
     * 将安装目录路径解析为实际的 tessdata 目录。
     * <ul>
     * <li>如果路径本身是 tessdata 目录（含 .traineddata 文件），直接返回</li>
     * <li>如果路径下有 tessdata 子目录（含 .traineddata 文件），返回子目录</li>
     * <li>都不是则返回 null</li>
     * </ul>
     */
    private static String resolveToTessdataDir(String path) {
        if (StringUtils.isBlank(path))
            return null;
        java.io.File dir = new java.io.File(path);
        if (!dir.exists() || !dir.isDirectory())
            return null;

        // 判断该路径本身是否就是 tessdata 目录（直接包含 .traineddata 文件）
        if (containsTrainedData(dir)) {
            return dir.getAbsolutePath();
        }

        // 判断是否有 tessdata 子目录
        java.io.File tessdata = new java.io.File(dir, "tessdata");
        if (tessdata.exists() && tessdata.isDirectory() && containsTrainedData(tessdata)) {
            return tessdata.getAbsolutePath();
        }

        return null;
    }

    /** 检查目录是否包含 .traineddata 语言文件 */
    private static boolean containsTrainedData(java.io.File dir) {
        java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".traineddata"));
        return files != null && files.length > 0;
    }

    /**
     * 验证 traineddata 语言文件是否存在。
     * <p>
     * 如果 datapath 下有 tessdata 子目录则检查子目录，否则直接检查 datapath 本身。
     *
     * @return true=至少 chi_sim 或 eng 其中一个存在
     */
    private static boolean validateTrainedData(String datapath) {
        if (StringUtils.isBlank(datapath))
            return false;
        java.io.File dir = new java.io.File(datapath);
        java.io.File tessdataDir = new java.io.File(dir, "tessdata");
        if (tessdataDir.exists() && tessdataDir.isDirectory()) {
            dir = tessdataDir;
        }
        java.io.File chiSim = new java.io.File(dir, "chi_sim.traineddata");
        java.io.File eng = new java.io.File(dir, "eng.traineddata");
        boolean chiExists = chiSim.exists() && chiSim.isFile();
        boolean engExists = eng.exists() && eng.isFile();
        if (chiExists || engExists) {
            LOGGER.info("tessdata 语言文件检测: chi_sim={}, eng={}, 路径={}", chiExists, engExists, dir.getAbsolutePath());
            return true;
        }
        LOGGER.warn("tessdata 目录下未找到语言文件: 路径={}, 请下载 chi_sim.traineddata 和 eng.traineddata", dir.getAbsolutePath());
        return false;
    }

    /**
     * 从 application.yml 的 tesseract.install-path 配置中获取安装路径
     * <p>
     * 通过 {@link TesseractOcrProperties} 静态桥接获取， 避免工具类直接依赖 Spring 容器。
     */
    private static String getInstallPath() {
        try {
            return TesseractOcrProperties.getInstallPath();
        } catch (Exception e) {
            LOGGER.debug("读取 Tesseract 配置失败", e);
            return "";
        }
    }

    /**
     * 图片 OCR 文字识别
     * <p>
     * 依赖 Tesseract OCR 引擎。如果服务器未安装 Tesseract，将返回 null。
     */
    private static String extractImageOcr(InputStream inputStream, int maxLength) {
        try {
            // 读取图片字节，验证是有效图片
            byte[] imageBytes = readBytes(inputStream);
            if (imageBytes == null || imageBytes.length == 0) {
                return null;
            }

            // 验证是否为有效图片
            try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes)) {
                if (ImageIO.read(bis) == null) {
                    LOGGER.warn("无法解析图片文件");
                    return null;
                }
            }

            // 1. 优先使用腾讯云 OCR（识别率显著高于 Tesseract，适合中文+表格场景）
            TencentOcrService ocrService = tencentOcrService;
            if (ocrService != null) {
                try {
                    String cloudText = ocrService.recognizeText(imageBytes);
                    if (cloudText != null) {
                        String trimmed = cloudText.trim();
                        if (!trimmed.isEmpty()) {
                            LOGGER.info("腾讯云 OCR 识别成功，字符数={}", trimmed.length());
                            return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
                        }
                    }
                    LOGGER.info("腾讯云 OCR 未配置或返回空，回退到本地 Tesseract");
                } catch (Exception cloudErr) {
                    LOGGER.warn("腾讯云 OCR 异常，回退到本地 Tesseract: {}", cloudErr.getMessage());
                }
            }

            // 2. 回退到本地 Tesseract OCR
            net.sourceforge.tess4j.ITesseract tesseract = getTesseract();
            if (tesseract == null) {
                return null;
            }

            // 图像预处理：灰度化 + 二值化 + 去噪，提升 OCR 质量
            java.awt.image.BufferedImage processedImage = preprocessImageForOcr(imageBytes);
            if (processedImage == null) {
                return null;
            }

            // 调用 Tesseract OCR（带语言回退：chi_sim+eng → eng）
            // 每次识别前重置语言，避免上次回退到 eng 后污染实例
            String text = doOcrWithFallback(tesseract, processedImage);
            if (StringUtils.isBlank(text)) {
                // 预处理图像没有识别到文字，尝试用原图回退（中值滤波可能误伤了文字）
                try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes)) {
                    java.awt.image.BufferedImage rawImage = ImageIO.read(bis);
                    if (rawImage != null && rawImage != processedImage) {
                        LOGGER.info("预处理图像 OCR 无结果，回退使用原图识别");
                        text = doOcrWithFallback(tesseract, rawImage);
                    }
                } catch (Exception fallbackErr) {
                    LOGGER.debug("原图回退 OCR 失败: {}", fallbackErr.getMessage());
                }
            }
            if (StringUtils.isBlank(text)) {
                LOGGER.info("图片 OCR 无识别结果（预处理和原图回退均未识别到文字）");
                return "";
            }
            text = text.trim();
            return text.length() > maxLength ? text.substring(0, maxLength) : text;
        } catch (Throwable t) {
            String errMsg = t.toString();
            LOGGER.warn("图片 OCR 识别失败: {}", errMsg, t);
            // 原生内存访问错误通常表明 Tesseract 环境配置有问题（如 tessdata 路径不对或 DLL 版本不匹配）
            // 清空实例后下次调用会重新尝试初始化（如果环境已修复则自动恢复）
            if (t instanceof java.lang.Error && errMsg.contains("Invalid memory access")) {
                tesseractInstance = null;
                LOGGER.warn("检测到 Tesseract 原生内存错误，已清空 OCR 实例（下次调用将自动重试）。请检查: "
                    + "1) TESSDATA_PREFIX 环境变量是否指向 Tesseract-OCR 安装目录 "
                    + "2) tessdata 目录下是否存在 chi_sim.traineddata 和 eng.traineddata "
                    + "3) Tesseract 和 JVM 的位数是否一致（都是 32 位或都是 64 位）");
            }
            return null;
        }
    }

    /**
     * 执行 OCR 识别，依次尝试多种 PSM 版面分割模式 + 语言回退，取最长识别结果。
     * <p>
     * 不同 PSM 模式适合不同布局：
     * <ul>
     * <li>PSM=6 — 单一块文本（适合整页文字）</li>
     * <li>PSM=11 — 稀疏文本（适合表格/表单中的分散文字）</li>
     * <li>PSM=4 — 单列文本（适合竖排或单列布局）</li>
     * <li>PSM=3 — 全自动（兜底）</li>
     * </ul>
     * 最终返回所有 PSM 模式中识别结果最长的非空文本。
     */
    private static String doOcrWithFallback(net.sourceforge.tess4j.ITesseract tesseract,
        java.awt.image.BufferedImage image) {
        // 先尝试 chi_sim+eng 组合语言，再用多 PSM 模式
        String best = doOcrMultiPsm(tesseract, image, "chi_sim+eng", new String[] {"6", "11", "4", "3"});
        if (StringUtils.isNotBlank(best)) {
            return best;
        }
        // 组合语言无结果，回退到纯英文
        LOGGER.debug("组合语言 (chi_sim+eng) 多PSM尝试无结果，回退到纯英文模式");
        return doOcrMultiPsm(tesseract, image, "eng", new String[] {"6", "11", "3"});
    }

    /**
     * 用指定语言和多个 PSM 模式分别执行 OCR，返回最长非空结果
     */
    private static String doOcrMultiPsm(net.sourceforge.tess4j.ITesseract tesseract, java.awt.image.BufferedImage image,
        String lang, String[] psmModes) {
        tesseract.setLanguage(lang);
        String best = "";
        for (String psm : psmModes) {
            try {
                tesseract.setTessVariable("tessedit_pageseg_mode", psm);
                String text = tesseract.doOCR(image);
                if (text != null) {
                    String trimmed = text.trim();
                    if (trimmed.length() > best.length()) {
                        LOGGER.debug("OCR PSM={} lang={} 识别到 {} 字符", psm, lang, trimmed.length());
                        best = trimmed;
                    }
                }
            } catch (Throwable ocrErr) {
                LOGGER.debug("OCR PSM={} lang={} 失败: {}", psm, lang, ocrErr.getMessage());
            }
        }
        return best;
    }

    // ==================== 工具方法 ====================

    /**
     * 读取输入流为字节数组
     */
    public static byte[] readBytes(InputStream inputStream) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            LOGGER.warn("读取文件字节失败", e);
            return null;
        }
    }

    /**
     * 为 OCR 预处理图片：灰度化 → 2x 放大 → 中值滤波（去表格线）→ 白边填充 → 对比度拉伸。
     * <p>
     * 核心思路：
     * <ul>
     * <li><b>中值滤波</b>：表格线（1-2px 宽）在 5×5 窗口中属于统计异常值，会被当作噪点抹除； 文字笔画（3-5px 宽）是窗口中的"主流"像素，得以保留</li>
     * <li>放大控制在 2x，用双线性插值（避免双三次的振铃伪影）</li>
     * <li>不做外部二值化 — Tesseract 内部自适应阈值更可靠</li>
     * </ul>
     */
    private static java.awt.image.BufferedImage preprocessImageForOcr(byte[] imageBytes) {
        try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes)) {
            java.awt.image.BufferedImage src = ImageIO.read(bis);
            if (src == null) {
                LOGGER.warn("无法解析图片（ImageIO 返回 null）");
                return null;
            }

            // 1. 灰度化
            java.awt.image.BufferedImage gray;
            if (src.getType() == java.awt.image.BufferedImage.TYPE_BYTE_GRAY) {
                gray = src;
            } else {
                gray = new java.awt.image.BufferedImage(src.getWidth(), src.getHeight(),
                    java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
                java.awt.Graphics2D g2 = gray.createGraphics();
                g2.drawImage(src, 0, 0, null);
                g2.dispose();
            }

            // 2. 智能放大：根据原图宽度动态选择放大倍率（最少 1x、最多 3x，目标 2400px 宽）
            // 上限 3x 而非 4x：避免过度放大导致处理变慢且 Tesseract 内部分辨率超限
            int TARGET_WIDTH = 2400;
            double scale = (double)TARGET_WIDTH / gray.getWidth();
            if (scale > 3.0)
                scale = 3.0;
            if (scale < 1.0)
                scale = 1.0;
            if (scale > 1.01) {
                int newW = (int)(gray.getWidth() * scale);
                int newH = (int)(gray.getHeight() * scale);
                java.awt.image.BufferedImage scaled =
                    new java.awt.image.BufferedImage(newW, newH, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
                java.awt.Graphics2D g3 = scaled.createGraphics();
                g3.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g3.drawImage(gray, 0, 0, newW, newH, null);
                g3.dispose();
                gray = scaled;
            }

            // 3. 白边填充 — 防止 Tesseract 边缘裁切
            // 注：去掉了中值滤波和对比度拉伸 — 这两步会增强表格网格线、放大噪声，
            // 对于扫描文档/表单类型图片反而降低 OCR 准确率。Tesseract 内部已有自适应阈值。
            int PAD = 20;
            int padW = gray.getWidth() + PAD * 2;
            int padH = gray.getHeight() + PAD * 2;
            java.awt.image.BufferedImage padded =
                new java.awt.image.BufferedImage(padW, padH, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
            java.awt.Graphics2D gPad = padded.createGraphics();
            gPad.setColor(java.awt.Color.WHITE);
            gPad.fillRect(0, 0, padW, padH);
            gPad.drawImage(gray, PAD, PAD, null);
            gPad.dispose();
            gray = padded;

            return gray;
        } catch (Throwable e) {
            LOGGER.warn("图片预处理失败，将使用原图进行 OCR: {}", e.getMessage());
            try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes)) {
                return ImageIO.read(bis);
            } catch (Exception ex) {
                LOGGER.warn("回退到原图也失败", ex);
                return null;
            }
        }
    }

    /**
     * 中值滤波器 — 用窗口内像素的中位数替换中心像素。
     * <p>
     * 作用：抑制薄线条（表格边框、网格线）和椒盐噪声，保留文字笔画。 表格线通常 1-2px 宽，在 5×5 窗口中是少数派 → 被中位数"投票出局"； 文字笔画 3-5px 宽，在窗口中占多数 → 中位数落在笔画值，得以保留。
     *
     * @param src 灰度源图
     * @param radius 窗口半径（radius=2 → 5×5 窗口）
     * @return 滤波后的图像
     */
    private static java.awt.image.BufferedImage medianFilter(java.awt.image.BufferedImage src, int radius) {
        int kernelDim = 2 * radius + 1; // 5 for radius=2
        int kernelSize = kernelDim * kernelDim;
        int w = src.getWidth();
        int h = src.getHeight();
        java.awt.image.BufferedImage result =
            new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        java.awt.image.WritableRaster srcRaster = src.getRaster();
        java.awt.image.WritableRaster dstRaster = result.getRaster();
        int[] window = new int[kernelSize];

        for (int y = radius; y < h - radius; y++) {
            for (int x = radius; x < w - radius; x++) {
                int idx = 0;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        window[idx++] = srcRaster.getSample(x + dx, y + dy, 0);
                    }
                }
                java.util.Arrays.sort(window);
                dstRaster.setSample(x, y, 0, window[kernelSize / 2]);
            }
        }
        // 边缘像素直接复制（不做滤波处理，因为是白边区域，不影响 OCR）
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < radius; x++) {
                dstRaster.setSample(x, y, 0, srcRaster.getSample(x, y, 0));
                dstRaster.setSample(w - 1 - x, y, 0, srcRaster.getSample(w - 1 - x, y, 0));
            }
        }
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < radius; y++) {
                dstRaster.setSample(x, y, 0, srcRaster.getSample(x, y, 0));
                dstRaster.setSample(x, h - 1 - y, 0, srcRaster.getSample(x, h - 1 - y, 0));
            }
        }
        return result;
    }
}
