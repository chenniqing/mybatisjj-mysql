package cn.javaex.mybatisjj.basic.common;

/**
 * 数据库类型
 * 
 * @author 陈霓清
 */
public enum DbType {
	
	MYSQL("mysql", "MySQL 数据库"),
	MARIADB("mariadb", "MariaDB 数据库"),
	ORACLE("oracle", "Oracle11g 及以下数据库"),
	ORACLE_12C("oracle12c", "Oracle12c 及以上数据库"),
	DB2("db2", "DB2 数据库"),
	H2("h2", "H2 数据库"),
	HSQL("hsql", "HSQL 数据库"),
	SQLITE("sqlite", "SQLite 数据库"),
	POSTGRE_SQL("postgresql", "PostgreSQL 数据库"),
	SQL_SERVER2005("sqlserver2005", "SQLServer2005 数据库"),
	SQL_SERVER("sqlserver", "SQLServer 数据库"),
	DM("dm", "达梦数据库"),
	XUGU("xugu", "虚谷数据库"),
	KINGBASE_ES("kingbasees", "人大金仓数据库"),
	PHOENIX("phoenix", "Phoenix HBase 数据库"),
	GAUSS("gauss", "Gauss 数据库"),
	CLICK_HOUSE("clickhouse", "ClickHouse 数据库"),
	GBASE("gbase", "南大通用(华库)数据库"),
	GBASE_8S("gbase-8s", "南大通用数据库 GBase 8s"),
	OSCAR("oscar", "神通数据库"),
	SYBASE("sybase", "Sybase ASE 数据库"),
	OCEAN_BASE("OceanBase", "OceanBase 数据库"),
	FIREBIRD("Firebird", "Firebird 数据库"),
	DERBY("derby", "Derby 数据库"),
	HIGH_GO("highgo", "瀚高数据库"),
	CUBRID("cubrid", "CUBRID 数据库"),
	GOLDILOCKS("goldilocks", "GOLDILOCKS 数据库"),
	CSIIDB("csiidb", "CSIIDB 数据库"),
	HANA("hana", "SAP_HANA 数据库"),
	IMPALA("impala", "Impala 数据库"),
	VERTICA("vertica", "Vertica 数据库"),
	XCLOUD("xcloud", "行云数据库"),
	REDSHIFT("redshift", "亚马逊 redshift 数据库"),
	OPEN_GAUSS("openGauss", "华为 openGauss 数据库"),
	TDENGINE("TDengine", "TDengine 数据库"),
	INFORMIX("informix", "Informix 数据库"),
	GREENPLUM("greenplum", "Greenplum 数据库"),
	UXDB("uxdb", "优炫数据库"),
	DORIS("Doris", "Doris数据库"),
	HIVE_SQL("Hive SQL", "Hive 数据库"),
	LEALONE("lealone", "Lealone 数据库"),
	SINODB("sinodb", "星瑞格数据库"),
	OTHER("other", "其他数据库");
	
	private final String code;
	private final String description;
	
	private DbType(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * 根据配置值解析数据库类型
	 * @param code
	 * @return
	 */
	public static DbType fromCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return OTHER;
		}
		String normalizedCode = normalize(code);
		for (DbType dbType : values()) {
			if (normalize(dbType.code).equals(normalizedCode) || normalize(dbType.name()).equals(normalizedCode)) {
				return dbType;
			}
		}
		return OTHER;
	}
	
	/**
	 * 根据JDBC元信息解析数据库类型
	 * @param productName
	 * @param url
	 * @return
	 */
	public static DbType fromJdbc(String productName, String url) {
		return fromJdbc(productName, url, 0);
	}
	
	/**
	 * 根据JDBC元信息解析数据库类型
	 * @param productName
	 * @param url
	 * @param majorVersion 数据库主版本号
	 * @return
	 */
	public static DbType fromJdbc(String productName, String url, int majorVersion) {
		String text = ((productName == null ? "" : productName) + " " + (url == null ? "" : url)).toLowerCase();
		if (text.contains("mariadb")) return MARIADB;
		if (text.contains("oceanbase")) return OCEAN_BASE;
		if (text.contains("gbase-8s") || text.contains("informix-sqli")) return GBASE_8S;
		if (text.contains("opengauss")) return OPEN_GAUSS;
		if (text.contains("greenplum")) return GREENPLUM;
		if (text.contains("redshift")) return REDSHIFT;
		if (text.contains("kingbase")) return KINGBASE_ES;
		if (text.contains("highgo")) return HIGH_GO;
		if (text.contains("postgresql")) return POSTGRE_SQL;
		if (text.contains("oracle")) return majorVersion > 0 && majorVersion < 12 ? ORACLE : ORACLE_12C;
		if (text.contains("sql server") || text.contains("microsoft")) return SQL_SERVER;
		if (text.contains("mysql")) return MYSQL;
		if (text.contains("db2")) return DB2;
		if (text.contains("sqlite")) return SQLITE;
		if (text.contains("hsql")) return HSQL;
		if (text.contains("h2")) return H2;
		if (text.contains("dm dbms") || text.contains("dm jdbc") || text.contains(":dm:")) return DM;
		if (text.contains("xugu")) return XUGU;
		if (text.contains("phoenix")) return PHOENIX;
		if (text.contains("gauss")) return GAUSS;
		if (text.contains("clickhouse")) return CLICK_HOUSE;
		if (text.contains("gbase")) return GBASE;
		if (text.contains("oscar")) return OSCAR;
		if (text.contains("sybase")) return SYBASE;
		if (text.contains("firebird")) return FIREBIRD;
		if (text.contains("derby")) return DERBY;
		if (text.contains("cubrid")) return CUBRID;
		if (text.contains("goldilocks")) return GOLDILOCKS;
		if (text.contains("csiidb")) return CSIIDB;
		if (text.contains("sap") || text.contains("hana")) return HANA;
		if (text.contains("impala")) return IMPALA;
		if (text.contains("vertica")) return VERTICA;
		if (text.contains("xcloud")) return XCLOUD;
		if (text.contains("tdengine")) return TDENGINE;
		if (text.contains("informix")) return INFORMIX;
		if (text.contains("uxdb")) return UXDB;
		if (text.contains("doris")) return DORIS;
		if (text.contains("hive")) return HIVE_SQL;
		if (text.contains("lealone")) return LEALONE;
		if (text.contains("sinodb")) return SINODB;
		return OTHER;
	}
	
	/**
	 * 归一化数据库类型名称，兼容大小写、中横线、下划线和空格
	 * @param value
	 * @return
	 */
	private static String normalize(String value) {
		return value == null ? "" : value.replace("-", "").replace("_", "").replace(" ", "").toLowerCase();
	}
	
}
