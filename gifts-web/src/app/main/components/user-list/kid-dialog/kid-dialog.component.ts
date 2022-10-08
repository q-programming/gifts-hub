import {Component, Inject, OnInit} from '@angular/core';
import {DOCUMENT} from "@angular/common";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {Account} from "@model/Account";
import {AlertService} from "@core-services/alert.service";
import {ApiService} from "@core-services/api.service";
import {environment} from "@env/environment";
import {getBase64Image} from "../../../../utils/utils";
import {ImageCroppedEvent} from "ngx-image-cropper";
import {debounceTime} from 'rxjs/operators';

@Component({
  selector: 'app-kid',
  templateUrl: './kid-dialog.component.html',
  styles: []
})
export class KidDialogComponent implements OnInit {
  kid: Account;
  avatarData: any = undefined;
  imageChangedEvent: any = '';
  form: UntypedFormGroup;
  update: boolean;
  uploadInProgress: boolean;

  constructor(private dialogRef: MatDialogRef<KidDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              @Inject(DOCUMENT) private document: Document,
              private apiSrv: ApiService,
              private alertSrv: AlertService) {
    this.kid = data.account;
    if (data.account.id) {
      this.avatarData = this.kid.avatar;
      this.update = true;
    }
    this.form = new UntypedFormGroup({
        name: new FormControl<string>(this.kid.name, [Validators.required]),
        surname: new FormControl<string>(this.kid.surname, [Validators.required]),
        username: new FormControl<string>(this.kid.username, [Validators.required]),
        publicList: new FormControl<boolean>(this.kid.publicList),
        birthday: new FormControl<any>(this.kid.birthday)
      }
    );
  }

  ngOnInit() {
    this.form.controls.username.valueChanges
      .pipe(debounceTime(300))
      .subscribe(value => {
        this.apiSrv.post(`${environment.account_url}/validate-username`, value).subscribe(() => {
        }, () => {
          this.form.controls.username.setErrors({username: true})
        })
      })


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
      this.kid.birthday = this.form.controls.birthday.value;
      this.kid.avatar = getBase64Image(this.avatarData);
      this.dialogRef.close(this.kid);
    }
  }

  fileChangeEvent(event: any): void {
    this.uploadInProgress = true;
    this.imageChangedEvent = event;
  }

  imageCropped(event: ImageCroppedEvent) {
    this.avatarData = event.base64;
  }

  removeDOB() {
    this.form.controls.birthday.setValue(undefined);
  }
}
