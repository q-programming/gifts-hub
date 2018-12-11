import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from "./guards/auth.guard";
import {HomeComponent} from "./components/home/home.component";
import {LoginComponent} from "./components/login/login.component";
import {UserListComponent} from "./components/user-list/user-list.component";
import {GiftsComponent} from "./components/gifts/gifts.component";
import {ErrorComponent} from "./components/error/error.component";
import {RegisterComponent} from "./components/register/register.component";
import {GiftsPublicComponent} from "./components/gifts/gifts-public.component";

const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [AuthGuard]},
  {path: 'login', component: LoginComponent},
  {path: 'users', component: UserListComponent, canActivate: [AuthGuard]},
  {path: 'list', component: GiftsComponent, canActivate: [AuthGuard]},
  {path: 'list/:user', component: GiftsComponent, canActivate: [AuthGuard]},
  {path: 'public/:user', component: GiftsPublicComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'error', component: ErrorComponent},
  // otherwise redirect to home
  {path: '**', redirectTo: '/error?type=404'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    anchorScrolling: 'enabled',
    useHash: true,
    onSameUrlNavigation: 'reload'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}


