import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {GiftService} from "@services/gift.service";
import {Gift, GiftStatus} from "@model/Gift";
import {AvatarService} from "@services/avatar.service";
import {AuthenticationService} from "@services/authentication.service";
import {UserService} from "@services/user.service";
import {Family} from "@model/Family";
import {Account} from "@model/Account";
import * as _ from "lodash"
import {AlertService} from "@services/alert.service";
import {NGXLogger} from "ngx-logger";
import {MatDialog} from "@angular/material";
import {KidDialogComponent} from "../user-list/kid-dialog/kid-dialog.component";
import {GiftDialogComponent} from "./gift-dialog/gift-dialog.component";

@Component({
  selector: 'gifts-list',
  templateUrl: './gifts.component.html',
  styleUrls: ['gifts.component.css']
})
export class GiftsComponent implements OnInit {

  //accounts
  identification: string;
  family: Family;
  isUserList: boolean;
  isFamilyAdmin: boolean;
  currentAccount: Account;
  //gifts
  categorizedGifts: Map<string, Gift[]>;
  realizedGifts: Gift[] = [];
  unCategorizedGifts: Gift[] = [];
  avatar: string;


  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private giftSrv: GiftService,
              private avatarSrv: AvatarService,
              private authSrv: AuthenticationService,
              private userSrv: UserService,
              private alertSrv: AlertService,
              public dialog: MatDialog,
              private logger: NGXLogger) {
  }

  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
    this.activatedRoute.params.subscribe(params => {
      this.identification = params['user'];
      //get gift list
      this.isUserList = this.identification === this.currentAccount.username;
      this.getGifts();
      this.getFamily();
      this.getAvatar(this.identification)
    });
  }


  private getGifts() {
    this.giftSrv.getUserGifts(this.identification).subscribe(result => {
      this.processList(result);
    });
  }

  private getFamily() {
    this.userSrv.getFamily(this.identification).subscribe(family => {
      if (family) {
        this.family = family;
        this.isFamilyAdmin = _.find(family.admins, (admin) => admin.id === this.currentAccount.id) !== undefined;
      }
    });
  }


  private processList(result: Map<string, Gift[]>) {
    this.categorizedGifts = result;
    this.realizedGifts = this.categorizedGifts[GiftStatus.REALISED];
    this.unCategorizedGifts = this.categorizedGifts[''];
    delete this.categorizedGifts[GiftStatus.REALISED];
    delete this.categorizedGifts[''];
  }

  getAvatar(username: string) {
    if (!username) {
      username = this.currentAccount.username;
    }
    this.avatarSrv.getUserAvatarByUsername(username).subscribe(avatar => this.avatar = avatar)
  }


  refresh(event: boolean) {
    if (event) {
      this.getGifts();
    }
  }

  delete(gift: Gift) {
    if (gift) {
      this.removeGiftFromCollection(gift);
      this.alertSrv.undoable('gift.delete.success', {name: gift.name}).subscribe(undo => {
        if (undo !== undefined) {
          if (undo) {
            this.getGifts();
          } else {
            this.giftSrv.delete(gift).subscribe(() => {
            }, error => {
              this.alertSrv.error('gift.delete.error');
              this.logger.error(error)
            })
          }
        }
      })
    }
  }

  private removeGiftFromCollection(gift: Gift) {
    if (gift.status === GiftStatus.REALISED) {
      _.remove(this.realizedGifts, g => g.id === gift.id);
    } else if (gift.category.id === undefined) {
      _.remove(this.unCategorizedGifts, g => g.id === gift.id);
    } else {
      Object.keys(this.categorizedGifts).some((key) => {
        const foundGift = _.find(this.categorizedGifts[key], g => g.id === gift.id);
        if (foundGift) {
          _.remove(this.categorizedGifts[key], g => g.id === gift.id);
          return true;
        }
      })
    }
  }

  addGiftDialog() {
    let gift = new Gift();
    const dialogRef = this.dialog.open(GiftDialogComponent, {
      panelClass: 'gifts-modal-normal', //TODO class needed
      data: {
        gift: gift
      }
    });
    dialogRef.afterClosed().subscribe((gift) => {
      if (gift) {
        this.giftSrv.createGift(gift).subscribe(newGift => {
          if (newGift) {
            this.alertSrv.success('user.family.add.kid.success')
          }
        }, error => {
          this.switchErrors(error);
        })
      }
    });
  }
  editGiftDialog(gift:Gift) {
    const dialogRef = this.dialog.open(GiftDialogComponent, {
      panelClass: 'gifts-modal-normal', //TODO class needed
      data: {
        gift: gift
      }
    });
    dialogRef.afterClosed().subscribe((gift) => {
      if (gift) {
        this.giftSrv.editGift(gift).subscribe(edited => {
          if (edited) {
            this.alertSrv.success('user.family.add.kid.success')
          }
        }, error => {
          this.switchErrors(error);
        })
      }
    });
  }


  trackByFn(index, item) {
    return item.id;
  }

  private switchErrors(error: any) {
    //TODO implement
  }
}
