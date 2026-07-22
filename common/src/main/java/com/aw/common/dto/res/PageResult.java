package com.aw.common.dto.res;

import java.util.List;

public class PageResult<T> {
    private List<T> items;
    private long totalRows;
    private int page;
    private int pageSize;

    public PageResult(List<T> items, long totalRows, int page, int pageSize) {
        this.items = items;
        this.totalRows = totalRows;
        this.page = page;
        this.pageSize = pageSize;
    }

    // Getters and Setters
}
