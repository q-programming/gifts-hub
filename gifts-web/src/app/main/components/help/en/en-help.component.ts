import {Component, Input, OnInit} from '@angular/core';
import {ScrollToConfigOptions, ScrollToService} from "@nicky-lenaers/ngx-scroll-to";
import {ActivatedRoute} from "@angular/router";
import {Account} from "@model/Account";
import {Gift, GiftStatus} from "@model/Gift";


@Component({
  selector: 'en-help',
  templateUrl: './en-help.component.html',
  styleUrls: ['../help.component.css']
})
export class EnHelpComponent implements OnInit {
  fragment: string;

  @Input() isAdmin: boolean;
  @Input() user: Account;
  @Input() admins: Account[];

  gift: Gift;
  gift_claimed: Gift;
  gift_realized: Gift;

  constructor(private route: ActivatedRoute,
              private scrollToService: ScrollToService) {
  }

  ngOnInit() {
    this.route.fragment.subscribe(fragment => {
      this.fragment = fragment;
      const config: ScrollToConfigOptions = {
        target: this.fragment,
        offset: -75
      };
      this.scrollToService.scrollTo(config);
      this.fragment = undefined
    });
    this.createGifts();
  }

  private createGifts() {
    this.gift = {
      id: -1,
      name: 'Electric train',
      category: {name: 'Toys'},
      description: 'Wooden if possible, can be metal',
      hidden: true,
      hasImage:true,
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
      name: 'Soccer ball',
      category: {name: 'Toys'},
      description: 'Black and white',
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
      name: 'Toy car',
      status: GiftStatus.REALISED,
      category: {name: 'Toys'},
      realised: new Date(),
      description: 'Remotely controlled, Must be red',
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
