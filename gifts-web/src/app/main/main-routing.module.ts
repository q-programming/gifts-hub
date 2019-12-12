import {RouterModule, Routes} from "@angular/router";
import {GiftsComponent} from "./components/gifts/gifts.component";
import {UserListComponent} from "./components/user-list/user-list.component";
import {ManageComponent} from "./components/manage/manage.component";
import {AdminGuard} from "../core/guards/admin.guard";
import {SettingsComponent} from "./components/settings/settings.component";
import {NgModule} from "@angular/core";
import {MainComponent} from "./main.component";
import {HelpComponent} from "./components/help/help.component";
import {GiftClaimedComponent} from "./components/gifts/gift-claimed.component";

const routes: Routes = [
  {
    path: '', component: MainComponent, children: [
      {path: '', component: GiftsComponent},
      {path: 'users', component: UserListComponent},
      {path: 'claimed', component: GiftClaimedComponent},
      {path: 'list', component: GiftsComponent},
      {path: 'list/:user', component: GiftsComponent},
      {path: 'manage', component: ManageComponent, canActivate: [AdminGuard]},
      {path: 'settings', component: SettingsComponent},
      {path: 'help', component: HelpComponent},
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule {
}
