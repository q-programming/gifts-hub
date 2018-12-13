import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {Account} from "@model/Account";
import {DOCUMENT} from "@angular/common";
import {AlertService} from "@services/alert.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {ApiService} from "@services/api.service";
import {environment} from "@env/environment";
import {getBase64Image} from "../../../utils/utils";

@Component({
  selector: 'app-kid',
  templateUrl: './kid.component.html',
  styles: []
})
export class KidComponent implements OnInit {

  @ViewChild('cropper', undefined)
  cropper: ImageCropperComponent;
  cropperSettings: CropperSettings;
  kid: Account;
  avatarData: any = {};
  form: FormGroup;
  update: boolean;
  uploadInProgress: boolean;


  constructor(private dialogRef: MatDialogRef<KidComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              @Inject(DOCUMENT) private document: Document,
              private apiSrv: ApiService,
              private alertSrv: AlertService) {
    this.kid = data.account;
    if (data.account.id) {
      this.avatarData.image = this.kid.avatar;
      this.update = true;
    }
    this.form = new FormGroup({
        name: new FormControl(this.kid.name, [Validators.required]),
        surname: new FormControl(this.kid.surname, [Validators.required]),
        username: new FormControl(this.kid.username, [Validators.required]),
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
    this.form.controls.username.valueChanges
      .debounceTime(300).subscribe(value => {
      this.apiSrv.post(`${environment.account_url}/validate-username`, value).subscribe(() => {
      }, error => {
        this.form.controls.username.setErrors({username: true})
      })
    })


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

  get publicUrl() {
    return `${this.document.location.href.split("#")[0]}#/public/${this.kid.username}`;
  }

  copyLink(element) {
    element.select();
    document.execCommand('copy');
    element.setSelectionRange(0, 0);
    this.alertSrv.success('user.settings.public.copy.success');
  }

  commitKid(valid: boolean) {
    if (valid) {
      this.kid.name = this.form.controls.name.value;
      this.kid.surname = this.form.controls.surname.value;
      this.kid.username = this.form.controls.username.value;
      this.kid.publicList = this.form.controls.publicList.value;
      this.kid.avatar = getBase64Image(this.avatarData.image);
      this.dialogRef.close(this.kid);
    }
  }

}
