import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {Account} from "@model/Account";
import {Router} from "@angular/router";

@Component({
  selector: 'navigation-unauthenticated',
  templateUrl: './navigation-unauthenticated.component.html'
})
export class NavigationUnauthenticatedComponent implements OnInit {

  currentAccount: Account;
  isLoginPage: boolean;

  constructor(private authSrv: AuthenticationService, private router: Router) {
  }

  ngOnInit() {
    this.isLoginPage = this.router.url === '/login';
    this.currentAccount = this.authSrv.currentAccount;
  }
}
