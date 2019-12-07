import {Component, OnInit} from '@angular/core';
import {GiftService} from "@services/gift.service";
import {Router} from "@angular/router";
import {Gift} from "@model/Gift";
import {Account} from "@model/Account";
import {TranslateService} from "@ngx-translate/core";
import {UserService} from "@services/user.service";

@Component({
  selector: 'app-gift-claimed',
  templateUrl: './gift-claimed.component.html',
  styleUrls: ['gift-claimed.component.css']
})
export class GiftClaimedComponent implements OnInit {

  identification: string;
  gifts: Map<Account, Gift[]>;
  accountsList: Account[];
  avatar: string = 'assets/images/avatar-placeholder.png';
  currentAccount: Account;

  constructor(private router: Router, private giftSrv: GiftService, private userSrv: UserService) {
  }

  ngOnInit() {
    this.getGifts()
  }

  private getGifts() {
    this.giftSrv.getClaimedGifts().subscribe(result => {
      this.gifts = result.claimedGifts;
      this.accountsList = result.accounts;
      this.userSrv.fetchAvatars(this.accountsList);
    }, () => {
      this.router.navigate(['/']);
    });
  }

  trackByFn(index, item) {
    return item.id;
  }

}
