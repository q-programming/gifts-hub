import {Component, OnInit} from '@angular/core';
import {GiftService} from "@services/gift.service";
import {Router} from "@angular/router";
import {Gift} from "@model/Gift";
import {Account} from "@model/Account";
import {UserService} from "@services/user.service";
import {NgProgress, NgProgressRef} from "@ngx-progressbar/core";

@Component({
  selector: 'app-gift-claimed',
  templateUrl: './gift-claimed.component.html',
  styleUrls: ['gift-claimed.component.css']
})
export class GiftClaimedComponent implements OnInit {

  gifts: Map<string, Gift[]> = new Map();
  accountsList: Account[] = [];
  private progress: NgProgressRef;
  private loading = true;

  constructor(private router: Router, private giftSrv: GiftService, private userSrv: UserService, public ngProgress: NgProgress) {
    this.progress = ngProgress.ref();
  }

  ngOnInit() {
    this.getGifts()
  }

  private getGifts() {
    this.progress.start();
    this.giftSrv.getClaimedGifts().subscribe(result => {
      let keys = Object.keys(result);
      for (let accountID of keys) {
        this.userSrv.geUserById(accountID).subscribe(account => {
          this.accountsList.push(account);
          this.gifts.set(account.id, result[accountID]);
          this.progress.complete();
          this.loading = false;
        })
      }
    }, () => {
      this.router.navigate(['/']);
    });
  }

  navigateToUser(event: Event, username: string) {
    event.stopPropagation();
    this.router.navigate(['/list/' + username])
  }

  trackByFn(index, item) {
    return item.id;
  }

}
