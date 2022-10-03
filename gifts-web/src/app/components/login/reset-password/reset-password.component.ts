import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormControl, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {ApiService} from "@core-services/api.service";
import {AlertService} from "@core-services/alert.service";
import {AuthenticationService} from "@core-services/authentication.service";
import {environment} from "@env/environment";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styles: []
})
export class ResetPasswordComponent implements OnInit {

  emailResetCtrl = new UntypedFormControl('', [Validators.required, Validators.email]);

  constructor(private formBuilder: UntypedFormBuilder,
              private router: Router,
              private apiSrv: ApiService,
              private alertSrv: AlertService,
              private authSrv: AuthenticationService) {

  }

  ngOnInit() {
    if (this.authSrv.currentAccount) {
      this.router.navigate(['/']);
    }
    this.authSrv.setLanguage();
  }

  reset() {
    this.apiSrv.post(`${environment.account_url}/password-reset`, this.emailResetCtrl.value).subscribe(() => {
      this.alertSrv.success('user.password.reset.sent');
      this.router.navigate(['/']);
    }, error1 => {
      this.alertSrv.error('user.password.reset.error.mailSrv');
    })
  }
}
