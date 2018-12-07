import {Component, OnInit} from '@angular/core';
import {Account} from "@model/Account";
import {AuthenticationService} from "@services/authentication.service";
import {AlertService} from "@services/alert.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styles: []
})
export class HomeComponent implements OnInit {

  account: Account;

  constructor(private authSrv: AuthenticationService, private alertSrv: AlertService) {
  }

  ngOnInit() {
    this.account = this.authSrv.currentAccount;
  }

}
