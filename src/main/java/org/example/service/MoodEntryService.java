package org.example.service;

import org.example.model.MoodEntry;

/**
 * 情绪记录服务接口。
 * <p>
 * 定义了与用户情绪记录相关的业务逻辑操作。
 * </p>
 */
public interface MoodEntryService {

    /**
     * 提交用户的情绪记录。
     *
     * @param moodEntry 包含用户情绪数据的 MoodEntry 对象。
     * @return 保存到数据库后的 MoodEntry 对象。
     */
    MoodEntry submitMoodEntry(MoodEntry moodEntry);
}