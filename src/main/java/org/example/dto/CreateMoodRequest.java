package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.model.EmotionType;

import java.util.List;

@Data
public class CreateMoodRequest {

    @NotNull(message = "情绪类型不能为空")
    @JsonProperty("mood_type")
    private EmotionType moodType;

    @Size(max = 500, message = "心情描述最多500字符")
    @JsonProperty("mood_description")
    private String moodDescription;

    @Size(max = 10, message = "最多10个触发器")
    private List<@Size(max = 50, message = "每个触发器最多50字符") String> triggers;

    @JsonProperty("share_to_public")
    private boolean shareToPublic = true;

    @JsonProperty("record_time")
    private String recordTime;

    @JsonProperty("is_anonymous")
    private boolean isAnonymous = false;
}