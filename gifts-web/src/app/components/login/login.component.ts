import {Component, OnInit} from '@angular/core';
import {environment} from "@env/environment";
import {AuthenticationService, FACEBOOK_AUTH_URL, GOOGLE_AUTH_URL} from "@core-services/authentication.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormControl, Validators} from "@angular/forms";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['login.component.css']
})
export class LoginComponent implements OnInit {

  GOOGLE_AUTH_URL = GOOGLE_AUTH_URL;
  FACEBOOK_AUTH_URL = FACEBOOK_AUTH_URL;
  usernameCtrl = new FormControl('', Validators.required);
  passwordCtrl = new FormControl('', Validators.required);

  constructor(private authSrv: AuthenticationService, private router: Router, private activatedRoute: ActivatedRoute,) {
  }

  ngOnInit() {
    if (this.authSrv.currentAccount) {
      this.router.navigate(['/']);
    }
    sessionStorage.clear();
    this.authSrv.setLanguage();
  }

  login() {
    this.authSrv.login(this.usernameCtrl.value, this.passwordCtrl.value).subscribe(() => {
      this.router.navigate(['/']);
    })
  }

}
