import {Component, Inject, OnInit} from '@angular/core';
import {AlertService} from "@services/alert.service";
import {ApiService} from "@services/api.service";
import {languages} from "../../../../assets/i18n/languages";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DOCUMENT} from "@angular/common";
import {SortBy} from "@model/Settings";

@Component({
    selector: 'manage-app',
    templateUrl: './app-manage.component.html',
    styles: []
})
export class AppManageComponent implements OnInit {
    SortBy = SortBy;
    form: FormGroup;
    languages: any = languages;
    settings: AppSettings = new AppSettings();

    constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: FormBuilder, @Inject(DOCUMENT) private document: Document) {
        this.form = this.formBuilder.group({
            language: [this.settings.language, Validators.required],
            appUrl: [this.settings.appUrl, Validators.required],
            giftAge: [this.settings.giftAge, Validators.required],
            sort: [this.settings.sort, Validators.required]
        })
    }

    ngOnInit() {
        this.apiSrv.getObject<AppSettings>(`${environment.app_url}/settings`).subscribe(result => {
            this.settings = result;
            this.setSettings();
        })
    }

    saveConfiguration() {
        if (this.form.valid) {
            this.getSettings();
            this.apiSrv.put(`${environment.app_url}/settings`, this.settings).subscribe(() => {
                this.alertSrv.success('app.manage.saved');
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
        this.settings.sort= this.form.controls.sort.value;
    }

    private setSettings() {
        this.form.controls.language.setValue(this.settings.language);
        this.form.controls.appUrl.setValue(this.settings.appUrl);
        this.form.controls.giftAge.setValue(this.settings.giftAge);
        this.form.controls.sort.setValue(this.settings.sort);

    }


}
