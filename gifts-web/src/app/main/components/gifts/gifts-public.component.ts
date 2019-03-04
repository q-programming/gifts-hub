import {Component, OnInit} from '@angular/core';
import {UserService} from "../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {GiftService} from "../../services/gift.service";
import {AuthenticationService} from "../../../core/services/authentication.service";
import {AlertService} from "../../../core/services/alert.service";
import {Gift, GiftStatus} from "../../../model/Gift";
import {Account} from "../../../model/Account";
import {TranslateService} from "@ngx-translate/core";
import {AvatarService} from "../../../core/services/avatar.service";
import {ApiService} from "../../../core/services/api.service";
import {environment} from "../../../../environments/environment";

@Component({
  selector: 'app-gifts-public',
  templateUrl: './gifts-public.component.html',
  styleUrls: ['gifts-public.component.css']
})
export class GiftsPublicComponent implements OnInit {
  identification: string;
  categorizedGifts: Map<string, Gift[]>;
  realizedGifts: Gift[] = [];
  unCategorizedGifts: Gift[] = [];
  GiftStatus = GiftStatus;
  label_other: string;
  label_realised: string;
  avatar: string = 'assets/images/avatar-placeholder.png';
  currentAccount: Account;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private giftSrv: GiftService,
              private authSrv: AuthenticationService,
              private alertSrv: AlertService,
              private userSrv: UserService,
              private translate: TranslateService,
              private avatarSrv: AvatarService,
              private apiSrv: ApiService) {
  }

  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
    this.apiSrv.get(`${environment.app_url}/default-language`).subscribe(defaults => {
      if (defaults) {
        let lang = defaults.language;
        this.translate.setDefaultLang(lang);
        this.translate.use(lang)
        this.translate.get('gift.category.other').subscribe(value => this.label_other = value);
        this.translate.get('gift.category.realised').subscribe(value => this.label_realised = value);
      }
    });
    this.activatedRoute.params.subscribe(params => {
      this.identification = params['user'];
      //get gift list
      this.giftSrv.getUserGifts(this.identification).subscribe(result => {
        this.avatarSrv.getUserAvatarByUsername(this.identification).subscribe(avatar => this.avatar = avatar);
        this.processList(result);
      }, () => {
        this.alertSrv.error("gift.list.public.error");
        this.router.navigate(['/']);
      });
    });
  }

  private processList(result: Map<string, Gift[]>) {
    this.categorizedGifts = result;
    this.realizedGifts = this.categorizedGifts[GiftStatus.REALISED];
    this.unCategorizedGifts = this.categorizedGifts[''];
    delete this.categorizedGifts[GiftStatus.REALISED];
    delete this.categorizedGifts[''];
  }

}
