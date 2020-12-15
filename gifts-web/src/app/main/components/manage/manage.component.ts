import {Component, OnInit} from '@angular/core';
import {ApiService} from "@core-services/api.service";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";
import {NgProgress, NgProgressRef} from 'ngx-progressbar';

@Component({
  selector: 'app-manage',
  templateUrl: './manage.component.html',
  styles: []
})
export class ManageComponent implements OnInit {

  settings: AppSettings;
  private progress: NgProgressRef;

  constructor(private apiSrv: ApiService, public ngProgress: NgProgress) {
    this.progress = ngProgress.ref()
  }

  ngOnInit() {
    this.getSettings();
  }

  getSettings() {
    this.progress.start()
    this.apiSrv.getObject<AppSettings>(`${environment.app_url}/settings`).subscribe(result => {
      this.settings = result;
      this.progress.complete();
    })
  }
}
