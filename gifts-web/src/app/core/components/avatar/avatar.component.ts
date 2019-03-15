import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'avatar',
  templateUrl: './avatar.component.html',
  styles: []
})
export class AvatarComponent implements OnInit {

  @Input() avatar: string;
  @Input() classes: string;

  constructor() {
  }

  ngOnInit() {
  }

}
