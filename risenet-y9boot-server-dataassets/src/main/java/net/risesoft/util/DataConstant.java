package net.risesoft.util;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据工具类
 * 
 * @author pzx
 *
 */
public class DataConstant {

    public static final String ES = "elasticsearch";

    public static final String MYSQL = "mysql";

    public static final String ORACLE = "oracle";

    public static final String DM = "dm";

    public static final String PG = "postgresql";

    public static final String KINGBASE = "kingbase";

    public static final String FTP = "ftp";

    public static final String SQLSERVER = "sqlserver";

    public static String getDirver(String type) {
        switch (type) {
            case MYSQL:
                return "com.mysql.cj.jdbc.Driver";
            case ORACLE:
                return "oracle.jdbc.OracleDriver";
            case PG:
                return "org.postgresql.Driver";
            case KINGBASE:
                return "com.kingbase8.Driver";
            case DM:
                return "dm.jdbc.driver.DmDriver";
            case SQLSERVER:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                return "";
        }
    }

    public static String getShareType(Integer type) {
        switch (type) {
            case 0:
                return "不予共享";
            case 1:
                return "有条件共享";
            case 2:
                return "无条件共享";
            default:
                return "";
        }
    }

    /**
     * 获取昨天日期
     * 
     * @return 昨天日期
     */
    public static String getYesterday() {
        // 1. 获取当前日期（本地日期，不含时间）
        LocalDate today = LocalDate.now();
        
        // 2. 减去1天，得到昨天的日期
        LocalDate yesterday = today.minusDays(1);
        return yesterday.toString();
    }

