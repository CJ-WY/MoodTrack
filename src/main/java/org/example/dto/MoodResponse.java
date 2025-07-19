package org.example.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class MoodResponse {
    private Long id;
    private Long user_id;
    private String emotion_type;
    private String mood_description;
    private List<String> triggers;
    private OffsetDateTime record_time;
    private OffsetDateTime created_at;
    private OffsetDateTime updated_at;
}
