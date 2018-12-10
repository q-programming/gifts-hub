import {Component, Input, OnInit} from '@angular/core';
import {Gift, GiftStatus} from "@model/Gift";
import {AuthenticationService} from "@services/authentication.service";
import {Account} from "@model/Account";
import {GiftService} from "@services/gift.service";
import {AlertService} from "@services/alert.service";
import {NGXLogger} from "ngx-logger";

@Component({
  selector: 'gift',
  templateUrl: './gift.component.html',
  styleUrls: ['./gift.component.css']
})
export class GiftComponent implements OnInit {

  GiftStatus = GiftStatus;
  @Input() gift: Gift;
  @Input() odd: boolean;
  currentAccount: Account;

  constructor(private authSrv: AuthenticationService, private giftSrv: GiftService, private alertSrv: AlertService, private logger: NGXLogger) {
  }


  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
  }


  canBeClaimed(): boolean {
    return this.gift.status !== GiftStatus.REALISED && !this.gift.claimed && this.gift.userId !== this.currentAccount.id;
  }

  canBeUnClaimed(): boolean {
    return this.gift.status !== GiftStatus.REALISED && (this.gift.claimed && this.gift.claimed.id === this.currentAccount.id)
  }

  claim() {
    this.giftSrv.claim(this.gift).subscribe(result => {
      if (result) {
        this.alertSrv.success('gift.claim.success', {name: this.gift.name});
        this.gift = result;
      }
    }, error => {
      this.logger.error(error);
    })
  }

  unClaim() {
    this.giftSrv.unclaim(this.gift).subscribe(result => {
      if (result) {
        this.alertSrv.success('gift.unclaim.success', {name: this.gift.name});
        this.gift = result;
      }
    }, error => {
      this.logger.error(error);
    })
  }


}
