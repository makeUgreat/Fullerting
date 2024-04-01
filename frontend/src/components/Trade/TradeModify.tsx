import styled from "styled-components";
import StyledInput from "../../components/common/Input/StyledInput";
import TradePostLayout from "../../components/common/Layout/TradePostLayout";
import useInput from "../../hooks/useInput";
import NonCheck from "/src/assets/svg/noncheck.svg";
import Check from "/src/assets/svg/check.svg";
import { useEffect, useState } from "react";
import StyledTextArea from "../../components/common/Input/StyledTextArea";
import CheckModal from "../../components/Trade/finishModal";
import SelectModal from "../../components/Trade/SelectModal";
import Diary from "/src/assets/svg/plus-diary.svg";
import Camera from "/src/assets/svg/camera.svg";
import MultiFileUploadInput from "../../components/common/Input/MultiFileUploadInput";
import { useMutation } from "@tanstack/react-query";
import { usePost, useUpdateArticle } from "../../apis/TradeApi";
import { useAtom } from "jotai";
import { imageFilesAtom, oldImagesAtom, selectedDiaryIdAtom } from "../../stores/trade";
import { BottomButton } from "../common/Button/LargeButton";
import { useLocation, useNavigate } from "react-router-dom";
import FileUploadInput from "../common/Input/FileUploadInput";

interface BackGround {
  backgroundColor?: string;
  zIndex?: number;
}
interface ImageInfo {
  id: number;
  imgStoreUrl: string;
}
const DiarySelectedText = styled.span`
  display: flex;
  font-size: 0.875rem;
  color: #2a7f00;
  margin-left: 0.5rem;
`;
const PreviewImage = styled.img`
  width: 4.2rem;
  height: 4.2rem;
  border-radius: 1rem;
  background: ${({ theme }) => theme.colors.white};
`;
const RadioBox = styled.div`
  width: 100%;
  height: auto;
  justify-content: space-around;
  gap: 0.5rem;
  display: flex;
  flex-direction: column;
`;
const RadioBoxContainer = styled.div`
  width: auto;
  justify-content: flex-start;
  gap: 0.5rem;
  align-items: center;
  display: flex;
  flex-direction: row;
`;
const SelectContainer = styled.div`
  width: auto;
  justify-content: space-between;
  gap: 0.12rem;
  align-items: center;
  display: flex;
  flex-direction: row;
`;
const TitleText = styled.div`
  width: auto;
  font-size: 0.875rem; // 제목의 글씨 크기 조정
  color: #8c8c8c; // 제목의 글씨 색상
  font-weight: bold;
`;

const DeleteImageButton = styled.div`
  position: absolute;
  top: -0.5rem;
  right: -0.5rem;
  width: 1.5rem;
  height: 1.5rem;
  background-color: rgba(59, 59, 59, 0.804);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;

  svg {
    width: 0.7rem;
    height: 0.7rem;
    fill: #ffffff;
  }
`;
const BiddingBox = styled.div`
  background-color: #eee;
  width: 100%;
  padding: 0.625rem 0.9375rem;
  border: 1px solid #d3d3d3;
  height: 3rem;
  border-radius: 0.5rem;
  align-items: center;
  display: flex;
`;
const CashText = styled.div`
  width: auto;
  font-size: 0.875rem;
  font-style: normal;
  font-weight: 400;
  color: #000000;
`;
const DiaryBox = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-start;
  width: 19.875rem;
  gap: 0.6rem;
`;
const Label = styled.label`
  display: flex;
  color: ${({ theme }) => theme.colors.gray0};
  text-align: center;
  font-size: 0.875rem;
  font-weight: bold;
`;
const SelectBackGround = styled.div<BackGround>`
  position: fixed; // 화면 전체에 고정되도록 설정
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: ${(props) =>
    props.backgroundColor || "rgba(0, 0, 0, 0.5)"}; // 기본 배경색 설정
  z-index: ${(props) =>
    props.zIndex || 1000}; // 다른 컨텐츠 위에 오도록 z-index 설정
  display: flex;
  justify-content: center; // 자식 요소들을 가운데 정렬
  align-items: center; // 자식 요소들을 수직 중앙 정렬
`;
const DiarySquare = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: row;
  width: 4.2rem;
  height: 4.2rem;
  border-radius: 1rem;
  border: 2px solid ${({ theme }) => theme.colors.gray1};
  background: ${({ theme }) => theme.colors.white};
`;

const RegisterBox = styled.div`
  display: flex;
  gap: 0.8rem;
  flex-wrap: wrap;
`;


