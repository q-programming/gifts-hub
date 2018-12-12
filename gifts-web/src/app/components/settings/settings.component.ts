import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {AuthenticationService} from "@services/authentication.service";
import {Account} from "@model/Account";
import {languages} from "../../../assets/i18n/languages";
import {ApiService} from "@services/api.service";
import {environment} from "@env/environment.prod";
import {AlertService} from "@services/alert.service";
import {NGXLogger} from "ngx-logger";
import {TranslateService} from "@ngx-translate/core";
import {DOCUMENT} from "@angular/common";
import {MAT_DIALOG_DATA, MatDialog} from "@angular/material";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {AvatarService} from "@services/avatar.service";
import {getBase64Image} from "../../utils/utils";


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
              public dialog: MatDialog,) {
  }

  ngOnInit() {
    this.account = this.authSrv.currentAccount;
    this.avatarData.image = this.account.avatar;
  }

  openAvatarDialog() {
    const dialogRef = this.dialog.open(AvatarUploadComponent, {
      panelClass: 'shopper-modal-normal', //TODO class needed
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

  deleteAccount() {

  }

  getAccountSettings() {
    return {
      newsletter: this.account.newsletter,
      publicList: this.account.publicList,
      language: this.account.language
    }
  }

  get publicUrl() {
    return `${this.document.location.href.split("#")[0]}#/public/${this.account.username}`;
  }

  copyLink(element) {
    element.select();
    document.execCommand('copy');
    element.setSelectionRange(0, 0);
    this.alertSrv.success('user.settings.public.copy.success');
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


  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
    this.account = data.account;
    this.avatarData = data.avatarData;
    this.cropperSettings = new CropperSettings();
    this.cropperSettings.width = 100;
    this.cropperSettings.height = 100;
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
