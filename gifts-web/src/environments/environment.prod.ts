import {NgxLoggerLevel} from "ngx-logger";
import {version} from "../../package.json"

export const environment = {
  production: true,
  logging: NgxLoggerLevel.OFF,
  default_lang: 'en',
  context: '/gifts',
  api_url: '/api',
  refresh_token_url: '/api/refresh',
  whoami_url: '/api/account/whoami',
  oauth_login_url: '/oauth2/authorize/',
  login_url: '/login',
  logout_url: '/logout',
  account_url: '/api/account',
  avatar_url: '/avatar',
  avatar_upload_url: '/avatar-upload',
  auth_url: '/auth',
  default_lang_url: '/api/app/default-language',
  app_url: '/api/app',
  gift_url: '/api/gift',
  version: version,
  routing_log: false
};
