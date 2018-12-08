import {Component, Input, OnInit} from '@angular/core';
import {Account} from "@model/Account";

@Component({
  selector: 'avatar',
  templateUrl: './avatar.component.html',
  styles: []
})
export class AvatarComponent implements OnInit {

  @Input() account: Account;
  @Input() classes: string;

  constructor() {
  }

  ngOnInit() {
  }

}
