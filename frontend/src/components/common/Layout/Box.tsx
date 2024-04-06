import styled from "styled-components";

const LayoutMainBox = styled.main`
  width: 100%;
  display: flex;
  align-items: center;
  flex-direction: column;
  height: 100%;
  padding-top: 3.125rem;
  padding-bottom: 6rem;
`;

const LayoutInnerBox = styled.div`
  display: flex;
  width: 19.875rem;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
  padding: 1.12rem 0;
`;

export { LayoutMainBox, LayoutInnerBox };
