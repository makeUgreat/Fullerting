interface CropType {
  packDiaryId: number;
  cropTypeName: string;
  packDiaryTitle: string;
  packDiaryCulStartAt: string;
  packDiaryCulEndAt: string | null;
  packDiaryGrowthStep: string;
  packDiaryCreatedAt: string;
  cropTypeImgUrl: string;
}

interface DiaryType {
  diaryId: number;
  packDiaryId: number;
  diaryBehavior: "다이어리" | "물주기";
  diaryTitle: string | null;
  diaryContent: string | null;
  diarySelectedAt: string;
  diaryCreatedAt: string;
}

interface TipType {
  cropTipId: number;
  cropTipGrowthStep: number;
  cropTipContent: string;
}