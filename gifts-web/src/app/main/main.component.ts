import {Component, OnInit} from '@angular/core';
import {ViewportScroller} from "@angular/common";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styles: []
})
export class MainComponent implements OnInit {

  constructor(private viewportScroller: ViewportScroller) {
  }

  ngOnInit() {
  }

  backToTop() {
    this.viewportScroller.scrollToAnchor('top');
  }

}
