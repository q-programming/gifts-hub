import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
  @Output() kid = new EventEmitter<Account>();


  constructor() {
  }

  ngOnInit() {
  }

  editKid() {
    if (this.user.type === AccountType.KID) {
      this.kid.emit(this.user);
    }
  }
}