    public static void main(String[] args) {
        String chineseChar = "？"; // 中文字符
        String hex = textToHex(chineseChar);
        System.out.println("16进制: " + hex);
        System.out.println("中文：" + hexToText(hex));
        try {
            // saveArticle("测试1",
            // "那是我故乡邻居院里的石榴，邻居爷爷的石榴比我们家早种了三年。那时，我家的院落空荡荡的，那石榴树便霸占着那一片暖阳，肆意伸展着它那繁茂的枝条，可当我们也种下那一棵承载着我满满的希望的小石榴树时，它却只能在那被占据一半的方寸之地默默伸展自己的枝芽，点点阳光丝丝缕缕透过大石榴的密叶打在小石榴树的叶片上，可大石榴树却独占了大片阳光，因此每次邻居爷爷来登门拜访时，我心中总是忿忿不平，特别是那丰收时节，我却最讨厌那天，对比我家小树枝头的清一色——绿，邻居爷爷家的石榴却个大饱满，红润而鲜亮，枝丫上硕果累累。对比之下，只觉得我的小树娇小可怜。一天，我拖着疲惫的身子回到家，一眼便看到了茶几上那鲜红的石榴，我狼吞虎咽地拿起吃了起来，吃着吃着我突然想起了邻居爷爷，这应该便是邻居爷爷的石榴了。我突然脸胀得通红，想起自己以前的忿忿不平，我感到十分羞愧。想起邻居爷爷傍晚与家人们饮酒畅谈，想起他帮我家菜园浇水，才恍然知道他的慈祥。他那布满褶皱的手中的石榴，也在这一刻成了温馨又美丽的风景。");
            // saveArticle("测试2",
            // "当车偏离了城市中心地带时，路变得坑坑洼洼了。人们在车上颠簸着，一连几个急刹车，我整个人仿佛快要被甩出去。我吃力的抓着车上的扶杆，车上小朋友吃的零食的味道在一瞬间被无限放大，这让本就晕车的我变得更加不堪。窗外似红的太阳直射着车窗，刺眼的阳光恶狠狠地打在我的脸颊上。身旁的那位奶奶许是察觉到了什么，犹犹豫豫地开口道：“小姑娘，是哪里不舒服吗？”我同样点点头作回应，我想开口说话，但我的胃像潮汐一般翻涌不停。奶奶又问道：“是不是晕车了？”我没有回答。奶奶自顾自的在她的小帆布包中努力寻找着什么东西，过了一会儿，她拿出一个小小的翡翠色的瓶子，翠绿的液体在阳光下晶莹剔透。奶奶开始解释：“这是风油精，晕车的话可以涂一点在太阳穴和手腕上。”话音落下时，她把风油精塞到了我的手中，只见她思索了一会儿又拿了回去。我拉着我的胳膊，用右手拧开瓶盖，滴了几滴在我的手腕处，随后又用左手轻轻的在我的太阳穴处抹了一点。清凉的感觉刹那间在我心头蔓延开来。我看着那位奶奶出了神，她和蔼的脸庞上有一些沟壑，那是岁月留下的足迹，她的青丝已变成了白发，那是时间进行的洗礼。即使岁月剥夺了她美丽的容颜，令她青春不再，但她依然像一位天使一般出现在我的人生中。当这一幕画面在我的生命中定格成永久时，又何谈不是一种风景呢？");
            // saveArticle("测试3",
            // "笔若长枪，势如破竹。我坐在争夺时间的战场上，手持笔尖，在绵白如雪的纸张上书写人生篇章。阳光洒下，是欢声笑语的操场，是严肃静穆的考场，亦是莘莘学子的归乡。一串银铃般的笑声飘过，一个个音符在我身边萦绕，带走了全身的焦燥不安。花儿不知何时因战胜困难而溅泪。我相信，这次我成功。");
            // saveArticle("测试4",
            // "“你看看你，这颜色是怎么涂的？又没涂均匀，这构图也有问题。面对着美术老师，我深深地低下了头，好似有千万条尖刀刺进了心灵。下课后，手里拿着那张不合格的作品，垂头叹气地慢慢向前走着，从教室往家走要经过一条小道，那绿色的树叶从白色的墙面伸过来，向着阳光生长，随风舞动，沙沙作响。心中竟产出一种念头：不想坚持，由于心情原因，我很反感这些沙沙作响的树叶，便采下几片、将它散成一团丢向远处的天空。夕阳从树叶缭中落下，在地面上留下金黄的小湖，我伸出去遮阳，夕阳光凝聚在手掌中，突然，眼前的你似风景般，与周围的环境融合一起，闪闪发光。你是多么高雅、朴素，富有艺术感的大风衣随风起舞，你左手拿着画笔，在画板上点缀，而你的右手顺着大风衣的袖子轻轻垂下，你鼻梁上的眼镜被夕阳照着，眼神中露着些许温柔和一丝凄凉。");
            // saveArticle("测试5",
            // "下了车，雨还在继续下着，正如我的心情一般，后悔没有听母亲的话将雨伞带好，湿漉漉的感觉真的很烦躁。于是我将书包顶在头顶往家的方向跑去。突然，好似雨晴了，周围不再有雨滴打在身上，但我仍然能听见雨水滴嗒的声音。抬头望去，原来是我的母亲，她用手将我脸肤上的水撩去，她的手凉凉的。我鼻子一酸，不知明的情绪在眼中绽开，脸肤上划过一滴水。");
            // saveArticle("测试6",
            // "今年，茉莉花香溢满整个小区，它洁白高大，枝繁叶茂。茉莉花的清香飘进了奶奶的房间，如小铃铛般呼唤着奶奶。周围的邻居也纷纷围了过来，一年时间，那茉莉花好像从哭哭啼啼在风中呜咽的小女孩长大成为亭亭玉立的少女，洁白无瑕。邻居们也纷纷感到震撼。奶奶的脸上流露着比那茉莉更洁白的笑容。这让奶奶在车水马龙的城市里找到田园风光的快乐。");
            // saveArticle("测试7",
            // "晨熹微露，孤星渐隐，破晓时的寒意中，我用物理题拉开清晨的序幕。力学这头“拦路虎”，仿佛压城的“片片黑云”，我披坚执锐细细观察敌军的破绽。伴随着悠悠槐花香，一个公式闪着金光定格在脑海，算出结果合上笔的郗那，竟生出了战士刀剑出鞘的畅快之感。");
            // saveArticle("测试8",
            // "上天给了他们一双听不见声音的耳朵，却没有给他们一个看不清明天的未来。在这热闹的小吃街，他们凭借自己的努力，与健全人做着生意上的竞争。乐观、豁达、坚韧就是他们的人生宣言，与他们相比，我学习上的一点困难又算什么呢？“苔花如米小，也学牡丹开”这对聋哑夫妻用自己的拼搏、努力、乐观谱写着自己生命的小诗。我也要像他们一样，乐观豁达地面对生活。");
            System.out.println(searchArticles("？"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String textToHex(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder hexString = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // 补零对齐
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String hexToText(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            String byteStr = hex.substring(i, i + 2);
            bytes[i / 2] = (byte)Integer.parseInt(byteStr, 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // 存储文章
    public static void saveArticle(String title, String content) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/abc", "root", "111111");
        String hexContent = textToHex(content);
        String sql = "INSERT INTO articles (title, hex_content) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, hexContent);
            stmt.executeUpdate();
        }

        if (connection != null) {
            connection.close();
        }
    }

    public static List<String> searchArticles(String keyword) throws SQLException {
        List<String> results = new ArrayList<>();
        String queryHex = textToHex(keyword); // 查询词转为16进制
        String sql = "SELECT hex_content FROM articles WHERE hex_content LIKE ?";
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/abc", "root", "111111");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + queryHex + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String hexContent = rs.getString("hex_content");
                if (hexContent.length() % 6 != 0) {// 验证是否6位
                    System.out.println("========" + keyword);
                }
                results.add(hexToText(hexContent)); // 还原为原文显示
            }
        }
        if (connection != null) {
            connection.close();
        }
        return results;
    }
}
