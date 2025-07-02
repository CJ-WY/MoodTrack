package org.example.service;

import org.example.model.MoodEntry;
import org.example.repository.MoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param moodEntry 包含用户情绪数据的 {@link MoodEntry} 对象。
     * @return 保存到数据库后的 {@link MoodEntry} 对象。
     * @throws RuntimeException 如果数据库操作失败。
     */
    @Override
    @Transactional // 确保数据库操作的原子性
    public MoodEntry submitMoodEntry(MoodEntry moodEntry) {
        try {
            // 设置情绪记录时间为当前系统时间
            moodEntry.setRecordTime(LocalDateTime.now());
            // 保存情绪记录到数据库
            return moodEntryRepository.save(moodEntry);
        } catch (Exception e) {
            // 捕获数据库操作异常，并抛出运行时异常
            throw new RuntimeException("提交情绪记录失败。", e);
        }
    }
}
