package com.cpt202.projectselection.service;

public class PageRequest {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final int page;
    private final int size;

    public PageRequest(Integer page, Integer size) {
        int normalizedPage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int normalizedSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        this.page = normalizedPage;
        this.size = normalizedSize;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}
