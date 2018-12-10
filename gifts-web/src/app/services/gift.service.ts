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

  claim(gift: Gift): Observable<Gift> {
    return this.apiSrv.put(`${environment.gift_url}/claim/${gift.id}`,undefined)
  }
  unclaim(gift: Gift): Observable<Gift> {
    return this.apiSrv.put(`${environment.gift_url}/unclaim/${gift.id}`, undefined)
  }
}
