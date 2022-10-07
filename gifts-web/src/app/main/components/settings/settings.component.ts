import {Component, Inject, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {NGXLogger} from "ngx-logger";
import {TranslateService} from "@ngx-translate/core";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {DOCUMENT} from "@angular/common";
import {AuthenticationService} from "@core-services/authentication.service";
import {languages} from "../../../../assets/i18n/languages";
import {Account} from "@model/Account";
import {environment} from "@env/environment.prod";
import {ApiService} from "@core-services/api.service";
import {AlertService} from "@core-services/alert.service";
import {AvatarService} from "@core-services/avatar.service";
import {getBase64Image} from "../../../utils/utils";
import {ConfirmDialogComponent, ConfirmDialogData} from "../../../components/dialogs/confirm/confirm-dialog.component";
import {ImageCroppedEvent} from "ngx-image-cropper";


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['settings.component.css']
})
export class SettingsComponent implements OnInit {
  account: Account;
  languages: any = languages;
  avatarData: any = {};
  emailList: string;
  croppedAvatar: any = '';
  uploadInProgress: boolean;
  imageChangedEvent: any = '';


  constructor(private authSrv: AuthenticationService,
              private apiSrv: ApiService,
              private alertSrv: AlertService,
              private avatarSrv: AvatarService,
              private translate: TranslateService,
              private logger: NGXLogger,
              @Inject(DOCUMENT) private document: Document,
              public dialog: MatDialog,
              private router: Router) {
  }

  ngOnInit() {
    this.account = this.authSrv.currentAccount;
    this.croppedAvatar = this.account.avatar;
  }

  uploadNewAvatar() {
    this.uploadInProgress = false;
    this.avatarSrv.updateAvatar(getBase64Image(this.croppedAvatar), this.account);
    this.alertSrv.success('user.settings.avatar.success');
  }

  changeLanguage() {
    this.apiSrv.post(`${environment.account_url}/settings`, this.getAccountSettings()).subscribe(() => {
      this.translate.use(this.account.language).subscribe(() => {
        this.alertSrv.success('user.settings.updated.success')
      });
    }, error => {
      this.logger.error(error);
      this.alertSrv.error('user.settings.updated.error');
    });
  }

  updateSettings() {
    this.apiSrv.post(`${environment.account_url}/settings`, this.getAccountSettings()).subscribe(() => {
      this.alertSrv.success('user.settings.updated.success')
    }, error => {
      this.logger.error(error);
      this.alertSrv.error('user.settings.updated.error');
    });
  }

  shareList() {
    this.apiSrv.post(`${environment.account_url}/share`, this.emailList).subscribe(result => {
      this.emailList = undefined;
      this.alertSrv.success('gift.share.success', {emails: result.emails});
    })
  }

  getAccountSettings() {
    return {
      newsletter: this.account.notifications,
      publicList: this.account.publicList,
      language: this.account.language,
      birthday: this.account.birthday,
      birthdayReminder: this.account.birthdayReminder
    }
  }

  get publicUrl() {
    return `${this.document.location.href.split("#")[0]}#/public/${this.account.username}`;
  }

  copyLink() {
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.publicUrl;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.alertSrv.success('user.settings.public.copy.success');
  }

  deleteAccount() {
    const data: ConfirmDialogData = {
      title_key: 'user.delete.text',
      message_key: 'user.delete.confirm',
      action_key: 'app.general.delete',
      action_class: 'warn'
    };
    const dialogConfig: MatDialogConfig = {
      disableClose: true,
      panelClass: 'gifts-dialog-modal',
      data: data
    };
    let dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.apiSrv.delete(`${environment.account_url}/delete/${this.account.id}`).subscribe(() => {
          this.alertSrv.success('user.delete.success');
          this.authSrv.currentAccount = null;
          this.router.navigate(['/login'])
        })
      }
    })
  }

  fileChangeEvent(event: any): void {
    this.uploadInProgress = true;
    this.imageChangedEvent = event;
  }

  imageCropped(event: ImageCroppedEvent) {
    this.croppedAvatar = event.base64;
  }

  removeDOB() {
    this.account.birthday = undefined;
    this.updateSettings();
  }
}
