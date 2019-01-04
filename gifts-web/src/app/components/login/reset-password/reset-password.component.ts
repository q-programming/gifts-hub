import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";
import {AuthenticationService} from "@services/authentication.service";
import {environment} from "@env/environment";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styles: []
})
export class ResetPasswordComponent implements OnInit {

  emailResetCtrl = new FormControl('', [Validators.required, Validators.email]);

  constructor(private formBuilder: FormBuilder,
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
    })
  }
}
