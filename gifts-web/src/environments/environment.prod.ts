import {NgxLoggerLevel} from "ngx-logger";

export const environment = {
  production: true,
  logging: NgxLoggerLevel.OFF,
  default_lang: 'en',
  context: '/shopper',
  api_url: '/api',
  refresh_token_url: '/api/refresh',
  whoami_url: '/api/account/whoami',
  login_url: '/login',
  logout_url: '/logout',
  account_url: '/api/account',
  avatar_url: '/avatar',
  avatar_upload_url: '/avatar-upload',
  auth_url: '/auth',
};
