import {Component, Input, OnInit} from '@angular/core';
import {Gift, GiftStatus} from "@model/Gift";
import {AuthenticationService} from "@services/authentication.service";
import {Account} from "@model/Account";

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

  constructor(private authSrv: AuthenticationService) {
  }


  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
  }

}
