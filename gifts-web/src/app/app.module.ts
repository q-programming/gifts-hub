import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule, NO_ERRORS_SCHEMA} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoggerModule, NgxLoggerLevel} from "ngx-logger";
import {environment} from "../environments/environment";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {AuthenticationService} from "./services/authentication.service";
import {ApiService} from "./services/api.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AuthGuard} from "./guards/auth.guard";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {NgProgressModule} from "ngx-progressbar";
import {FlexLayoutModule} from "@angular/flex-layout";
import {AppMaterialModules} from "./material.module";
import {HomeComponent} from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import {AvatarService} from "@services/avatar.service";
import {AlertService} from "@services/alert.service";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    AppMaterialModules,
    NgProgressModule,
    FlexLayoutModule,
    LoggerModule.forRoot({
      level: environment.logging,
      serverLogLevel: NgxLoggerLevel.ERROR
    }),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (createTranslateLoader),
        deps: [HttpClient]
      }
    }),
  ],
  schemas: [NO_ERRORS_SCHEMA],
  providers: [
    AuthGuard,
    AuthenticationService,
    ApiService,
    AvatarService,
    AlertService,
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
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

export function initUserFactory(authService: AuthenticationService) {
  return () => authService.initUser();
}
