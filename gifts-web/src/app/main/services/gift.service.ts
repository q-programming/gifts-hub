import {Injectable} from '@angular/core';
import {ApiService} from "@core-services/api.service";
import {Gift} from "@model/Gift";
import {map} from 'rxjs/operators';
import {Observable} from "rxjs";
import {environment} from "@env/environment.prod";

@Injectable({
  providedIn: 'root'
})
export class GiftService {

  constructor(private apiSrv: ApiService) {
  }


  getUserGifts(identification: string, realised?: boolean): Observable<Map<string, Gift[]>> {
    if (identification) {
      return this.apiSrv.get(`${environment.gift_url}/user/${identification}`);
    } else {
      return this.apiSrv.get(`${environment.gift_url}/mine`);
    }
  }

  getRealisedGifts(identification: string): Observable<Map<string, Gift[]>> {
    if (identification) {
      return this.apiSrv.get(`${environment.gift_url}/user/${identification}?realised=true`);
    } else {
      return this.apiSrv.get(`${environment.gift_url}/mine?realised=true`);
    }

  }

  getClaimedGifts(): Observable<Map<string, Gift[]>> {
    return this.apiSrv.get(`${environment.gift_url}/claimed`);
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

  loadImage(gift: Gift): Observable<Gift> {
    return this.apiSrv.get(`${environment.gift_url}/image/${gift.id}`)
      .pipe(map(result => {
        gift.image = result;
        return gift;
      }));
  }

}
