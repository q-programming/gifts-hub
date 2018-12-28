import {Injectable} from '@angular/core';
import {ApiService} from "@services/api.service";
import {Group, GroupForm} from "@model/Group";
import {Account} from "@model/Account";
import {Observable} from "rxjs";
import {environment} from "@env/environment.prod";
import {AvatarService} from "@services/avatar.service";
import {SortBy} from "@model/Settings";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {ConfirmDialog, ConfirmDialogComponent} from "../components/dialogs/confirm/confirm-dialog.component";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiSrv: ApiService, private avatarSrv: AvatarService, public dialog: MatDialog,) {
  }

  getGroup(identification?: string): Observable<Group> {
    return this.apiSrv.get(`${environment.account_url}/group`, {username: identification}).map(family => {
      if (family) {
        this.fetchAvatars(<Group>family.members);
        this.fetchAvatars(<Group>family.admins);
      }
      return <Group>family;
    })
  }

  isGroupAdmin(identification: string): Observable<boolean> {
    return this.apiSrv.get(`${environment.account_url}/group/isAdmin`, {username: identification}).map(result => {
      return result
    })
  }


  getRelatedUsers(identification?: string, gifts?: boolean): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/userList`, {
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
        this.fetchAvatars(group.members)

      });
      return groups;
    })
  }

  getAllUsers(users?: boolean): Observable<Account[]> {
    return this.apiSrv.get(`${environment.account_url}/users`, {users: users}).map(users => {
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
    return this.apiSrv.post(`${environment.account_url}/group-create`, form)
  }

  updateGroup(form: GroupForm): Observable<any> {
    return this.apiSrv.put(`${environment.account_url}/group-update`, form)
  }


  leaveGroup(form: GroupForm) {
    return this.apiSrv.put(`${environment.account_url}/group-leave`, form)
  }

  confirmGroupLeave(group: any): Observable<boolean> {
    const data: ConfirmDialog = {
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
          }, error1 => {
          });
        }
      });
    });
  }
}
