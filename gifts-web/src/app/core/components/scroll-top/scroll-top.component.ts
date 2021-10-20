import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'scroll-to-top',
  templateUrl: './scroll-top.component.html',
  styleUrls: ['./scroll-top.component.css']
})
export class ScrollTopComponent implements OnInit {

  windowScrolled: boolean;

  constructor(@Inject(DOCUMENT) private document: Document) {
  }

  @HostListener("window:scroll", [])

  onWindowScroll() {
    if (window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop > 100) {
      this.windowScrolled = true;
    } else if (this.windowScrolled && window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop < 10) {
      this.windowScrolled = false;
    }
  }

  scrollToTop() {
    window.scrollTo(0,0);
  }


  ngOnInit(): void {
  }

}
