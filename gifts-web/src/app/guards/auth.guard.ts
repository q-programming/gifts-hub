import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Injectable} from "@angular/core";
import {AuthenticationService} from "@services/authentication.service";
import {NGXLogger} from "ngx-logger";


@Injectable()
export class AuthGuard implements CanActivate {

    constructor(private router: Router, private authSrv: AuthenticationService, private logger: NGXLogger) {
    }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        this.logger.debug(this.authSrv.currentAccount);
        if (this.authSrv.currentAccount) {
            // logged in so return true
            return true;
        }
        // not logged in so redirect to login page with the return url
        this.router.navigate(['/login']);
        return false;
    }

}
