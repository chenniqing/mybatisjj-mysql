package cn.javaex.mybatisjj.config.dialect;

import cn.javaex.mybatisjj.basic.common.DbType;
import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * 数据库方言注册器
 * 
 * @author 陈霓清
 */
public class DbDialectRegistry {
	
	private static final DbDialect MYSQL_DIALECT = new MySqlDialect();
	private static final DbDialect LIMIT_OFFSET_DIALECT = new LimitOffsetDialect();
	private static final DbDialect OFFSET_FETCH_DIALECT = new OffsetFetchDialect();
	private static final DbDialect POSTGRE_SQL_DIALECT = new PostgreSqlDialect();
	private static final DbDialect SQLITE_DIALECT = new SqliteDialect();
	private static final DbDialect H2_DIALECT = new H2Dialect();
	private static final DbDialect HSQL_DIALECT = new HsqlDialect();
	private static final DbDialect DB2_DIALECT = new Db2Dialect();
	private static final DbDialect ORACLE_DIALECT = new OracleDialect();
	private static final DbDialect SQL_SERVER_DIALECT = new SqlServerDialect();
	private static final DbDialect SQL_SERVER2005_DIALECT = new SqlServer2005Dialect();
	private static final DbDialect FIREBIRD_DIALECT = new FirebirdDialect();
	private static final DbDialect INFORMIX_DIALECT = new InformixDialect();
	
	private DbDialectRegistry() {
		
	}
	
	/**
	 * 根据数据库类型获取方言
	 * @param dbType
	 * @return
	 */
	public static DbDialect getDialect(DbType dbType) {
		if (dbType == null) {
			return LIMIT_OFFSET_DIALECT;
		}
		
		switch (dbType) {
			case MYSQL:
			case MARIADB:
			case OCEAN_BASE:
			case CLICK_HOUSE:
			case GBASE:
			case GBASE_8S:
			case CUBRID:
			case DORIS:
			case LEALONE:
			case TDENGINE:
				return MYSQL_DIALECT;
			case ORACLE:
				return ORACLE_DIALECT;
			case DB2:
			case DERBY:
				return DB2_DIALECT;
			case SQL_SERVER2005:
				return SQL_SERVER2005_DIALECT;
			case SQL_SERVER:
				return SQL_SERVER_DIALECT;
			case FIREBIRD:
				return FIREBIRD_DIALECT;
			case INFORMIX:
				return INFORMIX_DIALECT;
			case H2:
				return H2_DIALECT;
			case HSQL:
				return HSQL_DIALECT;
			case SQLITE:
				return SQLITE_DIALECT;
			case POSTGRE_SQL:
			case KINGBASE_ES:
			case GAUSS:
			case HIGH_GO:
			case REDSHIFT:
			case OPEN_GAUSS:
			case GREENPLUM:
			case UXDB:
				return POSTGRE_SQL_DIALECT;
			case ORACLE_12C:
			case DM:
			case XUGU:
			case OSCAR:
			case SYBASE:
			case GOLDILOCKS:
			case CSIIDB:
			case HANA:
				return OFFSET_FETCH_DIALECT;
			case PHOENIX:
			case IMPALA:
			case VERTICA:
			case XCLOUD:
			case HIVE_SQL:
			case SINODB:
			case OTHER:
			default:
				return LIMIT_OFFSET_DIALECT;
		}
	}
	
	/**
	 * 基础方言
	 */
	private static abstract class AbstractDbDialect implements DbDialect {
		
		@Override
		public String buildCountSql(String sql) {
			return "SELECT COUNT(1) FROM (" + sql + ") temp";
		}
		
		@Override
		public String getIdentitySql() {
			return null;
		}
		
	}
	
