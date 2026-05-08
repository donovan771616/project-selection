package com.cpt202.projectselection.service;

import java.util.Collections;
import java.util.List;

public class PageResult<T> {

    private final List<T> rows;
    private final int page;
    private final int size;
    private final int total;
    private final int totalPages;

    public PageResult(List<T> rows, int page, int size, int total) {
        this.rows = rows == null ? Collections.emptyList() : rows;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
    }

    public List<T> getRows() {
        return rows;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < totalPages;
    }

    public int getPreviousPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(totalPages, page + 1);
    }
}
