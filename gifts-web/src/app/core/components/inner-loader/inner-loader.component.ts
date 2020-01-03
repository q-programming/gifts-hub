import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'inner-loader',
  templateUrl: './inner-loader.component.html',
  styleUrls: ['./inner-loader.component.css']
})
export class InnerLoaderComponent implements OnInit {

  @Input() blue: boolean;

  constructor() {
  }

  ngOnInit() {
  }

}