	/**
	 * MySQL风格分页方言
	 */
	private static class MySqlDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			return sql + " LIMIT " + offset + ", " + limit;
		}
		
		@Override
		public String getIdentitySql() {
			return "SELECT LAST_INSERT_ID()";
		}
		
	}
	
	/**
	 * LIMIT/OFFSET风格分页方言
	 */
	private static class LimitOffsetDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			return sql + " LIMIT " + limit + " OFFSET " + offset;
		}
		
	}
	
	/**
	 * PostgreSQL兼容分页方言
	 */
	private static class PostgreSqlDialect extends LimitOffsetDialect {
		
		@Override
		public String getIdentitySql() {
			return "SELECT LASTVAL()";
		}
		
	}
	
	/**
	 * SQLite分页方言
	 */
	private static class SqliteDialect extends LimitOffsetDialect {
		
		@Override
		public String getIdentitySql() {
			return "SELECT LAST_INSERT_ROWID()";
		}
		
	}
	
	/**
	 * H2分页方言
	 */
	private static class H2Dialect extends LimitOffsetDialect {
		
		@Override
		public String getIdentitySql() {
			return "CALL IDENTITY()";
		}
		
	}
	
	/**
	 * HSQL分页方言
	 */
	private static class HsqlDialect extends LimitOffsetDialect {
		
		@Override
		public String getIdentitySql() {
			return "CALL IDENTITY()";
		}
		
	}
	
	/**
	 * SQL标准OFFSET/FETCH分页方言
	 */
	private static class OffsetFetchDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			return sql + " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
		}
		
	}
	
	/**
	 * DB2/Derby分页方言
	 */
	private static class Db2Dialect extends OffsetFetchDialect {
		
		@Override
		public String getIdentitySql() {
			return "VALUES IDENTITY_VAL_LOCAL()";
		}
		
	}
	
	/**
	 * Oracle11g及以下分页方言
	 */
	private static class OracleDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			long end = offset + limit;
			return "SELECT * FROM ( SELECT TMP_PAGE.*, ROWNUM ROW_ID FROM ( " + sql
					+ " ) TMP_PAGE WHERE ROWNUM <= " + end + " ) WHERE ROW_ID > " + offset;
		}
		
		@Override
		public String getIdentitySql() {
			return null;
		}
		
	}
	
	/**
	 * SQLServer2012及以上分页方言
	 */
	private static class SqlServerDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			return appendDefaultOrderBy(sql) + " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
		}
		
		@Override
		public String getIdentitySql() {
			return "SELECT SCOPE_IDENTITY()";
		}
		
	}
	
	/**
	 * SQLServer2005分页方言
	 */
	private static class SqlServer2005Dialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			long start = offset + 1;
			long end = offset + limit;
			return "SELECT * FROM ( SELECT TMP_PAGE.*, ROW_NUMBER() OVER (ORDER BY (SELECT 0)) AS ROW_ID FROM ( "
					+ sql + " ) TMP_PAGE ) TMP_PAGE2 WHERE ROW_ID BETWEEN " + start + " AND " + end;
		}
		
		@Override
		public String getIdentitySql() {
			return "SELECT SCOPE_IDENTITY()";
		}
		
	}
	
	/**
	 * Firebird分页方言
	 */
	private static class FirebirdDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			return sql + " ROWS " + (offset + 1) + " TO " + (offset + limit);
		}
		
	}
	
	/**
	 * Informix分页方言
	 */
	private static class InformixDialect extends AbstractDbDialect {
		
		@Override
		public String buildPaginationSql(String sql, long offset, long limit) {
			return sql.replaceFirst("(?i)^\\s*select\\s+", "SELECT SKIP " + offset + " FIRST " + limit + " ");
		}
		
	}
	
	/**
	 * SQLServer分页必须带ORDER BY，没有排序时补一个稳定占位排序
	 * @param sql
	 * @return
	 */
	private static String appendDefaultOrderBy(String sql) {
		if (SqlStringUtils.isEmpty(sql) || sql.toLowerCase().contains(" order by ")) {
			return sql;
		}
		return sql + " ORDER BY (SELECT 0)";
	}
	
}
