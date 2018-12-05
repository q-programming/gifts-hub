import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import 'rxjs/add/observable/throw';
import {serialize} from "../utils/serialize";
import {environment} from "@env/environment";
import {AlertService} from "./alert.service";
import {NgProgress} from "ngx-progressbar";

export enum RequestMethod {
    Get = 'GET',
    Head = 'HEAD',
    Post = 'POST',
    Put = 'PUT',
    Delete = 'DELETE',
    Options = 'OPTIONS',
    Patch = 'PATCH'
}

@Injectable()
export class ApiService {

    headers = new HttpHeaders({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    });

    constructor(private http: HttpClient, private alertSrv: AlertService, public ngProgress: NgProgress) {
    }

    get(path: string, args?: any): Observable<any> {
        path = environment.context + path;
        const options = {
            headers: this.headers,
            withCredentials: true
        };
        if (args) {
            options['params'] = serialize(args);
        }
        return this.http.get(path, options)
            .catch(this.checkError.bind(this));
    }

    getObject<R>(path: string, args?: any): Observable<any> {
        path = environment.context + path;
        const options = {
            headers: this.headers,
            withCredentials: true
        };
        if (args) {
            options['params'] = serialize(args);
        }
        return this.http.get<R>(path, options).catch(this.checkError.bind(this));
    }

    postObject<R>(path: string, body: any, customHeaders?: HttpHeaders): Observable<any> {
        return this.requestObject<R>(path, body, RequestMethod.Post, customHeaders);
    }


    post(path: string, body: any, customHeaders?: HttpHeaders): Observable<any> {
        return this.request(path, body, RequestMethod.Post, customHeaders);
    }

    put(path: string, body: any): Observable<any> {
        return this.request(path, body, RequestMethod.Put);
    }

    delete(path: string, body?: any): Observable<any> {
        return this.request(path, body, RequestMethod.Delete);
    }

    private requestObject<R>(path: string, body: any, method = RequestMethod.Post, custemHeaders?: HttpHeaders): Observable<any> {
        path = environment.context + path;
        const req = new HttpRequest(method, path, body, {
            headers: custemHeaders || this.headers,
            withCredentials: true
        });

        return this.http.request<R>(req)
            .map((response: HttpResponse<R>) => response.body)
            .catch(error => this.checkError(error));
    }

    private request(path: string, body: any, method = RequestMethod.Post, custemHeaders?: HttpHeaders): Observable<any> {
        this.ngProgress.start();
        path = environment.context + path;
        const req = new HttpRequest(method, path, body, {
            headers: custemHeaders || this.headers,
            withCredentials: true
        });

        return this.http.request(req)
            .filter(response => response instanceof HttpResponse)
            .map((response: HttpResponse<any>) => {
                this.ngProgress.done();
                return response.body
            })
            .catch(error => this.checkError(error));
    }

    // Display error if logged in, otherwise redirect to IDP
    private checkError(error: any): any {
        if (error && error.status === 401) {
            this.alertSrv.error('app.api.error.unauthorized');
            // this.redirectIfUnauth(error);
        } else if (error && error.status === 404) {
            this.alertSrv.error('app.api.error.notfound');
        } else if (error && error.status === 403) {
            this.alertSrv.error('app.api.error.unauthorized');
            //TODO redirect to login?
        }
        else if (error && error.status === 503) {
        }
        throw error;
    }

}
