import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ApiService} from "@core-services/api.service";
import {AlertService} from "@core-services/alert.service";
import {environment} from "@env/environment.prod";

@Component({
  selector: 'app-confirm',
  templateUrl: './confirm.component.html',
  styles: []
})
export class ConfirmComponent implements OnInit {


  token: string;

  constructor(private apiSrv: ApiService, private alertSrv: AlertService,
              private activatedRoute: ActivatedRoute,
              private router: Router,) {
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.alertSrv.error('user.confirm.token.error.missing');
      }
      this.apiSrv.post(`${environment.account_url}/confirm`, this.token).subscribe(response => {
        switch (response.result) {
          case 'group_admin':
            this.alertSrv.success('user.confirm.group.admin.success');
            break;
          case 'group_member':
            this.alertSrv.success('user.confirm.group.success');
            break;
          case 'kid_confirmed':
            this.alertSrv.success('user.confirm.group.kid.success');
            break;
          case 'kid_already_confirmed':
            this.alertSrv.info('user.confirm.group.kid.alreadyConfirmed');
            break;
          case 'confirmed':
            this.alertSrv.success('user.register.confirmed');
            break;
        }
        this.router.navigate(['/']);
      }, error => {
        this.switchErrors(error)
      })
    });
  }

  private switchErrors(error: any) {
    if (error.status === 404) {
      this.alertSrv.error('user.confirm.token.error.missing');
    } else if (error.status === 409) {
      switch (error.error) {
        case 'expired':
          this.alertSrv.error('user.confirm.token.error.time');
          break;
        case 'group_exists':
          this.alertSrv.error('user.confirm.group.exists');
          break;
      }
    }
    this.router.navigate(['/']);
  }

}
