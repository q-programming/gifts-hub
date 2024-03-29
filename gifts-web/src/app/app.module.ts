import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {RegisterComponent} from "./components/login/register/register.component";
import {GiftsPublicComponent} from './components/gifts-public/gifts-public.component';
import {ResetPasswordComponent} from './components/login/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/login/change-password/change-password.component';
import {CoreModule} from "./core";
import {ConfirmComponent} from "./components/confirm/confirm.component";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {HttpClient} from "@angular/common/http";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {ErrorComponent} from "./components/error/error.component";
import {ConfirmDialogComponent} from "./components/dialogs/confirm/confirm-dialog.component";
import {MainModule} from "./main/main.module";
import {ImageDialogComponent} from "./components/dialogs/image/image-dialog.component";
import {NgProgressModule} from 'ngx-progressbar';


@NgModule({
  declarations: [
    AppComponent,
    ErrorComponent,
    LoginComponent,
    RegisterComponent,
    GiftsPublicComponent,
    ResetPasswordComponent,
    ChangePasswordComponent,
    ConfirmDialogComponent,
    ImageDialogComponent,
    ConfirmComponent

  ],
  imports: [
    CommonModule,
    CoreModule.forRoot(),
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    NgProgressModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (createTranslateLoader),
        deps: [HttpClient]
      }
    }),
    MainModule,
  ],
  schemas: [NO_ERRORS_SCHEMA],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '/translations.json');
}
