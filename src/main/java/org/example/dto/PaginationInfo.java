package org.example.dto;

import lombok.Data;

@Data
public class PaginationInfo {
    private int currentPage;
    private int perPage;
    private long totalCount;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrev;
}