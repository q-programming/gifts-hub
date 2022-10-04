import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {AuthenticationService} from "@core-services/authentication.service";
import {Account} from "@model/Account";
import {AppService} from "@core-services/app.service";

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styles: []
})
export class ErrorComponent implements OnInit {

  type: string;
  message: string;
  currentAccount: Account;

  constructor(private activatedRoute: ActivatedRoute,
              private translate: TranslateService,
              private appSrv: AppService,
              private authSrv: AuthenticationService) {
    this.currentAccount = this.authSrv.currentAccount;
    if (!this.authSrv.currentAccount) {
      this.appSrv.getDefaultLanguage().subscribe(lang => {
          this.translate.setDefaultLang(lang);
          this.translate.use(lang);
        }
      )
    }
  }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.type = params['type'];
      switch (this.type) {
        case 'account':
          this.message = 'error.token.invalid';
          break;
        case 'expired':
          this.message = 'error.token.expired';
          break;
        case '404':
          this.message = 'error.404';
          break;
        case '403':
          this.message = 'error.403';
          break;
      }
    });
  }

}
