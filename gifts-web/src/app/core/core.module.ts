import {APP_INITIALIZER, LOCALE_ID, NgModule, NO_ERRORS_SCHEMA, Optional, SkipSelf} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {AppMaterialModules} from "./material.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {LoggerModule, NgxLoggerLevel} from "ngx-logger";
import {environment} from "@env/environment";
import {TranslateModule} from "@ngx-translate/core";
import {LayoutModule} from "@angular/cdk/layout";
import {AuthGuard} from "./guards/auth.guard";
import {AdminGuard} from "./guards/admin.guard";
import {AuthenticationService} from "@core-services/authentication.service";
import {ApiService} from "@core-services/api.service";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {AlertComponent} from "./components/alert/alert.component";
import {AvatarService} from "@core-services/avatar.service";
import {AlertService} from "@core-services/alert.service";
import {HighlightPipe} from "./pipes/highlight.directive";
import {NavigationComponent} from "./navigation/authenticated/navigation.component";
import {NavigationUnauthenticatedComponent} from "./navigation/unauthenticated/navigation-unauthenticated.component";
import {GoTopButtonModule} from "ng2-go-top-button";
import {RouterModule} from "@angular/router";
import {AvatarComponent} from "./components/avatar/avatar.component";
import { InnerLoaderComponent } from './components/inner-loader/inner-loader.component';

@NgModule({
  declarations: [
    AvatarComponent,
    AlertComponent,
    HighlightPipe,
    NavigationComponent,
    NavigationUnauthenticatedComponent,
    InnerLoaderComponent
  ],
  imports: [
    RouterModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AppMaterialModules,
    FlexLayoutModule,
    LoggerModule.forRoot({
      level: environment.logging,
      serverLogLevel: NgxLoggerLevel.ERROR
    }),
    TranslateModule.forChild(),
    LayoutModule,
    GoTopButtonModule
  ],
  providers: [
    AuthGuard,
    AdminGuard,
    FormBuilder,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      'provide': APP_INITIALIZER,
      'useFactory': initUserFactory,
      'deps': [AuthenticationService],
      'multi': true
    },
    {provide: LOCALE_ID, useValue: 'en_EN'}
  ],
  schemas: [NO_ERRORS_SCHEMA],
  exports: [
    AlertComponent,
    AvatarComponent,
    NavigationComponent,
    NavigationUnauthenticatedComponent,
    FormsModule,
    ReactiveFormsModule,
    AppMaterialModules,
    TranslateModule,
    FlexLayoutModule,
    LoggerModule,
    HighlightPipe,
    GoTopButtonModule,
    InnerLoaderComponent
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
  }

  static forRoot() {
    return {
      ngModule: CoreModule,
      providers: [
        AuthenticationService,
        ApiService,
        AlertService,
        AvatarService
      ]
    }
  }
}


export function initUserFactory(authService: AuthenticationService) {
  return () => authService.initUser();
}

