package org.example.service;

import org.example.dto.CreateMoodRequest;
import org.example.dto.MoodResponse;
import org.example.dto.PaginatedMoodResponse;
import org.example.dto.PaginationInfo;
import org.example.model.EmotionType;
import org.example.model.MoodEntry;
import org.example.model.User;
import org.example.repository.MoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoodEntryServiceImpl implements MoodEntryService {

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Override
    public PaginatedMoodResponse getMoods(User user, Pageable pageable, LocalDate startDate, LocalDate endDate, EmotionType emotionType) {
        Specification<MoodEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordTime"), startDate.atStartOfDay().atOffset(ZoneOffset.UTC)));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordTime"), endDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC)));
            }
            if (emotionType != null) {
                predicates.add(cb.equal(root.get("emotionType"), emotionType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MoodEntry> page = moodEntryRepository.findAll(spec, pageable);
        List<MoodResponse> moodResponses = page.getContent().stream().map(this::convertToResponse).collect(Collectors.toList());

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrent_page(page.getNumber() + 1);
        paginationInfo.setPer_page(page.getSize());
        paginationInfo.setTotal_count(page.getTotalElements());
        paginationInfo.setTotal_pages(page.getTotalPages());
        paginationInfo.setHas_next(page.hasNext());
        paginationInfo.setHas_prev(page.hasPrevious());

        PaginatedMoodResponse response = new PaginatedMoodResponse();
        response.setMoods(moodResponses);
        response.setPagination(paginationInfo);

        return response;
    }

    @Override
    public MoodEntry createMood(CreateMoodRequest request, User user) {
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setUser(user);
        moodEntry.setEmotionType(request.getMoodType());
        moodEntry.setMoodDescription(request.getMoodDescription());
        moodEntry.setTriggers(request.getTriggers());
        moodEntry.setShareToPublic(request.isShareToPublic());
        moodEntry.setAnonymous(request.isAnonymous());
        moodEntry.setRecordTime(request.getRecordTime() != null ? OffsetDateTime.parse(request.getRecordTime()) : OffsetDateTime.now());
        return moodEntryRepository.save(moodEntry);
    }

    private MoodResponse convertToResponse(MoodEntry moodEntry) {
        MoodResponse response = new MoodResponse();
        response.setId(moodEntry.getId());
        response.setUser_id(moodEntry.getUser().getId());
        response.setEmotion_type(moodEntry.getEmotionType().name());
        response.setMood_description(moodEntry.getMoodDescription());
        response.setTriggers(moodEntry.getTriggers());
        response.setRecord_time(moodEntry.getRecordTime());
        response.setCreated_at(moodEntry.getCreatedAt());
        response.setUpdated_at(moodEntry.getUpdatedAt());
        return response;
    }
}