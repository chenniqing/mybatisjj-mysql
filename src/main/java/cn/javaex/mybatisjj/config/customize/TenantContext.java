package cn.javaex.mybatisjj.config.customize;

/**
 * 租户
 * 
 * @author 陈霓清
 * @Date 2026年2月8日
 */
public class TenantContext {
	private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<>();

	// 设置
	public static void setTenantId(String id) {
		TENANT_HOLDER.set(id);
	}

	// 获取
	public static String getTenantId() {
		return TENANT_HOLDER.get();
	}

	// 清理（防止内存泄漏，通常在请求结束后调用）
	public static void clear() {
		TENANT_HOLDER.remove();
	}
}
