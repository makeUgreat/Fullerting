import styled from "styled-components";

interface CropProfileType {
  crop: CropType;
  direction?: "row" | "column";
}

const CropProfileBox = styled.div`
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
`;

const RowBox = styled.div`
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: flex-start;
`;

const ColumnBox = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  align-items: center;
  justify-content: center;
`;

const CropImageBox = styled.div`
  width: 6.25rem;
  height: 6.25rem;
  flex-shrink: 0;
  border-radius: 50%;
  border: 2.5px solid #3d0c112c;
  margin: 0.6rem;
`;

const CropImg = styled.img`
  width: 100%;
  height: auto;
  border-radius: 50%;
`;

const CropInfoBox = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const CropInfoBoxCenter = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
`;

const CropTitle = styled.div`
  font-size: 1rem;
  font-weight: bold;
`;

const CropDescriptionBox = styled.div`
  color: ${({ theme }) => theme.colors.gray0};
  font-size: 0.75rem;
  font-weight: bold;
`;

const CropEnd = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 0.64rem;
  background-color: ${({ theme }) => theme.colors.sub0};
  padding: 0.25rem 0.5rem;
  border-radius: 0.78125rem;
  line-height: 1rem;
  font-weight: bold;
`;

const CropProfile = ({ crop, direction }: CropProfileType) => {
  const Box = direction === "column" ? ColumnBox : RowBox;
  const Align = direction === "column" ? CropInfoBoxCenter : CropInfoBox;

  const calculateDDay = (createdAt: string) => {
    const today = new Date();
    const createdDate = new Date(createdAt);
    const diffTime = Math.abs(today.getTime() - createdDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return `D+${diffDays}`;
  };

  const calculateDDayDifference = (startDate: string, endDate: string) => {
    const startMillis = new Date(startDate).getTime();
    const endMillis = new Date(endDate).getTime();

    const differenceMillis = Math.abs(endMillis - startMillis);
    const differenceDays = Math.ceil(differenceMillis / (1000 * 60 * 60 * 24));

    return differenceDays + 1;
  };

  return (
    <CropProfileBox>
      <Box>
        <CropImageBox>
          <CropImg src={crop.cropTypeImgUrl} alt="" />
        </CropImageBox>
        <Align>
          <CropTitle>
            <p>{crop.packDiaryTitle}</p>
          </CropTitle>
          <CropDescriptionBox>
            <span>
              {crop.cropTypeName} {crop.packDiaryGrowthStep}단계
            </span>
            <span> · </span>
            {crop.packDiaryCulEndAt === null ? (
              <span>{calculateDDay(crop.packDiaryCulStartAt)}</span>
            ) : (
              <span>수확😊</span>
            )}
          </CropDescriptionBox>
          {direction !== "column" && (
            <CropEnd>
              {crop.packDiaryCulEndAt === null
                ? `" 수확까지 ${
                    crop.cropGrowDay !== null ? crop.cropGrowDay : "?"
                  }일 남았습니다 "`
                : `${crop.packDiaryCulStartAt} ~ ${
                    crop.packDiaryCulEndAt
                  } (${calculateDDayDifference(
                    crop.packDiaryCulStartAt,
                    crop.packDiaryCulEndAt
                  )}일)`}
            </CropEnd>
          )}
        </Align>
      </Box>
    </CropProfileBox>
  );
};

export default CropProfile;
