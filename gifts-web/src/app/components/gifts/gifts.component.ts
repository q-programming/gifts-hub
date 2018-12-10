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
              private userSrv: UserService) {
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
      this.family = family;
      this.isFamilyAdmin = _.find(family.admins, (admin) => admin.id === this.currentAccount.id) !== undefined;
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


}
