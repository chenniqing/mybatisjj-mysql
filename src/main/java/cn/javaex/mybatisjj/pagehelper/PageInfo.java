package cn.javaex.mybatisjj.pagehelper;

import java.util.List;

/**
 * 分页信息
 * 
 * @author 陈霓清
 * @Date 2024年7月12日
 * @param <T>
 */
public class PageInfo<T> {
	private long total;                         // 总条数
	private List<T> list;                       // 数据
	private int pageNum;                        // 当前页
	private int pageSize;                       // 每页的数量
	private int size;                           // 当前页的数量
	private int startRow;                       // 当前页面第一个元素在数据库中的行号
	private int endRow;                         // 当前页面最后一个元素在数据库中的行号
	private int pages;                          // 总页数
	private int prePage;                        // 前一页
	private int nextPage;                       // 下一页
	private boolean isFirstPage = false;        // 是否为第一页
	private boolean isLastPage = false;         // 是否为最后一页
	private boolean hasPreviousPage = false;    // 是否有前一页
	private boolean hasNextPage = false;        // 是否有下一页

	public PageInfo(List<T> list) {
		this.list = list;
		
		PageHelper.Page page = PageHelper.getPage();
		if (page == null) {
			this.total = list == null ? 0 : list.size();
			this.pageNum = 1;
			this.pageSize = list == null ? 0 : list.size();
			this.size = list == null ? 0 : list.size();
			this.startRow = this.size > 0 ? 1 : 0;
			this.endRow = this.size;
			this.pages = this.pageSize > 0 ? 1 : 0;
			this.prePage = 0;
			this.nextPage = 0;
			this.isFirstPage = true;
			this.isLastPage = true;
			this.hasPreviousPage = false;
			this.hasNextPage = false;
			return;
		}
		
		this.total = page.getTotal() == null ? 0L : page.getTotal();
		this.pageNum = page.getPageNum();
		this.pageSize = page.getPageSize();
		this.size = list == null ? 0 : list.size();
		
		// 由于结果是>startRow的，所以实际的需要 + 1
		if (this.size == 0) {
			this.startRow = 0;
			this.endRow = 0;
		} else {
			this.startRow = (this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0) + 1;
			// 计算实际的endRow（最后一页的时候特殊）
			this.endRow = this.startRow - 1 + this.size;
		}
		
		// 计算总页数
		if (this.pageSize > 0) {
			this.pages = (int) (this.total / this.pageSize + ((this.total % this.pageSize == 0) ? 0 : 1));
		} else {
			this.pages = 0;
		}
		
		// 计算前一页和后一页
		if (this.pageNum > 1) {
			this.prePage = this.pageNum - 1;
		}
		if (this.pageNum < this.pages) {
			this.nextPage = this.pageNum + 1;
		}
		
		// 判断页面边界
		this.isFirstPage = this.pageNum == 1;
		this.isLastPage = this.pageNum == this.pages || this.pages == 0;
		this.hasPreviousPage = this.pageNum > 1;
		this.hasNextPage = this.pageNum < this.pages;
		
		// 清除分页参数以防影响后续操作
		PageHelper.clearPage();
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
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

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public int getEndRow() {
		return endRow;
	}

	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public int getPrePage() {
		return prePage;
	}

	public void setPrePage(int prePage) {
		this.prePage = prePage;
	}

	public int getNextPage() {
		return nextPage;
	}

	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}

	public boolean isFirstPage() {
		return isFirstPage;
	}

	public void setFirstPage(boolean isFirstPage) {
		this.isFirstPage = isFirstPage;
	}

	public boolean isLastPage() {
		return isLastPage;
	}

	public void setLastPage(boolean isLastPage) {
		this.isLastPage = isLastPage;
	}

	public boolean isHasPreviousPage() {
		return hasPreviousPage;
	}

	public void setHasPreviousPage(boolean hasPreviousPage) {
		this.hasPreviousPage = hasPreviousPage;
	}

	public boolean isHasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

}
