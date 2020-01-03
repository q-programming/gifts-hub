import {AfterViewInit, Component, ElementRef, OnInit} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {ActivatedRoute} from "@angular/router";
import {ScrollToService} from "@nicky-lenaers/ngx-scroll-to";
import {Account} from "@model/Account";
import {AuthenticationService} from "@core-services/authentication.service";
import {environment} from "@env/environment";

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
              private scrollToService: ScrollToService,
              private elem: ElementRef,
              private authSrv: AuthenticationService) {
  }

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
      this.toc = this.elem.nativeElement.querySelectorAll('h3,h4');
    }, 0)
  }
}
