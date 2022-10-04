import {Component, OnInit} from '@angular/core';
import {registerLocaleData} from "@angular/common";
import localeEnGb from '@angular/common/locales/en-GB';
import localePL from '@angular/common/locales/pl';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styles: []
})
export class AppComponent implements OnInit {


  constructor() {
  }

  ngOnInit(): void {
    registerLocaleData(localePL, 'pl');
    registerLocaleData(localeEnGb, 'en');
  }
}
