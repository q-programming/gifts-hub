import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Gift, GiftStatus} from "@model/Gift";
import {AuthenticationService} from "@core-services/authentication.service";
import {Account} from "@model/Account";
import {GiftService} from "@services/gift.service";
import {AlertService} from "@core-services/alert.service";
import {NGXLogger} from "ngx-logger";

@Component({
  selector: 'gift',
  templateUrl: './gift.component.html',
  styleUrls: ['./gift.component.css']
})
export class GiftComponent implements OnInit {

  GiftStatus = GiftStatus;
  @Input() gift: Gift;
  @Input() even: boolean;
  @Input() canEdit: boolean;
  @Input() public: boolean;
  @Output() refresh = new EventEmitter<boolean>();
  @Output() delete = new EventEmitter<Gift>();
  @Output() edit = new EventEmitter<Gift>();
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

  complete() {
    this.giftSrv.complete(this.gift).subscribe(result => {
      this.alertSrv.success('gift.complete.success', {name: this.gift.name});
      this.refresh.emit(true);
    }, error1 => {
      this.alertSrv.error('gift.complete.error');
    })
  }

  undoComplete() {
    this.giftSrv.undoComplete(this.gift).subscribe(() => {
      this.alertSrv.success('gift.complete.undo.success');
      this.refresh.emit(true);
    }, error1 => {
      this.alertSrv.error('gift.complete.error');
    })

  }

  /**
   * Notify parent component about gift deletion and show undoable message which will trigger actual deletion after timeout
   */
  deleteGift() {
    this.delete.emit(this.gift);
  }

  editGift() {
    this.edit.emit(this.gift);
  }


}
