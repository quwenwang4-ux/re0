package com.seafish.common;

import java.util.List;

//响应类

public class PageResponse<T> {

    private final List<T> records;
    private final long total;
    private final long page;
    private final long size;
    private final long pages;

    public PageResponse(
            List<T> records,
            long total,
            long page,
            long size,
            long pages
    ) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }

    public long getPage() {
        return page;
    }

    public long getSize() {
        return size;
    }

    public long getPages() {
        return pages;
    }
}