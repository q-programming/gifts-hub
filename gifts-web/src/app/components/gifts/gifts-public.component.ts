import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-gifts-public',
  templateUrl: './gifts-public.component.html',
  styles: []
})
export class GiftsPublicComponent implements OnInit {
  avatar: string = 'assets/images/avatar-placeholder.png';

  constructor() {
  }

  ngOnInit() {
  }

}
