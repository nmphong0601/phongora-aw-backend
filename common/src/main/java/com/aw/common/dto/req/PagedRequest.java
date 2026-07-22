package com.aw.common.dto.req;

import lombok.Data;

@Data
public class PagedRequest {
    private Integer index;
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer totalRows;

    public int getOffset() {
        return (Math.max(1, page) - 1) * pageSize;
    }
}
