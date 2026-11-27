import {Component, Inject, OnInit} from '@angular/core';
import {DOCUMENT} from "@angular/common";
import {ActivatedRoute, Router} from "@angular/router";
import {
  AbstractControl,
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators
} from "@angular/forms";
import {environment} from "@env/environment";
import {ApiService} from "@core-services/api.service";
import {AlertService} from "@core-services/alert.service";
import {AuthenticationService, GOOGLE_KID_CONVERT_URL} from "@core-services/authentication.service";

/**
 * Landing page for kid account conversion invitation. Kid can finish conversion using Google account or by setting username and password
 */
@Component({
  selector: 'app-kid-convert',
  templateUrl: './kid-convert.component.html',
  styleUrls: ['../login/login.component.css']
})
export class KidConvertComponent implements OnInit {
  token: string;
  invite: { name: string, username: string, email: string };
  invalid = false;
  googleUrl: string;
  form: UntypedFormGroup;

  constructor(private formBuilder: UntypedFormBuilder,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private apiSrv: ApiService,
              private alertSrv: AlertService,
              private authSrv: AuthenticationService,
              @Inject(DOCUMENT) private document: Document) {
  }

  ngOnInit() {
    this.authSrv.setLanguage();
    const query = new URLSearchParams(this.document.location.search);
    // result is passed in the hash route, query string stays supported
    const param = (name: string): string => this.activatedRoute.snapshot?.queryParams?.[name] || query.get(name);
    if (param('converted')) {
      this.finish();
      return;
    }
    this.showGoogleError(param('error'));
    this.activatedRoute.params.subscribe(params => {
      this.token = params['token'];
      this.googleUrl = this.buildGoogleUrl();
      this.apiSrv.get(`${environment.account_url}/kid-convert/${this.token}`).subscribe(invite => {
        this.invite = invite;
        this.form = this.formBuilder.group({
          username: [invite.username, [Validators.required, Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
          password: ['', [Validators.required, Validators.minLength(8)]],
          confirmPassword: ['', [Validators.required]]
        }, {validators: this.matchingPasswords});
      }, () => this.invalid = true);
    });
  }

  // server verifies Google account and returns to this page, converted or with error
  private buildGoogleUrl(): string {
    const back = `${this.document.location.href.split('#')[0]}#/convert/${this.token}`;
    return `${GOOGLE_KID_CONVERT_URL}${this.token}&redirect_uri=${encodeURIComponent(back)}`;
  }

  private showGoogleError(error: string) {
    if (error) {
      this.alertSrv.error(error.includes('kid_convert_email') ? 'user.kidConvert.error.email' : 'user.kidConvert.error.google');
    }
  }

  // end any existing session (e.g. the admin who sent the invite), so the new credentials have to be used
  private finish() {
    this.alertSrv.success('user.kidConvert.success');
    const toLogin = () => {
      this.authSrv.currentAccount = null;
      this.router.navigate(['/login']);
    };
    this.authSrv.logout().subscribe(toLogin, toLogin);
  }

  matchingPasswords(c: AbstractControl): { [key: string]: any } {
    return c.get('password').value !== c.get('confirmPassword').value ? {notSame: true} : null;
  }

  convert() {
    if (this.form.invalid) {
      return;
    }
    this.apiSrv.post(`${environment.account_url}/kid-convert/${this.token}/local`, {
      username: this.form.controls.username.value,
      password: this.form.controls.password.value,
      confirmpassword: this.form.controls.confirmPassword.value
    }).subscribe(() => this.finish(), error => {
      switch (error.error) {
        case 'bad_username':
          this.form.controls.username.setErrors({badusername: true});
          break;
        case 'username':
          this.form.controls.username.setErrors({username: true});
          break;
        case 'passwords':
          this.form.setErrors({notSame: true});
          break;
        case 'weak':
          this.form.controls.password.setErrors({minlength: true});
          break;
        case 'email':
        case 'invalid':
          this.invalid = true;
          break;
      }
    });
  }
}
