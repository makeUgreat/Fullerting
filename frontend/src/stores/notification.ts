import { atom } from 'jotai';

export const notificationAtom = atom({
  show: false,
  name: "",
  content: "",
  redirectURL: "",
});

