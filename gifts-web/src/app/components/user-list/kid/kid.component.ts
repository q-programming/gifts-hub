import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {Account} from "@model/Account";
import {DOCUMENT} from "@angular/common";
import {AlertService} from "@services/alert.service";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-kid',
  templateUrl: './kid.component.html',
  styles: []
})
export class KidComponent implements OnInit {

  @ViewChild('cropper', undefined)
  cropper: ImageCropperComponent;
  cropperSettings: CropperSettings;
  kid = new Account();
  avatarData: any = {};
  form: FormGroup;


  constructor(private dialogRef: MatDialogRef<KidComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              @Inject(DOCUMENT) private document: Document,
              private alertSrv: AlertService) {
    if (data.account) {
      this.kid = data.account;
      this.avatarData.image = this.kid.avatar;
    }
    this.form = new FormGroup({
        name: new FormControl(this.kid.name),
        surname: new FormControl(this.kid.surname),
        username: new FormControl(this.kid.username),
        publicList: new FormControl(this.kid.publicList)
      }
    );
    //CROPER
    this.cropperSettings = new CropperSettings();
    this.cropperSettings.width = 100;
    this.cropperSettings.height = 100;
    this.cropperSettings.croppedWidth = 100;
    this.cropperSettings.croppedHeight = 100;
    this.cropperSettings.canvasWidth = 200;
    this.cropperSettings.canvasHeight = 200;
    this.cropperSettings.noFileInput = true;
    this.cropperSettings.rounded = true;
  }

  ngOnInit() {

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

  get publicUrl() {
    return `${this.document.location.href.split("#")[0]}#/public/${this.kid.username}`;
  }

  copyLink(element) {
    element.select();
    document.execCommand('copy');
    element.setSelectionRange(0, 0);
    this.alertSrv.success('user.settings.public.copy.success');
  }

}
