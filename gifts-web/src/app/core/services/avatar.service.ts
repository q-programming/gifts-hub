import {Injectable} from '@angular/core';
import {Account} from "../../model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "../../../environments/environment";
import {Observable} from "rxjs";

@Injectable()
export class AvatarService {

    constructor(private logger: NGXLogger, private api: ApiService) {
    }

    /**
     * Returns Observable with base64 string data of avatar.
     * Firstly session storage is checked if image was not already wrote there.
     * If nothing is found in session storage, read avatar from database and persist it on local storage
     *
     * @param username user id for which avatar should be read
     */
    getUserAvatarByUsername(username: string): Observable<string> {
        return new Observable((observable) => {
            let image = sessionStorage.getItem("avatar:" + username);
            if (!image) {
                this.logger.debug(`Getting avatar from DB for user ${username}`);
                this.api.getObject(environment.account_url + `/${username}${environment.avatar_url}`).subscribe(result => {
                    if (result) {
                        const dataType = "data:" + result.type + ";base64,";
                        image = dataType + result.image;
                        sessionStorage.setItem("avatar:" + username, image);
                    } else {
                        image = 'assets/images/avatar-placeholder.png';
                        sessionStorage.setItem("avatar:" + username, image);
                    }
                    observable.next(image);
                    observable.complete();
                });
            } else {
                this.logger.debug(`Fetching avatar from sessionStorage for account : ${username}`);
                observable.next(image);
                observable.complete();
            }
        });
    }

    /**
     * Get avatar for account
     *
     * @see AvatarService.getUserAvatarByUsername
     * @param account
     */
    getUserAvatar(account: Account): Observable<string> {
        return this.getUserAvatarByUsername(account.username);

    }

    /**
     * Updates avatar data for given account.
     * Data for that account is removed from localstorage. But it only removed currently logged user ( other users will still see old avatar,
     * until their local storage is cleared ( for ex. logout )
     *
     * @param base64Image new avatar base 64 data
     * @param account account for which avatar is updated
     */
    updateAvatar(base64Image: String, account: Account) {
        return this.api.post(`${environment.account_url}${environment.avatar_upload_url}`, base64Image).subscribe(() => {
            localStorage.removeItem("avatar:" + account.username);
            this.getUserAvatar(account).subscribe(avatar => {
                account.avatar = avatar;
            });
        })
    }

  /**
   * Reloads account avatar by removing it from locastorage and fetching it once more
   * @param account account for which avatar will be reloaded
   */
  reloadAvatar(account: Account){
    localStorage.removeItem("avatar:" + account.username);
    this.getUserAvatar(account).subscribe(avatar => {
      account.avatar = avatar;
    });
    }
}
