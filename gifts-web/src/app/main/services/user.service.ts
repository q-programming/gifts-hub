import {Injectable} from '@angular/core';
import {ApiService} from "@core-services/api.service";
import {Group, GroupForm} from "@model/Group";
import {Account} from "@model/Account";
import {Observable} from "rxjs";
import {environment} from "@env/environment.prod";
import {AvatarService} from "@core-services/avatar.service";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import * as _ from "lodash";
import {ConfirmDialogData, ConfirmDialogComponent} from "../../components/dialogs/confirm/confirm-dialog.component";
import {SortBy} from "@model/AppSettings";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiSrv: ApiService, private avatarSrv: AvatarService, public dialog: MatDialog,) {
  }

  getRelatedUsers(identification?: string, gifts?: boolean): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/users`, {
      username: identification,
      gifts: gifts
    }).map(accounts => {
        return this.fetchAvatars(accounts);
      }
    )
  }


  getDefaultSorting(): Observable<SortBy> {
    return this.apiSrv.get(`${environment.app_url}/sort`)
  }

  getAllGroups(): Observable<Group[]> {
    return this.apiSrv.get(`${environment.account_url}/groups`).map(groups => {
      (groups as Group[]).forEach(group => {
        this.fetchAvatars(group.members);
        this.markAdmins(group)
      });
      return groups;
    })
  }

  getAllUsers(users?: boolean): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/usersList`, {users: users}).map(users => {
      return this.fetchAvatars(users as Account[]);
    })
  }

  geUserById(id: string): Observable<Account> {
    const accountStr = sessionStorage.getItem(id);
    if (accountStr) {
      return new Observable((observable) => {
        let account = JSON.parse(accountStr) as Account;
        observable.next(account);
        observable.complete();
      })
    }
    //missing account info , force fetch all
    return this.apiSrv.get(`${environment.account_url}/users`).map(users => {
      return this.fetchAvatars(users as Account[]);
    })
  }


  private async markAdmins(group: Group) {
    group.members.forEach((member) => {
      member.groupAdmin = !!_.find(group.admins, (a) => a.id === member.id);
    })
  }

  /**
   * Fetch all avatars for given accounts, while at this , put account information into session storage for future retrieval
   * @param accounts
   */
  fetchAvatars(accounts) {
    accounts.forEach(account => {
      if (!account.avatar) {
        this.avatarSrv.getUserAvatarByUsername(account.username).subscribe(avatar => {
          account.avatar = avatar;
          sessionStorage.setItem(account.id, JSON.stringify(account));
        })
      }
    });
    return accounts
  }

  addKid(kid: Account): Observable<Account> {
    return this.apiSrv.post(`${environment.account_url}/kid-add`, kid)
  }

  updateKid(kid: Account): Observable<Account> {
    return this.apiSrv.post(`${environment.account_url}/kid-update`, kid)
  }

  addAdmin(user: Account): Observable<Account> {
    return this.apiSrv.put(`${environment.app_url}/add-admin`, user.id)
  }

  removeAdmin(user: Account): Observable<Account> {
    return this.apiSrv.put(`${environment.app_url}/remove-admin`, user.id)
  }

  createGroup(form: GroupForm): Observable<any> {
    return this.apiSrv.post(`${environment.account_url}/group/create`, form)
  }

  updateGroup(form: GroupForm): Observable<any> {
    return this.apiSrv.put(`${environment.account_url}/group/${form.id}/update`, form)
  }


  leaveGroup(form: GroupForm) {
    return this.apiSrv.put(`${environment.account_url}/group/${form.id}/leave`, form)
  }

  confirmGroupLeave(group: any): Observable<boolean> {
    const data: ConfirmDialogData = {
      title_key: 'user.group.leave.text',
      message_key: 'user.group.leave.confirm',
      action_key: 'user.group.leave.text',
      action_class: 'warn'
    };
    const dialogConfig: MatDialogConfig = {
      disableClose: true,
      panelClass: 'gifts-dialog-modal',
      data: data
    };
    return new Observable((observable) => {
      let dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.leaveGroup({id: group.id}).subscribe(() => {
            observable.next(true);
            observable.complete();
          }, error1 => {
          });
        }
      });
    });
  }

  canEditAll(identification: string) {
    return this.apiSrv.get(`${environment.account_url}/allowed`, {username: identification})
  }

  getUser(identification: string): Observable<Account> {
    return this.apiSrv.get(`${environment.account_url}/get/${identification}`).map(user => {
      return this.fetchAvatars(user as Account);
    })
  }
}
