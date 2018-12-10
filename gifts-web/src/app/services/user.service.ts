import {Injectable} from '@angular/core';
import {ApiService} from "@services/api.service";
import {Family} from "@model/Family";
import {Account} from "@model/Account";
import {Observable} from "rxjs";
import {environment} from "@env/environment.prod";
import {AvatarService} from "@services/avatar.service";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private apiSrv: ApiService, private avatarSrv: AvatarService) {
  }

  getFamily(identification: string): Observable<Family> {
    return this.apiSrv.get(`${environment.account_url}/family`, {username: identification})
  }

  getUsers(identification: string): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/userList`, {username: identification}).map(accounts => {
        accounts.forEach(account => {
          this.avatarSrv.getUserAvatarByUsername(account.username).subscribe(avatar => {
            account.avatar = avatar;
          })
        });
        return accounts
      }
    )
  }
}
