import { Component, OnInit } from '@angular/core';
import {ApiService} from "@core-services/api.service";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";

@Component({
  selector: 'app-manage',
  templateUrl: './manage.component.html',
  styles: []
})
export class ManageComponent implements OnInit {

  settings:AppSettings;

  constructor(private apiSrv:ApiService) { }

  ngOnInit() {
    this.getSettings();
  }

  getSettings() {
    this.apiSrv.getObject<AppSettings>(`${environment.app_url}/settings`).subscribe(result => {
      this.settings = result;
    })
  }
}
