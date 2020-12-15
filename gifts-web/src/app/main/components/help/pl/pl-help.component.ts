import {Component, Input, OnInit} from '@angular/core';
import {Account} from "@model/Account";
import {Gift, GiftStatus} from "@model/Gift";


@Component({
  selector: 'pl-help',
  templateUrl: './pl-help.component.html',
  styleUrls: ['../help.component.css']
})
export class PlHelpComponent implements OnInit {
  fragment: string;
  @Input() isAdmin: boolean;
  @Input() user: Account;
  @Input() admins: Account[];
  gift: Gift;
  gift_claimed: Gift;
  gift_realized: Gift;

  constructor() {
  }

  ngOnInit() {
    this.createGifts();
  }

  private createGifts() {
    this.gift = {
      id: -1,
      name: 'Kolejka elektryczna',
      category: {name: 'Zabawki'},
      description: 'Najlepiej drewniania, ew metalowa',
      hidden: true,
      hasImage: true,
      links: ['#'],
      createdBy: this.user.id,
      engines: [
        {
          name: 'google',
          icon: 'fa-google',
          id: 1,
          searchString: '#'
        }
      ],
      created: new Date()
    };
    this.gift_claimed = {
      id: -1,
      name: 'Piłka do nogi',
      category: {name: 'Zabawki'},
      description: 'Biało czarna',
      claimed: this.user,
      links: ['#'],
      engines: [
        {
          name: 'google',
          icon: 'fa-google',
          id: 1,
          searchString: '#'
        }
      ],
      created: new Date()
    };
    this.gift_realized = {
      id: -1,
      name: 'Samochodzik',
      status: GiftStatus.REALISED,
      category: {name: 'Zabawki'},
      realised: new Date(),
      description: 'Zdalnie sterowany, koniecznie czerwony',
      links: ['#'],
      engines: [
        {
          name: 'google',
          icon: 'fa-google',
          id: 1,
          searchString: '#'
        }
      ],
      created: new Date()
    }
  }

  menuClick(event: Event) {
    event.stopPropagation();
  }

}
