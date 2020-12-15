import {AfterViewInit, Component, ElementRef, OnInit} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {ActivatedRoute} from "@angular/router";
import {Account} from "@model/Account";
import {AuthenticationService} from "@core-services/authentication.service";
import {environment} from "@env/environment";
import {first} from 'rxjs/operators';
import {ViewportScroller} from '@angular/common';

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit, AfterViewInit {

  lang: string;
  toc: NodeList;
  isAdmin: boolean;
  user: Account;
  admins: Account[];
  version: string = environment.version;

  constructor(private translate: TranslateService,
              private route: ActivatedRoute,
              private elem: ElementRef,
              private viewportScroller: ViewportScroller,
              private authSrv: AuthenticationService) {
  }

  //TODO needs viewportScroller

  ngOnInit() {
    this.lang = this.translate.currentLang;
    this.isAdmin = this.authSrv.isAdmin();
    this.user = this.authSrv.currentAccount;
    this.authSrv.admins().subscribe(admins => {
      this.admins = admins
    })
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.toc = this.elem.nativeElement.querySelectorAll('h3,h4,h5');
      this.route.fragment.pipe(first()).subscribe(fragment => {
        this.viewportScroller.scrollToAnchor(fragment)
      });
    }, 0)
  }
}
