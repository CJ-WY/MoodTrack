package org.example.dto;

import lombok.Data;

@Data
public class PaginationInfo {
    private int current_page;
    private int per_page;
    private long total_count;
    private int total_pages;
    private boolean has_next;
    private boolean has_prev;
}
