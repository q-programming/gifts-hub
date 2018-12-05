import {Injectable} from '@angular/core';
import {Account} from "@model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "@env/environment";
import {Observable} from "rxjs";

@Injectable()
export class AvatarService {

    constructor(private logger: NGXLogger, private api: ApiService) {
    }

    /**
     * Returns Observable with base64 string data of avatar.
     * Firstly local storage is checked if image was not already wrote there.
     * If nothing is found in local storage, read avatar from database and persist it on local storage
     *
     * @param id user id for which avatar should be read
     */
    getUserAvatarById(id: string): Observable<string> {
        return new Observable((observable) => {
            let image = localStorage.getItem("avatar:" + id);
            if (!image) {
                this.logger.debug(`Getting avatar from DB for user ${id}`);
                this.api.getObject(environment.account_url + `/${id}${environment.avatar_url}`).subscribe(result => {
                    if (result) {
                        const dataType = "data:" + result.type + ";base64,";
                        image = dataType + result.image;
                        localStorage.setItem("avatar:" + id, image);
                    } else {
                        image = 'assets/images/avatar-placeholder.png';
                        localStorage.setItem("avatar:" + id, image);
                    }
                    observable.next(image);
                    observable.complete();
                });
            } else {
                this.logger.debug(`Fetching avatar from localStorage for account : ${id}`);
                observable.next(image);
                observable.complete();
            }
        });
    }

    /**
     * Get avatar for account
     *
     * @see AvatarService.getUserAvatarById
     * @param account
     */
    getUserAvatar(account: Account): Observable<string> {
        return this.getUserAvatarById(account.id);

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
            localStorage.removeItem("avatar:" + account.id);
            this.getUserAvatar(account).subscribe(avatar => {
                account.avatar = avatar;
            });
        })
    }
}
