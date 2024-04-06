package com.ssafy.fullerting.record.diary.service;

import com.ssafy.fullerting.record.diary.model.dto.request.CreateDiaryRequest;
import com.ssafy.fullerting.record.diary.model.dto.request.UpdateDiaryRequest;
import com.ssafy.fullerting.record.diary.model.dto.request.WateringCropsRequest;
import com.ssafy.fullerting.record.diary.model.dto.response.GetAllDiaryResponse;
import com.ssafy.fullerting.record.diary.model.dto.response.GetDetailDiaryResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DiaryService {
    List<GetAllDiaryResponse> getAllDiary(Long packDiaryId);
    GetDetailDiaryResponse getDetailDiary(Long diaryId);
    void createDiary(Long packDiaryId, CreateDiaryRequest createDiaryRequest);
    void updateDiary(Long diaryId, UpdateDiaryRequest updateDiaryRequest);
    void deleteDiary(Long diaryId);
    void wateringCrops(Long packDiaryId, WateringCropsRequest wateringCropsRequest);
}
