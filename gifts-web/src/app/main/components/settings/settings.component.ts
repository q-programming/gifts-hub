import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {NGXLogger} from "ngx-logger";
import {TranslateService} from "@ngx-translate/core";
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig} from "@angular/material";
import {DOCUMENT} from "@angular/common";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {AuthenticationService} from "@core-services/authentication.service";
import {languages} from "../../../../assets/i18n/languages";
import {Account} from "@model/Account";
import {environment} from "@env/environment.prod";
import {ApiService} from "@core-services/api.service";
import {AlertService} from "@core-services/alert.service";
import {AvatarService} from "@core-services/avatar.service";
import {getBase64Image} from "../../../utils/utils";
import {ConfirmDialogData, ConfirmDialogComponent} from "../../../components/dialogs/confirm/confirm-dialog.component";


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
    this.avatarData.image = this.account.avatar;
  }

  openAvatarDialog() {
    const dialogRef = this.dialog.open(AvatarUploadComponent, {
      panelClass: 'gifts-modal-normal',
      data: {
        account: this.account,
        avatarData: this.avatarData
      }
    });
    dialogRef.afterClosed().subscribe((upload) => {
      if (upload) {
        this.uploadNewAvatar();
      }
    });
  }

  uploadNewAvatar() {
    this.avatarSrv.updateAvatar(getBase64Image(this.avatarData.image), this.account);
    // this.avatarUploadModal.hide();
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
      language: this.account.language
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
}

@Component({
  selector: 'settings-avatar-upload',
  templateUrl: './avatar-upload.component.html',
  styles: []
})
export class AvatarUploadComponent implements OnInit {

  @ViewChild('cropper', undefined)
  cropper: ImageCropperComponent;
  cropperSettings: CropperSettings;
  account: Account;
  avatarData: any;
  uploadInProgress: boolean;


  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
    this.account = data.account;
    this.avatarData = data.avatarData;
    this.cropperSettings = new CropperSettings();
    this.cropperSettings.width = 100;
    this.cropperSettings.height = 100;
    this.cropperSettings.cropperClass = '';
    this.cropperSettings.croppingClass = '';
    this.cropperSettings.croppedWidth = 100;
    this.cropperSettings.croppedHeight = 100;
    this.cropperSettings.canvasWidth = 350;
    this.cropperSettings.canvasHeight = 300;
    this.cropperSettings.noFileInput = true;
    this.cropperSettings.rounded = true;
  }

  ngOnInit(): void {
  }

  fileChangeListener($event) {
    this.uploadInProgress = true;
    let image: any = new Image();
    let file: File = $event.target.files[0];
    const myReader: FileReader = new FileReader();
    const that = this;
    myReader.onloadend = function (loadEvent: any) {
      image.src = loadEvent.target.result;
      that.cropper.setImage(image);
    };
    myReader.readAsDataURL(file);
  }


}
