import {
  LayoutMainBox,
  LayoutInnerBox,
} from "../../components/common/Layout/Box";
import MyTrade from "../../components/mypage/trade/MyTrade";
import useInput from "../../hooks/useInput";
import { NavBar, TopBar } from "../../components/common/Navigator/navigator";
import Search from "../../components/common/Input/Search";
const TransPost = () => {
  const [search, setSearch] = useInput("");
  return (
    <>
      <TopBar title="나의 거래 목록" />
      <LayoutMainBox>
        <LayoutInnerBox>
          <Search
            type="text"
            id="search"
            name="search"
            placeholder="내용 또는 작성자를 입력해주세요"
            onChange={setSearch}
          />
          <MyTrade />
        </LayoutInnerBox>
      </LayoutMainBox>
      <NavBar />
    </>
  );
};
export default TransPost;
