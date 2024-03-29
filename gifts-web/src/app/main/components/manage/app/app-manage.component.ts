import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {AlertService} from "@core-services/alert.service";
import {ApiService} from "@core-services/api.service";
import {languages} from "../../../../../assets/i18n/languages";
import {AppSettings, SortBy} from "@model/AppSettings";
import {environment} from "@env/environment";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'manage-app',
  templateUrl: './app-manage.component.html',
  styles: []
})
export class AppManageComponent implements OnInit {
  SortBy = SortBy;
  form: UntypedFormGroup;
  languages: any = languages;
  @Input()
  settings: AppSettings;
  @Output()
  commit: EventEmitter<boolean> = new EventEmitter();

  constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: UntypedFormBuilder, @Inject(DOCUMENT) private document: Document) {

  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      language: [this.settings.language, Validators.required],
      appUrl: [this.settings.appUrl, Validators.required],
      giftAge: [this.settings.giftAge, Validators.required],
      birthdayReminder: [this.settings.birthdayReminder, Validators.required],
      sort: [this.settings.sort, Validators.required]
    });
    this.setSettings();
  }

  saveConfiguration() {
    if (this.form.valid) {
      this.getSettings();
      this.apiSrv.put(`${environment.app_url}/settings`, this.settings).subscribe(() => {
        this.alertSrv.success('app.manage.saved');
        this.commit.emit(true);
      }, error => {
        this.alertSrv.error('app.manage.error');
      })
    }
  }


  getAppURL() {
    this.form.controls.appUrl.setValue(this.document.location.href.split("#")[0]);
  }

  testMessages() {
    this.alertSrv.successMessage("Success");
    this.alertSrv.errorMessage("Error");
    this.alertSrv.warningMessage("Warning");
    this.alertSrv.infoMessage("Info");
  }

  private getSettings() {
    this.settings.language = this.form.controls.language.value;
    this.settings.appUrl = this.form.controls.appUrl.value;
    this.settings.giftAge = this.form.controls.giftAge.value;
    this.settings.birthdayReminder = this.form.controls.birthdayReminder.value;
    this.settings.sort = this.form.controls.sort.value;
  }

  private setSettings() {
    this.form.controls.language.setValue(this.settings.language);
    this.form.controls.appUrl.setValue(this.settings.appUrl);
    this.form.controls.giftAge.setValue(this.settings.giftAge);
    this.form.controls.birthdayReminder.setValue(this.settings.birthdayReminder);
    this.form.controls.sort.setValue(this.settings.sort);

  }
}
