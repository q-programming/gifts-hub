import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from "./core/guards/auth.guard";
import {LoginComponent} from "./components/login/login.component";
import {ErrorComponent} from "./components/error/error.component";
import {RegisterComponent} from "./components/login/register/register.component";
import {GiftsPublicComponent} from "./components/gifts-public/gifts-public.component";
import {ResetPasswordComponent} from "./components/login/reset-password/reset-password.component";
import {ChangePasswordComponent} from "./components/login/change-password/change-password.component";
import {ConfirmComponent} from "./components/confirm/confirm.component";

const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./main/main.module').then(m => m.MainModule),
    canActivate:[AuthGuard],
  },
  {path: 'login', component: LoginComponent},
  {path: 'public/:user', component: GiftsPublicComponent},
  {path: 'confirm/:token', component: ConfirmComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'reset', component: ResetPasswordComponent},
  {path: 'password-change/:token', component: ChangePasswordComponent},
  {path: 'error', component: ErrorComponent},
  // otherwise redirect to home
  {path: '**', redirectTo: '/error?type=404'}
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      anchorScrolling: 'enabled',
      useHash: true,
      scrollPositionRestoration: 'enabled',
      onSameUrlNavigation: 'reload'
    })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}


