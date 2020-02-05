import {Component, Inject, OnInit} from '@angular/core';
import {AuthenticationService, FACEBOOK_AUTH_URL, GOOGLE_AUTH_URL} from "@core-services/authentication.service";
import {Router} from "@angular/router";
import {FormControl, Validators} from "@angular/forms";
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['login.component.css']
})
export class LoginComponent implements OnInit {

  usernameCtrl = new FormControl('', Validators.required);
  passwordCtrl = new FormControl('', Validators.required);
  FacebookLoginURL;
  GoogleLoginURL;
  redirect_url;

  constructor(private authSrv: AuthenticationService,
              private router: Router,
              @Inject(DOCUMENT) private document: Document) {
  }

  ngOnInit() {
    if (this.authSrv.currentAccount) {
      this.router.navigate(['/']);
    }
    sessionStorage.clear();
    this.authSrv.setLanguage();
    this.redirect_url = `${this.document.location.href.split("#")[0]}#/`;
    this.FacebookLoginURL = FACEBOOK_AUTH_URL + this.redirect_url;
    this.GoogleLoginURL = GOOGLE_AUTH_URL + this.redirect_url;
  }

  login() {
    this.authSrv.login(this.usernameCtrl.value, this.passwordCtrl.value).subscribe(() => {
      this.router.navigate(['/']);
    })
  }
}
