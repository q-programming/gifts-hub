import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Gift, GiftStatus} from "@model/Gift";
import {AuthenticationService} from "@core-services/authentication.service";
import {Account} from "@model/Account";
import {GiftService} from "@services/gift.service";
import {AlertService} from "@core-services/alert.service";
import {NGXLogger} from "ngx-logger";
import {UserService} from "@services/user.service";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {ImageDialogComponent} from "../../../../components/dialogs/image/image-dialog.component";

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
  createdBy: Account;


  constructor(private authSrv: AuthenticationService,
              private giftSrv: GiftService,
              private alertSrv: AlertService,
              private userSrv: UserService,
              public dialog: MatDialog,
              private logger: NGXLogger) {
  }


  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
    if (!this.public && this.gift.createdBy && this.gift.userId != this.gift.createdBy) {
      this.userSrv.geUserById(this.gift.createdBy).subscribe(acc => this.createdBy = acc)
    }
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
      if(error.status === 409){
        this.alertSrv.warning('gift.claim.claimedError');
        this.refresh.emit(true);
      }

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

  showImage() {
    if (this.gift.id > -1) {
      const dialogConfig: MatDialogConfig = {
        disableClose: true,
        panelClass: 'gifts-dialog-modal',
        data: this.gift.image
      };
      if (!this.gift.image) {
        this.giftSrv.loadImage(this.gift).subscribe(gift => {
          this.gift = gift;
          dialogConfig.data = gift.image;
          this.dialog.open(ImageDialogComponent, dialogConfig);
        })
      } else {
        this.dialog.open(ImageDialogComponent, dialogConfig);
      }
    }
  }
}
