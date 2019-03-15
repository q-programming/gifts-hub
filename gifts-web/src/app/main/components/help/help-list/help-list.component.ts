import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'help-list',
  templateUrl: './help-list.component.html',
  styles: []
})
export class HelpListComponent implements OnInit {

  @Input() name: string;
  @Input() icon: string;

  constructor() {
  }

  ngOnInit() {
  }
}
