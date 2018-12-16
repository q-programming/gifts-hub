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
    return this.apiSrv.put(`${environment.gift_url}/claim/${gift.id}`)
  }

  unclaim(gift: Gift): Observable<Gift> {
    return this.apiSrv.put(`${environment.gift_url}/unclaim/${gift.id}`)
  }

  delete(gift: Gift) {
    return this.apiSrv.delete(`${environment.gift_url}/delete/${gift.id}`)
  }

  createGift(gift: Gift): Observable<Gift> {
    return this.apiSrv.post(`${environment.gift_url}/create`, gift)
  }

  editGift(gift: Gift): Observable<Gift> {
    return this.apiSrv.put(`${environment.gift_url}/edit`, gift)
  }

  complete(gift: Gift): Observable<Gift> {
    return this.apiSrv.put(`${environment.gift_url}/complete/${gift.id}`);
  }

  undoComplete(gift: Gift): Observable<Gift> {
    return this.apiSrv.put(`${environment.gift_url}/undo-complete/${gift.id}`)
  }
}
