import {Component, OnInit} from '@angular/core';
import {environment} from "@env/environment";
import {ApiService} from "@services/api.service";
import {AuthenticationService} from "@services/authentication.service";
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {FormControl, Validators} from "@angular/forms";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['login.component.css']
})
export class LoginComponent implements OnInit {

  login_url = environment.context + environment.login_url;
  usernameCtrl = new FormControl('', Validators.required);
  passwordCtrl = new FormControl('', Validators.required);

  constructor(private authSrv: AuthenticationService, private router: Router, private translate: TranslateService, private apiService: ApiService) {
  }

  ngOnInit() {
    if (this.authSrv.currentAccount) {
      this.router.navigate(['/']);
    }
  }

  login() {
    this.authSrv.login(this.usernameCtrl.value, this.passwordCtrl.value).subscribe((account) => {
      if (account) {
        this.router.navigate(['/']);
      }
    })
  }

}
