// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import {NgxLoggerLevel} from "ngx-logger";

export const environment = {
  production: false,
  logging: NgxLoggerLevel.DEBUG,
  default_lang: 'en',
  context: '/gifts',
  api_url: '/api',
  refresh_token_url: '/api/refresh',
  whoami_url: '/api/account/whoami',
  login_url: '/login',
  logout_url: '/logout',
  account_url: '/api/account',
  avatar_url: '/avatar',
  avatar_upload_url: '/avatar-upload',
  auth_url: '/auth',
  app_url: '/api/app',
  default_lang_url: '/api/app/default-language',
  gift_url: '/api/gift',
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.