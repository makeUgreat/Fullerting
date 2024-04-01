import styled from "styled-components";
import cartIcon from "../../assets/svg/cart.svg";
import diaryIcon from "../../assets/svg/diary.svg";
import { useQuery } from "@tanstack/react-query";
import { getAlarms, readAlarm } from "../../apis/Alarm";
import { useNavigate } from "react-router-dom";
interface NotificationProps {
  alarmType: "커뮤니티" | "작물거래";
  alarmContent: string;
  alarmRedirect: string;
  alarmId: number;
  checked: boolean;
}

const NotificationList = styled.div`
  display: flex;
  flex-direction: column;
`;
const Notification = styled.div<NotificationProps>`
  color: var(--gray1, #8c8c8c);
  display: flex;
  align-items: center;
  font-family: "GamtanRoad Dotum TTF";
  font-size: 0.73rem;

  &::before {
    content: "";
    display: block;
    width: 0.75rem;
    height: 0.75rem;
    margin-right: 5px;
    background-image: url(${(props) =>
      props.alarmType === "커뮤니티" ? diaryIcon : cartIcon});
    background-size: cover;
    background-repeat: no-repeat;
  }
`;
const NotificationItem = styled.div<{ isNew: boolean }>`
  font-family: "GamtanRoad Dotum TTF";
  font-size: 0.9rem;
  font-weight: 400;
  line-height: 2rem;
  width: 23.45rem;
  height: 5.5rem;
  padding: 0.6rem 1.8rem;
  background-color: ${(props) => (props.isNew ? "none" : "#E5F9DB")};
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;

  &:first-child {
    border-top: 1.5px solid #e5e7eb;
  }

  min-height: 5.5rem; /* 고정된 높이에서 최소 높이로 변경 */
  height: auto; /* 높이를 자동으로 조정하도록 설정 */
`;

const AllAlarm = () => {
  const navigate = useNavigate();
  const {
    data: mockNotifications,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["alarms"],
    queryFn: getAlarms,
  });

  if (isLoading) {
    console.log("데이터 로딩 중...");
    return <div>로딩중..</div>;
  }

  if (error) {
    console.error("알람 데이터를 가져오는데 실패했습니다:", error);
    return <div>알람 데이터를 가져오는데 실패했습니다: {error.message}</div>;
  }
  // console.log("alarm", mockNotifications);
  const sortedNotifications = [...mockNotifications].sort((a, b) => b.alarmId - a.alarmId);

  const parseContent = (content: string): React.ReactNode => {
    const regex = /#(.*?)#/g;
    const parts = content.split(regex);

    return parts.map((part, index) => {
      if (index % 2 === 1) {
        return <strong key={index} style={{ color: '#A0D8B3', fontWeight: 'bold' }} >{part}</strong>;
      } else {
        return part;
      }
    });
  };
  // const sortedNotifications = mockNotifications.slice().sort((a, b) => {
  //   return Number(a.checked) - Number(b.checked);
  // });

  const handleClick = async (alarmId: number) => {
    try {
      await readAlarm(alarmId);
      const notification = sortedNotifications.find(
        notification => notification.alarmId === alarmId
      );
      // console.log(`Notification ${alarmId} clicked.`);
      navigate(notification.alarmRedirect);
    } catch (error) {
      console.error(`알람 ${alarmId} 처리 중 오류 발생: `, error);
    }
  };

  return (
    <div>
      <NotificationList>
        {sortedNotifications.map((notification) => (
          <NotificationItem
            key={notification.alarmId}
            isNew={notification.checked}
            onClick={() => handleClick(notification.alarmId)}
          >
            <Notification alarmType={notification.alarmType}>
              {notification.alarmType}
              {/* {notification.alarmId} */}
            </Notification>
            <div>{parseContent(notification.alarmContent)}</div>
          </NotificationItem>
        ))}
      </NotificationList>
    </div>
  );
};

export default AllAlarm;
