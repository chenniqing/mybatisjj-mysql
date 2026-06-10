package cn.javaex.mybatisjj.pagehelper;

/**
 * 分页设置
 * 
 * @author 陈霓清
 * @Date 2024年6月28日
 */
public class PageHelper {

	private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();

	public static void startPage(int pageNum, int pageSize) {
		Page page = new Page(pageNum, pageSize);
		LOCAL_PAGE.set(page);
	}

	public static void clearPage() {
		LOCAL_PAGE.remove();
	}

	public static Page getPage() {
		return LOCAL_PAGE.get();
	}

	public static class Page {
		private int pageNum;
		private int pageSize;
		private Long total;
		private boolean used;

		public Page(int pageNum, int pageSize) {
			this.pageNum = pageNum;
			this.pageSize = pageSize;
		}

		public int getPageNum() {
			return pageNum;
		}

		public void setPageNum(int pageNum) {
			this.pageNum = pageNum;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public Long getTotal() {
			return total;
		}

		public void setTotal(Long total) {
			this.total = total;
		}
		
		public boolean isUsed() {
			return used;
		}
		
		public void setUsed(boolean used) {
			this.used = used;
		}

	}
}
