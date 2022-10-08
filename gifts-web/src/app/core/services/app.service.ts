import {Injectable} from '@angular/core';
import {ApiService} from "@core-services/api.service";
import {environment} from "@env/environment";
import {map, Observable, tap} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AppService {
  private defaultLanguage: string;
  tillBirthDayReminder: number;

  constructor(private apiSrv: ApiService) {
    this.getDefaultLanguage();
    this.apiSrv.get(`${environment.app_url}/birthday-reminder`).subscribe(reminder => this.tillBirthDayReminder = reminder && reminder.days ? reminder.days : 30);
  }

  getDefaultLanguage(): Observable<string> {
    if (!this.defaultLanguage) {
      return this.apiSrv.get(`${environment.app_url}/default-language`).pipe(
        map((result => result && result.language ? result.language : 'en')),
        tap(defaultLang => this.defaultLanguage = defaultLang)
      )
    } else {
      return new Observable<string>((observable) => {
        observable.next(this.defaultLanguage);
        observable.complete();
      });
    }
  }
}