const TradeModify = () => {

  const location = useLocation();
  const isEditMode = Boolean(location.state);
  const [title, setTitle] = useInput(location.state?.exArticleTitle || "");
  const [check, setCheck] = useState([true, false, false]);
  const [cashCheck, setCashCheck] = useState<boolean>(false);
  const [cash, setCash] = useInput(location.state?.deal_cur_price || "");
  const [place, setPlace] = useInput(location.state?.ex_article_location || "");
  const [imageFiles, setImageFiles] = useAtom(imageFilesAtom);
  const postId = location.state?.postId;
  const [imageArray, setImageArray] = useState(
    location.state?.imageResponse || ""
  );

  const [images, setImages] = useAtom(oldImagesAtom);
  // console.log("타입을 알려주세요", typeof imageArray);

  const [showDiary, setShowDiary] = useState(
    location.state?.packdiaryid || null
  );
  const [content, setContent] = useInput(
    location.state?.exArticleContent || ""
  );
  const [tradeType, setTradeType] = useState(
    location.state?.exArticleType || ""
  );

  console.log(location?.state);

  let test: Array<number> = [];
  const [diary, setSelectedDiaryId] = useAtom(selectedDiaryIdAtom);
  const [modal, setModal] = useState<boolean>(false);
  const handleCashClick = () => {
    setCashCheck(!cashCheck);
  };
  const handleDiarySelect = (diaryId: number) => {
    setSelectedDiaryId(diaryId);
    setModal(false); // 모달 닫기
  };
  const openModal = () => {
    setModal(true);
  };
  const [isLoading, setIsLoading] = useState(false);



  // const { mutate } = useMutation({
  //   mutationFn: update,
  //   onSuccess: () => {
  //     navigate(`/diary/${diaryId}`);
  //     setIsLoading(false);
  //     setSelectedFiles([]);
  //   },
  //   onError: (error) => {
  //     console.log(error);
  //     setIsLoading(false);
  //   },

  // });


  useEffect(() => {
    console.log(document.body);
    // 모달이 열리면 body의 overflow를 hidden으로 설정
    if (modal) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }

    console.log('lengthhhhhhhhhhhhhhh '+location.state.imageResponse.length)
    setImages(location.state.imageResponse);
    // 컴포넌트가 언마운트 될 때 원래 상태로 복구
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [modal]);
  useEffect(() => {
    if (isEditMode && location.state?.imageResponse) {
      // imageResponse 배열에서 필요한 정보만 추출하여 setImageFiles 호출
      const initialImages = location.state.imageResponse.map(
        (img: ImageInfo) => img.imgStoreUrl
      ); // 예시 코드, 실제 구조에 맞게 조정 필요
      setImageFiles(initialImages);
    }
  }, [isEditMode, location.state, setImageFiles]);
  const tradeOptions = [
    { title: "제안", value: "DEAL" },
    { title: "일반 거래", value: "GENERAL_TRANSACTION" },
    { title: "나눔", value: "SHARING" },
  ];
  // console.log("저는 로케이션", location.state);
  const [selectedFiles, setSelectedFiles] = useAtom(imageFilesAtom);
  const { mutate: handlePost } = usePost();
  const navigate = useNavigate();
  const { mutate: handleModified } = useUpdateArticle();

  const handleCheckClick = async () => {

    console.log("저 여기 왔어요", 111111);
    const formData = new FormData();
    console.log(location.state.imageResponse[0].imgStoreUrl);

    if (!selectedFiles || selectedFiles.length === 0) {
      console.log("nullllllllllllllll")
      formData.append("images", "");
    }

    else {
      console.log("ffffffffffffffff")

      // selectedFiles에 있는 각 파일을 FormData에 추가
      selectedFiles.forEach((file) => {
        formData.append("images", file);
      });
    }

    // if (selectedFiles.length === 0) { // 없어도 1이라 여기는 안와
    //   // 이미지 파일이 없을 경우 빈 문자열을 서버에 보냅니다.
    //   console.log("no imaggggggggggggg")
    //   formData.append("images", new Blob([], { type: "application/json" }));
    // }


    console.log("imagggggggggggggggg"+images)

    const updateInfo = {
      exArticleTitle: title,
      exArticleContent: content,
      ex_article_location: place,
      exArticleType: tradeType,
      packdiaryid: showDiary,
      dealCurPrice: cash,
      // unmodifiedimageid: [],
      images: images.map((img) => img.id),

      // 이곳에 수정할 다른 필드 정보를 추가합니다.
    };

    formData.append(
      "updateInfo",
      new Blob([JSON.stringify(updateInfo)], { type: "application/json" })
    );
    try {
      await handleModified({ postId, formData });
      setSelectedFiles([]);
      setSelectedDiaryId(null);
      navigate(-1);
      // 요청 성공 후 페이지 이동 또는 상태 업데이트
      // navigate("/trade");
    } catch (error) {
      // 오류 처리
      console.error("업로드 실패:", error);
    }
  };


  // const handleDeleteImage = (index: number) => {
  //   const updatedFiles = [...selectedFiles];
  //   updatedFiles.splice(index, 1);
  //   setSelectedFiles(updatedFiles);

  //   const updatedPreviewURLs = [...previewURLs];
  //   updatedPreviewURLs.splice(index, 1);
  //   setPreviewURLs(updatedPreviewURLs);
  // };


  const handleDeleteImage = (id: number) => {
    setImages(images.filter((img) => img.id !== id));
  };


  return (
    <>
      {modal && (
        <SelectBackGround backgroundColor="rgba(4.87, 4.87, 4.87, 0.28)">
          <SelectModal
            closeModal={() => setModal(false)}
            onDiarySelect={handleDiarySelect}
          />
        </SelectBackGround>
      )}
      <TradePostLayout title="작물거래">
        <StyledInput
          label="제목"
          type="text"
          id="title"
          name="title"
          placeholder="제목을 입력해주세요"
          value={title}
          onChange={setTitle}
          isRequired={false}
        />
        <RadioBox>
          <TitleText>거래 방법</TitleText>
          <RadioBoxContainer>
            {tradeOptions.map((option, index) => (
              <SelectContainer
                key={index}
                onClick={() => setTradeType(option.value)}
              >
                <img
                  src={tradeType === option.value ? Check : NonCheck}
                  alt={option.title}
                  style={{ marginRight: "0.12rem" }}
                />
                <TitleText>{option.title}</TitleText>
              </SelectContainer>
            ))}
          </RadioBoxContainer>
        </RadioBox>
        <RadioBox>
          <TitleText>거래 단위</TitleText>
          <RadioBoxContainer>
            {cashCheck === true ? (
              <SelectContainer>
                <img
                  src={Check}
                  alt="check"
                  onClick={handleCashClick}
                  style={{ marginRight: "0.12rem" }}
                />{" "}
                <TitleText>현금</TitleText>
              </SelectContainer>
            ) : (
              <SelectContainer>
                <img
                  src={NonCheck}
                  alt="check"
                  onClick={handleCashClick}
                  style={{ marginRight: "0.12rem" }}
                />{" "}
                <TitleText>현금</TitleText>
              </SelectContainer>
            )}
          </RadioBoxContainer>
        </RadioBox>
        {/* <StyledInput
          label="시작가"
          type="text"
          id="startcash"
          name="startcash"
          placeholder="₩ 가격을 입력해주세요."
          value={cash}
          onChange={setCash}
          isRequired={false}
        /> */}
        <BiddingBox>{cash}원</BiddingBox>
        <RadioBox>
          <TitleText>입찰 단위</TitleText>
          <BiddingBox>
            <CashText>100원</CashText>
          </BiddingBox>
        </RadioBox>
        <StyledInput
          label="거래 희망 장소"
          type="text"
          id="place"
          name="place"
          placeholder="거래 장소를 입력해주세요."
          value={place}
          onChange={setPlace}
          isRequired={false}
        />
        <StyledTextArea
          label="내용"
          name="content"
          placeholder="내용을 입력해주세요."
          value={content}
          onChange={setContent}
          maxLength={300}
          isRequired={false}
        />
        <DiaryBox>
          <Label>작물 일지</Label>
          <DiarySquare onClick={openModal}>
            <img src={Diary} alt="diary" />
          </DiarySquare>
          {diary && <DiarySelectedText>선택되었습니다</DiarySelectedText>}
        </DiaryBox>

        <DiaryBox>
          <MultiFileUploadInput />
        </DiaryBox>


        {/* <RegisterBox>
          {images.map((image) => (
            <div key={image.id} style={{ position: "relative" }}>
              <PreviewImage
                src={image.imgStoreUrl}
                alt={`Preview ${image.id}`}
              />
              <DeleteImageButton
                onClick={() => handleDeleteImage(image.id)}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                >
                  <path
                    d="M3 3L21 21M21 3L3 21"
                    stroke="white"
                    strokeWidth="5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </DeleteImageButton>
            </div>
          ))}

        </RegisterBox> */}

      </TradePostLayout>
      <BottomButton
        onClick={handleCheckClick}
        text="수정하기
      "
      />
    </>
  );
};

export default TradeModify;
