import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CoreModule} from "../core";
import {UserListComponent} from "./components/user-list/user-list.component";
import {GiftsComponent} from "./components/gifts/gifts.component";
import {GiftComponent} from "./components/gifts/gift/gift.component";
import {AccountListComponent} from "./components/gifts/account-list/account-list.component";
import {UserComponent} from "./components/user-list/user/user.component";
import {AvatarUploadComponent, SettingsComponent} from "./components/settings/settings.component";
import {KidDialogComponent} from "./components/user-list/kid-dialog/kid-dialog.component";
import {GiftDialogComponent} from "./components/gifts/gift-dialog/gift-dialog.component";
import {ManageComponent} from "./components/manage/manage.component";
import {AppManageComponent} from "./components/manage/app/app-manage.component";
import {EmailManageComponent} from "./components/manage/email/email-manage.component";
import {EngineManageComponent} from "./components/manage/engines/engines-manage.component";
import {AdminsComponent} from "./components/manage/admins/admins.component";
import {CategoryListComponent} from "./components/gifts/category-list/category-list.component";
import {ConfirmDialogComponent} from "./components/dialogs/confirm/confirm-dialog.component";
import {GroupDialogComponent} from "./components/user-list/group-dialog/group-dialog.component";
import {PermissionComponent} from "./components/user-list/permission/permission.component";
import {ImageCropperModule} from "ngx-img-cropper";
import {NgxMatSelectSearchModule} from "ngx-mat-select-search";
import {MainRoutingModule} from "./main-routing.module";
import {MainComponent} from './main.component';

@NgModule({
  declarations: [
    UserListComponent,
    GiftsComponent,
    GiftComponent,
    AccountListComponent,
    UserComponent,
    SettingsComponent,
    AvatarUploadComponent,
    KidDialogComponent,
    GiftDialogComponent,
    ManageComponent,
    AppManageComponent,
    EmailManageComponent,
    EngineManageComponent,
    AdminsComponent,
    CategoryListComponent,
    ConfirmDialogComponent,
    GroupDialogComponent,
    PermissionComponent,
    MainComponent,
  ],
  imports: [
    CommonModule,
    CoreModule,
    ImageCropperModule,
    NgxMatSelectSearchModule,
    MainRoutingModule
  ],
  entryComponents: [
    AvatarUploadComponent,
    KidDialogComponent,
    GiftDialogComponent,
    ConfirmDialogComponent,
    GroupDialogComponent
  ],
  schemas: [NO_ERRORS_SCHEMA],
  providers: []
})
export class MainModule {
}
