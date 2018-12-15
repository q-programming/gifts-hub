import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, LOCALE_ID, NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoggerModule, NgxLoggerLevel} from "ngx-logger";
import {environment} from "@env/environment";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {AuthenticationService} from "@services/authentication.service";
import {ApiService} from "@services/api.service";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AuthGuard} from "./guards/auth.guard";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {FlexLayoutModule} from "@angular/flex-layout";
import {AppMaterialModules} from "./material.module";
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {AvatarService} from "@services/avatar.service";
import {AlertService} from "@services/alert.service";
import {AlertComponent} from "./components/alert/alert.component";
import {NgProgressModule} from "@ngx-progressbar/core";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NavigationComponent} from './components/navigation/navigation.component';
import {LayoutModule} from '@angular/cdk/layout';
import {MatButtonModule, MatIconModule, MatListModule, MatSidenavModule, MatToolbarModule} from '@angular/material';
import {UserListComponent} from './components/user-list/user-list.component';
import {GiftsComponent} from './components/gifts/gifts.component';
import {ErrorComponent} from "./components/error/error.component";
import {RegisterComponent} from "./components/register/register.component";
import {PasswordStrengthBarModule} from "ng2-password-strength-bar";
import {AvatarComponent} from './components/avatar/avatar.component';
import {GiftComponent} from './components/gifts/gift/gift.component';
import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {HighlightPipe} from "./pipes/highlight.directive";
import {AccountListComponent} from './components/gifts/account-list/account-list.component';
import {NgProgressRouterModule} from "@ngx-progressbar/router";
import {UserComponent} from './components/user-list/user/user.component';
import {GiftsPublicComponent} from './components/gifts/gifts-public.component';
import {AvatarUploadComponent, SettingsComponent} from './components/settings/settings.component';
import {ImageCropperModule} from "ngx-img-cropper";
import {KidDialogComponent} from './components/user-list/kid-dialog/kid-dialog.component';
import {GiftDialogComponent} from './components/gifts/gift-dialog/gift-dialog.component';


@NgModule({
  declarations: [
    AppComponent,
    AlertComponent,
    ErrorComponent,
    HomeComponent,
    LoginComponent,
    NavigationComponent,
    UserListComponent,
    RegisterComponent,
    GiftsComponent,
    AvatarComponent,
    GiftComponent,
    HighlightPipe,
    AccountListComponent,
    UserComponent,
    GiftsPublicComponent,
    SettingsComponent,
    AvatarUploadComponent,
    KidDialogComponent,
    GiftDialogComponent,
  ],
  entryComponents: [
    AvatarUploadComponent,
    KidDialogComponent,
    GiftDialogComponent
  ],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    AppMaterialModules,
    NgProgressModule.forRoot(),
    NgProgressRouterModule.forRoot(),
    ImageCropperModule,
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
    LayoutModule,
    NgxMatSelectSearchModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    PasswordStrengthBarModule
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
    },
    {provide: LOCALE_ID, useValue: 'en_EN'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '/translations.json');
}

export function initUserFactory(authService: AuthenticationService) {
  return () => authService.initUser();
}
