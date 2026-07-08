package luxaeterna.itforum.common;

public class PageRequest {
    private int page = 1;
    private int size = 20;
    private String sort;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page < 1 ? 1 : page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size < 1 || size > 100 ? 20 : size; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public int getOffset() { return (page - 1) * size; }
}
