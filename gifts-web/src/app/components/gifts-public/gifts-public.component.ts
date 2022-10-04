import {Component, OnInit} from '@angular/core';
import {UserService} from "@services/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {GiftService} from "@services/gift.service";
import {AuthenticationService} from "@core-services/authentication.service";
import {AlertService} from "@core-services/alert.service";
import {Gift, GiftStatus} from "@model/Gift";
import {Account} from "@model/Account";
import {TranslateService} from "@ngx-translate/core";
import {AvatarService} from "@core-services/avatar.service";
import {AppService} from "@core-services/app.service";


@Component({
  selector: 'app-gifts-public',
  templateUrl: './gifts-public.component.html',
  styleUrls: ['gifts-public.component.css']
})
export class GiftsPublicComponent implements OnInit {
  identification: string;
  categorizedGifts: Map<string, Gift[]>;
  categorizedKeys: string[];
  realizedGifts: Gift[] = [];
  unCategorizedGifts: Gift[] = [];
  GiftStatus = GiftStatus;
  label_other: string;
  label_realised: string;
  avatar: string = 'assets/images/avatar-placeholder.png';
  currentAccount: Account;
  isLoading: boolean;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private giftSrv: GiftService,
              private authSrv: AuthenticationService,
              private alertSrv: AlertService,
              private userSrv: UserService,
              private translate: TranslateService,
              private avatarSrv: AvatarService,
              private appSrv: AppService) {
  }

  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
    this.appSrv.getDefaultLanguage().subscribe(lang => {
      this.translate.setDefaultLang(lang);
      this.translate.use(lang)
      this.translate.get('gift.category.other').subscribe(value => this.label_other = value);
      this.translate.get('gift.category.realised.text').subscribe(value => this.label_realised = value);
    })

    this.activatedRoute.params.subscribe(params => {
      this.identification = params['user'];
      //get gift list
      this.isLoading = true;
      this.giftSrv.getUserGifts(this.identification).subscribe(result => {
        this.fetchRealised();
        this.avatarSrv.getUserAvatarByUsername(this.identification).subscribe(avatar => this.avatar = avatar);
        this.processList(result);
      }, () => {
        this.alertSrv.error("gift.list.public.error");
        this.router.navigate(['/']);
      });
    });
  }

  private fetchRealised() {
    this.giftSrv.getRealisedGifts(this.identification).subscribe(result => {
      this.realizedGifts = result[GiftStatus.REALISED];
    })
  }

  private processList(result: Map<string, Gift[]>) {
    this.categorizedGifts = result;
    this.unCategorizedGifts = this.categorizedGifts[''];
    delete this.categorizedGifts[''];
    this.categorizedKeys = Object.keys(this.categorizedGifts);
    this.isLoading = false;
  }

  trackByFn(index, item) {
    return item.id;
  }

}
