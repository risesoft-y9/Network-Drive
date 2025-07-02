package net.risesoft.util;

/**
 * 数据工具类
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
	
}
