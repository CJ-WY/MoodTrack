package org.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedMoodResponse {
    private List<MoodResponse> moods;
    private PaginationInfo pagination;
}
