// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_CUSTOM_ENV_VARIABLE: string;
  readonly VITE_SERVER_URL: string;
  readonly __WEBSOCKET_URL__: string;

}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

 