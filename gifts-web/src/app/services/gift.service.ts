import {Injectable} from '@angular/core';
import {ApiService} from "@services/api.service";
import {Gift} from "@model/Gift";
import {Observable} from "rxjs";
import {environment} from "@env/environment.prod";

@Injectable({
  providedIn: 'root'
})
export class GiftService {

  constructor(private apiSrv: ApiService) {
  }


  getUserGifts(identification: string): Observable<Map<string, Gift[]>> {
    if (identification) {
      return this.apiSrv.get(`${environment.gift_url}/user/${identification}`);
    } else {
      return this.apiSrv.get(`${environment.gift_url}/mine`);
    }
  }

}
