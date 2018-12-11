import {Component, Input, OnInit} from '@angular/core';
import {Account, AccountType} from "@model/Account";

@Component({
  selector: 'user',
  templateUrl: './user.component.html',
  styles: []
})
export class UserComponent implements OnInit {

  AccountType = AccountType;
  @Input() user: Account;
  @Input() even: boolean;
  @Input() isUserFamily: boolean;


  constructor() {
  }

  ngOnInit() {
  }

}
