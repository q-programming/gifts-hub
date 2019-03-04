import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import 'rxjs/add/observable/throw';
import {serialize} from "../../utils/serialize";
import {environment} from "@env/environment";
import {AlertService} from "./alert.service";
import {NgProgress, NgProgressRef} from "@ngx-progressbar/core";
import {TranslateService} from "@ngx-translate/core";


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

  progress: NgProgressRef;

  constructor(private http: HttpClient, private alertSrv: AlertService, public ngProgress: NgProgress, private translate: TranslateService) {
    this.progress = ngProgress.ref()
  }

  get(path: string, args?: any): Observable<any> {
    this.progress.start();
    path = environment.context + path;
    const options = {
      headers: this.headers,
      withCredentials: true
    };
    if (args) {
      options['params'] = serialize(args);
    }
    return this.http.get(path, options)
      .map((response) => {
        this.progress.complete();
        return response
      })
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

  login(path: string, body: any): Observable<any> {
    this.progress.start();
    const xformHeader = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');
    return this.request(path,body.toString(),RequestMethod.Post,xformHeader);
  }

  /**
   * Executes POST call to API
   * @param path URL of API call
   * @param body body to be passed
   * @param customHeaders custom headers
   */
  post(path: string, body?: any, customHeaders?: HttpHeaders): Observable<any> {
    return this.request(path, body, RequestMethod.Post, customHeaders);
  }

  put(path: string, body?: any): Observable<any> {
    return this.request(path, body, RequestMethod.Put);
  }

  delete(path: string, body?: any): Observable<any> {
    return this.request(path, body, RequestMethod.Delete);
  }

  private requestObject<R>(path: string, body: any, method = RequestMethod.Post, customHeaders?: HttpHeaders): Observable<any> {
    path = environment.context + path;
    const req = new HttpRequest(method, path, body, {
      headers: customHeaders || this.headers,
      withCredentials: true
    });

    return this.http.request<R>(req)
      .map((response: HttpResponse<R>) => response.body)
      .catch(error => this.checkError(error));
  }

  private request(path: string, body: any, method = RequestMethod.Post, customHeaders?: HttpHeaders): Observable<any> {
    this.progress.start();
    path = environment.context + path;
    const req = new HttpRequest(method, path, body, {
      headers: customHeaders || this.headers,
      withCredentials: true
    });

    return this.http.request(req)
      .filter(response => response instanceof HttpResponse)
      .map((response: HttpResponse<any>) => {
        this.progress.complete();
        return response.body
      })
      .catch(error => {
        return this.checkError(error)
      });
  }

  // Display error if logged in, otherwise redirect to IDP
  private checkError(error: any): any {
    this.progress.complete();
    if (error && error.status === 401) {
      this.alertSrv.error('error.api.unauthorized');
      // this.redirectIfUnauth(error);
    } else if (error && error.status === 404) {
      this.alertSrv.error('error.api.notfound');
    } else if (error && error.status === 403) {
      this.alertSrv.error('error.api.unauthorized');
    } else if (error && error.status === 503) {
    }
    throw error;
  }
}
