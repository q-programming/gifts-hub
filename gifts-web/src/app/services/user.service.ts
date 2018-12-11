import {Injectable} from '@angular/core';
import {ApiService} from "@services/api.service";
import {Family} from "@model/Family";
import {Account} from "@model/Account";
import {Observable} from "rxjs";
import {environment} from "@env/environment.prod";
import {AvatarService} from "@services/avatar.service";
import {SortBy} from "@model/Settings";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private apiSrv: ApiService, private avatarSrv: AvatarService) {
  }

  getFamily(identification?: string): Observable<Family> {
    return this.apiSrv.get(`${environment.account_url}/family`, {username: identification})
  }

  getRelatedUsers(identification: string): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/userList`, {username: identification}).map(accounts => {
        return this.fetchAvatars(accounts);
      }
    )
  }


  getDefaultSorting(): Observable<SortBy> {
    return this.apiSrv.get(`${environment.app_url}/sort`)
  }

  getAllFamilies(): Observable<Family[]> {
    return this.apiSrv.get(`${environment.account_url}/families`).map(families => {
      (families as Family[]).forEach(family => {
        this.fetchAvatars(family.members)

      });
      return families;
    })
  }

  getUsersWithoutFamily(): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/users`, {noFamily: true}).map(users => {
      return this.fetchAvatars(users as Account[]);
    })
  }

  getAllUsers(): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/users`).map(users => {
      return this.fetchAvatars(users as Account[]);
    })
  }

  private fetchAvatars(accounts) {
    accounts.forEach(account => {
      this.avatarSrv.getUserAvatarByUsername(account.username).subscribe(avatar => {
        account.avatar = avatar;
      })
    });
    return accounts
  }

}
