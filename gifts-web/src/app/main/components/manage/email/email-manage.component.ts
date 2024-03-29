import {Component, OnInit} from '@angular/core';
import {AlertService} from "@core-services/alert.service";
import {ApiService} from "@core-services/api.service";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";

@Component({
  selector: 'manage-email',
  templateUrl: './email-manage.component.html',
  styles: []
})
export class EmailManageComponent implements OnInit {

  form: UntypedFormGroup;
  settings: AppSettings = new AppSettings();
  encodings = ["UTF-8"];

  constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: UntypedFormBuilder) {
    this.form = this.formBuilder.group({
      host: [this.settings.email.host, Validators.required],
      port: [this.settings.email.port, Validators.required],
      username: [this.settings.email.username, Validators.required],
      password: [this.settings.email.password, Validators.required],
      encoding: [this.settings.email.encoding, Validators.required],
      from: [this.settings.email.from, [Validators.required, Validators.email]],
    })
  }

  ngOnInit() {
    this.apiSrv.getObject<AppSettings>(`${environment.app_url}/settings`).subscribe(result => {
      this.settings = result;
      this.setSettings();
    })
  }

  sendTest(){
    this.apiSrv.put(`${environment.app_url}/settings/email/test`).subscribe(()=>{
      this.alertSrv.successMessage('Test msg sent');
    },(error)=>{
      console.log(error);
    })
  }
  saveConfiguration() {
    if (this.form.valid) {
      this.getSettings();
      this.apiSrv.put(`${environment.app_url}/settings/email`, this.settings.email).subscribe(() => {
        this.alertSrv.success('app.manage.saved');
      }, error => {
        this.alertSrv.warning('app.manage.email.error', {error: error.error});
      })
    }
  }

  private getSettings() {
    Object.keys(this.form.controls).forEach(name => {
      this.settings.email[name] = this.form.controls[name].value
    });
  }

  private setSettings() {
    Object.keys(this.settings.email).forEach(name => {
      this.form.controls[name].setValue(this.settings.email[name]);
    });
  }
}
