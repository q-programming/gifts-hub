import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {environment} from "@env/environment";
import {TranslateService} from "@ngx-translate/core";
import {ApiService} from "@services/api.service";
import {AuthenticationService} from "@services/authentication.service";

@Component({
    selector: 'app-error',
    templateUrl: './error.component.html',
    styles: []
})
export class ErrorComponent implements OnInit {

    type: string;
    message: string;

    constructor(private activatedRoute: ActivatedRoute,
                private translate: TranslateService,
                private apiService: ApiService,
                private authSrv:AuthenticationService) {
        if(!this.authSrv.currentAccount){
            this.apiService.get(environment.default_lang_url).subscribe(defaults => {
                if (defaults) {
                    let lang = defaults.language;
                    this.translate.setDefaultLang(lang);
                    this.translate.use(lang)
                }
            })
        }
    }

    ngOnInit() {
        this.activatedRoute.queryParams.subscribe(params => {
            this.type = params['type'];
            switch (this.type) {
                case 'account':
                    this.message = 'app.error.token.invalid';
                    break;
                case 'expired':
                    this.message = 'app.error.token.expired';
                    break;
                case '404':
                    this.message = 'app.error.404';
                    break;
            }
        });
    }

}
