package com.ssafy.fullerting.record.packdiary.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdatePackDiaryRequest {
    private String packDiaryTitle;
    private LocalDate packDiaryCulStartAt;
}
