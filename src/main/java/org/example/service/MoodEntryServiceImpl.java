package org.example.service;

import org.example.model.MoodEntry;
import org.example.repository.MoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 情绪记录服务实现类。
 * <p>
 * 实现了 {@link MoodEntryService} 接口，处理用户情绪记录的业务逻辑。
 * </p>
 */
@Service
public class MoodEntryServiceImpl implements MoodEntryService {

    /**
     * 情绪记录数据仓库，用于与数据库进行交互。
     */
    @Autowired
    private MoodEntryRepository moodEntryRepository;

    /**
     * 提交用户的情绪记录。
     * <p>
     * 在保存情绪记录之前，会自动设置记录时间为当前时间。
     * </p>
     *
     * @param moodEntry 包含用户情绪数据的 MoodEntry 对象。
     * @return 保存到数据库后的 MoodEntry 对象。
     */
    @Override
    public MoodEntry submitMoodEntry(MoodEntry moodEntry) {
        moodEntry.setRecordTime(LocalDateTime.now());
        return moodEntryRepository.save(moodEntry);
    }
}