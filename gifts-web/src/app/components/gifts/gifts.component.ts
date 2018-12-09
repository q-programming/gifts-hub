import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {GiftService} from "@services/gift.service";
import {Gift, GiftStatus} from "@model/Gift";

@Component({
  selector: 'gifts-list',
  templateUrl: './gifts.component.html',
  styles: []
})
export class GiftsComponent implements OnInit {

  identification: string;
  categorizedGifts: Map<string, Gift[]>;
  realizedGifts: Gift[] = [];
  unCategorizedGifts: Gift[] = [];

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private giftSrv: GiftService) {
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.identification = params['user'];
      this.giftSrv.getUserGifts(this.identification).subscribe(result => {
        console.log(result);
        // Object.keys(result)
        // let all: Map<string, Gift[]> = result;
        // all.forEach((value, key) => {
        //   if (key === GiftStatus.REALISED) {
        //     console.log("Realised");
        //     console.log(value);
        //   } else if (key === '') {
        //     console.log("other");
        //     console.log(value);
        //   } else {
        //     console.log("normal category")
        //     console.log(value);
        //   }
        // });
        this.categorizedGifts = result as Map<string, Gift[]>;
        this.realizedGifts = this.categorizedGifts[GiftStatus.REALISED];
        this.unCategorizedGifts = this.categorizedGifts[''];
        delete this.categorizedGifts[GiftStatus.REALISED];
        delete this.categorizedGifts[''];
        console.log(this.realizedGifts);
        console.log(this.unCategorizedGifts);
      })
    });

  }

}
