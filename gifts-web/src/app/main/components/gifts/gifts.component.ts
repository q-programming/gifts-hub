import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {GiftService} from "@services/gift.service";
import {Gift, GiftStatus} from "@model/Gift";
import {AvatarService} from "@core-services/avatar.service";
import {AuthenticationService} from "@core-services/authentication.service";
import {UserService} from "@services/user.service";
import {Group} from "@model/Group";
import {Account} from "@model/Account";
import * as _ from "lodash"
import {AlertService} from "@core-services/alert.service";
import {NGXLogger} from "ngx-logger";
import {MatDialog} from "@angular/material/dialog";
import {GiftDialogComponent} from "./gift-dialog/gift-dialog.component";
import {TranslateService} from "@ngx-translate/core";
import {CategoryOption} from "@model/Category";
import {NgProgress, NgProgressRef} from "@ngx-progressbar/core";

@Component({
  selector: 'gifts-list',
  templateUrl: './gifts.component.html',
  styleUrls: ['gifts.component.css'],
})
export class GiftsComponent implements OnInit {

  //accounts
  identification: string;
  group: Group;
  isUserList: boolean;
  currentAccount: Account;
  viewedAccount: Account;
  //gifts
  categorizedGifts: Map<string, Gift[]>;
  categorizedKeys: string[];
  realizedGifts: Gift[] = [];
  unCategorizedGifts: Gift[] = [];
  GiftStatus = GiftStatus;

  avatar: string;

  label_realised: string;
  label_other: string;
  categories: CategoryOption[];
  filteredCategory: string;
  filter: boolean;
  filterTabOpen: string;
  canEditAll: boolean;
  noGifts: boolean;

  progress: NgProgressRef;


  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private giftSrv: GiftService,
              private avatarSrv: AvatarService,
              private authSrv: AuthenticationService,
              private userSrv: UserService,
              private alertSrv: AlertService,
              public dialog: MatDialog,
              private logger: NGXLogger,
              private translate: TranslateService,
              public ngProgress: NgProgress) {
    this.progress = ngProgress.ref();
  }

  ngOnInit() {
    //get translations
    this.translate.get('gift.category.other').subscribe(value => this.label_other = value);
    this.translate.get('gift.category.realised').subscribe(value => this.label_realised = value);
    this.currentAccount = this.authSrv.currentAccount;
    this.activatedRoute.params.subscribe(params => {
      this.identification = params['user'];
      //get gift list
      this.isUserList = !this.identification || this.identification === this.currentAccount.username;
      this.getGifts();
      this.getAvatar(this.identification)
    });
  }


  private getGifts() {
    this.progress.start();
    this.giftSrv.getUserGifts(this.identification).subscribe(result => {
      if (this.identification) {
        this.userSrv.canEditAll(this.identification).subscribe(result => this.canEditAll = result);
      }
      this.processList(result);
    }, () => {
      this.router.navigate(['/']);
    });
  }

  private processList(result: Map<string, Gift[]>) {
    this.categorizedGifts = result;
    this.noGifts = Object.keys(result).length == 0;
    this.categories = Object.keys(result).map(key => {
      return {
        key: key ? key : '####',
        name: this.getCategoryName(key)
      }
    });
    this.realizedGifts = this.categorizedGifts[GiftStatus.REALISED];
    this.unCategorizedGifts = this.categorizedGifts[''];
    delete this.categorizedGifts[GiftStatus.REALISED];
    delete this.categorizedGifts[''];
    this.categorizedKeys = Object.keys(this.categorizedGifts);
    this.progress.complete();
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
    } else if (gift.category.name === "") {
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
        gift: gift,
        familyUser: this.currentAccount.id !== this.viewedAccount.id
      }
    });
    dialogRef.afterClosed().subscribe((gift) => {
      if (gift) {
        gift.userId = this.viewedAccount.id;
        this.giftSrv.createGift(gift).subscribe(newGift => {
          if (newGift) {
            this.getGifts();
            this.alertSrv.success('gift.add.success', {name: newGift.name})
          }
        }, error => {
          this.switchErrors(error);
        })
      }
    });
  }

  editGiftDialog(gift: Gift) {
    const oldCategory = gift.category;
    const dialogRef = this.dialog.open(GiftDialogComponent, {
      panelClass: 'gifts-modal-normal', //TODO class needed
      data: {
        gift: gift,
        familyUser: this.currentAccount.id !== this.viewedAccount.id
      }
    });
    dialogRef.afterClosed().subscribe((gift) => {
      if (gift) {
        this.giftSrv.editGift(gift).subscribe(edited => {
          if (edited) {
            gift = edited;
            if (oldCategory.id !== gift.category.id) {
              this.getGifts();
            }
            this.alertSrv.success('gift.edit.success')
          }
        }, error => {
          this.switchErrors(error);
        })
      }
    });
  }

  changeViewedAccount(account: Account) {
    if (!account) {
      this.realizedGifts = [];
      this.unCategorizedGifts = [];
      this.categorizedGifts = new Map<string, Gift[]>();
      this.categorizedKeys = [];
    }
    this.viewedAccount = account;
  }


  trackByFn(index, item) {
    return item.id;
  }

  private switchErrors(error: any) {
    if (error.status === 404) {
      this.alertSrv.error('error.account.notFound')
    } else if (error.status === 409) {
      this.alertSrv.error('user.group.admin.error')
    } else {
      this.alertSrv.error('error.gif.general')
    }
  }

  canEdit(gift) {
    return gift.userId === this.currentAccount.id || gift.createdBy === this.currentAccount.id || this.canEditAll;
  }

  // Filtering
  toggleHelpMenu(): void {
    this.filterTabOpen = this.filterTabOpen === 'out' ? 'in' : 'out';
  }

  filterByCategory(category: string) {
    this.filteredCategory = category;
  }

  getCategoryName(key: string) {
    if (key === GiftStatus.REALISED) {
      return this.label_realised;
    } else if (key === '') {
      return this.label_other;
    }
    return key;
  }

  close(event: boolean) {
    this.filter = event
  }
}

