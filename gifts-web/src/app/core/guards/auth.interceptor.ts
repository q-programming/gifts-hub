import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let user = localStorage.getItem('currentAccount');
        if (user) {
            let currentUser = JSON.parse(user);
            request = request.clone({
                setHeaders: {
                    Authorization: `Basic ${currentUser.auth}`
                }
            });
        }
        return next.handle(request);
    }
}
