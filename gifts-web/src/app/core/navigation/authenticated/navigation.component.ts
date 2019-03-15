import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "@core-services/authentication.service";
import {Account} from "@model/Account";
import {Router} from "@angular/router";

@Component({
  selector: 'navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['navigation.component.css']
})
export class NavigationComponent implements OnInit {

  account: Account;

  constructor(private router: Router, private authSrv: AuthenticationService) {
  }

  ngOnInit() {
    this.account = this.authSrv.currentAccount;
  }

  get isAdmin() {
    return this.authSrv.isAdmin()
  }

  logout() {
    this.authSrv.logout().subscribe(() => {
      this.account = undefined;
      this.router.navigate(['/login']);
    });
  }


}
