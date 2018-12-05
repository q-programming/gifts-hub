import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthenticationService} from "@services/authentication.service";


@Injectable()
export class AdminGuard implements CanActivate {
    constructor(private router: Router, private authService: AuthenticationService) {
    }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.authService.currentAccount) {
            if (this.authService.isAdmin()) {
                return true;
            } else {
                this.router.navigate(['/403']);
                return false;
            }
        } else {
            console.log('NOT AN ADMIN ROLE');
            this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
            return false;
        }
    }
}

