import {Component, OnInit, ViewChild} from '@angular/core';
import {UserService} from "@services/user.service";
import {MatButtonToggleGroup, MatDialog, MatMenuTrigger} from "@angular/material";
import {NGXLogger} from "ngx-logger";
import {Group} from "@model/Group";
import {Account} from "@model/Account";
import {SortBy} from "@model/Settings";
import {AlertService} from "@core-services/alert.service";
import {AvatarService} from "@core-services/avatar.service";
import {AuthenticationService} from "@core-services/authentication.service";
import {KidDialogComponent} from "./kid-dialog/kid-dialog.component";
import {GroupDialogComponent} from "./group-dialog/group-dialog.component";
import * as _ from "lodash";

@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['user-list.component.css']
})
export class UserListComponent implements OnInit {
  SortBy = SortBy;
  @ViewChild("sortBy") sortBy: MatButtonToggleGroup;
  @ViewChild(MatMenuTrigger) trigger: MatMenuTrigger;
  group: Group;
  groups: Group[] = [];
  withoutFamily: Account[] = [];
  usersByName: Account[] = [];
  private currentAccount: Account;
  loaded: boolean;

  constructor(private userSrv: UserService,
              private alertSrv: AlertService,
              private avatarSrv: AvatarService,
              private authSrv: AuthenticationService,
              private logger: NGXLogger,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
    this.userSrv.getDefaultSorting().subscribe(sorting => {
      this.sortBy.value = sorting;
    });
    this.sortBy.valueChange
      .debounceTime(1) //small delay not to trigger multiple times
      .subscribe(() => {
        this.getUsers();
      })
  }

  /**
   * Get all users that might be part of same group as currently logged in user
   * @param reload if passed true, cached values will be reload
   */
  getUsers(reload?: boolean) {
    if (reload) {
      this.groups = [];
      this.withoutFamily = [];
      this.usersByName = [];
    }
    if (this.sortBy.value == SortBy.GROUP) {
      this.sortByGroups()
    } else {
      this.sortByName()
    }
  }

  /**
   * Fetch all accounts grouped per Groups
   */
  sortByGroups() {
    if (this.groups.length == 0 && this.withoutFamily.length == 0) {
      this.loaded = false;
      this.userSrv.getAllGroups().subscribe(groups => {
        this.groups = groups;
        this.loaded = true;
      });
    }
  }

  /**
   * Fetch all accounts sorted by names
   */
  sortByName() {
    if (this.usersByName.length == 0) {
      this.loaded = false;
      this.userSrv.getRelatedUsers(undefined, true).subscribe(users => {
        this.usersByName = users;
        this.loaded = true;
      });
    }
  }

  /**
   * Show create group dialog
   */
  createGroupDailog() {
    const dialogRef = this.dialog.open(GroupDialogComponent, {
      panelClass: 'gifts-modal-normal',
      autoFocus: true,
      disableClose: true,
      width: '700px',
      data: {
        group: new Group()
      }
    });
    dialogRef.afterClosed().subscribe((form) => {
      if (form && !form.canceled) {
        this.userSrv.createGroup(form).subscribe(reply => {
          if (reply.result === 'invites') {
            this.alertSrv.success('user.group.create.success.invites')
          } else {
            this.alertSrv.success('user.group.create.success.text')
          }
          this.getUsers(true);
        }, error => {
          this.logger.error(error);
          this.alertSrv.error('user.group.create.error');
        })
      }
    });
  }

  /**
   * Shows edit group dialog
   * @param group group to be edited
   */
  editGroupDialog(group: Group) {
    this.trigger.closeMenu();
    const dialogRef = this.dialog.open(GroupDialogComponent, {
      panelClass: 'gifts-modal-normal',
      disableClose: true,
      autoFocus: true,
      width: '700px',
      data: {
        group: group
      }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        if (result.canceled) {
          this.getUsers(true);
        } else if (!result.removed) {
          this.userSrv.updateGroup(result).subscribe(resp => {
            if (resp.result === 'invites') {
              this.alertSrv.success('user.group.edit.success.invites')
            } else {
              this.alertSrv.success('user.group.edit.success.text')
            }
            this.getUsers(true);
          }, error => {
            this.logger.error(error);
            this.alertSrv.error('user.group.edit.error');
          })
        } else {
          this.getUsers(true);
        }
      }
    });
  }

  /**
   * Shows confirmation dialog and leaves group if confirmed
   * @param group group which current user would like to leave
   */
  leaveFamily(group) {
    this.trigger.closeMenu();
    this.userSrv.confirmGroupLeave(group).subscribe(result => {
      if (result) {
        this.getUsers(true);
        this.alertSrv.success('user.group.left');
      }
    });
  }

  /**
   * Stop propagation of any next event that happens after action
   * @param event event to stop propagation ( so that mat-expansion-panel won't collapse )
   */
  menuClick(event: Event) {
    event.stopPropagation();
  }

  /**
   * Shows add kid dialog to add kid to group
   * @param group group to which kid will be added
   */
  addKidDialog(group: Group) {
    this.trigger.closeMenu();
    let kid = new Account();
    const dialogRef = this.dialog.open(KidDialogComponent, {
      panelClass: 'gifts-modal-normal',
      data: {
        account: kid
      }
    });
    dialogRef.afterClosed().subscribe((kid) => {
      if (kid) {
        kid.groupId = group.id;
        this.userSrv.addKid(kid).subscribe(newKid => {
          if (newKid) {
            this.getUsers(true);
            this.alertSrv.success('user.group.add.kid.success')
          }
        }, error => {
          this.switchErrors(error);
        })
      }
    });
  }

  /**
   * Show edit kid dialog
   * @param kid kid account that will be modified
   */
  editKidDialog(kid: Account) {
    const dialogRef = this.dialog.open(KidDialogComponent, {
      panelClass: 'gifts-modal-normal', //TODO class needed
      data: {
        account: kid
      }
    });
    dialogRef.afterClosed().subscribe((kid) => {
      if (kid) {
        this.userSrv.updateKid(kid).subscribe(newKid => {
          if (newKid) {
            this.avatarSrv.reloadAvatar(kid);
            this.getUsers(true);
            this.alertSrv.success('user.group.edit.kid.success')
          }
        }, error => {
          this.switchErrors(error);
        })
      }
    });
  }

  private switchErrors(error) {
    this.logger.error(error);
    switch (error.error) {
      case 'group':
        this.alertSrv.error('user.register.username.exists');
        break;
      case 'group_admin':
        this.alertSrv.error('user.group.admin.error');
        break;
      case 'username':
        this.alertSrv.error('user.group.admin.error');
        break;
    }
  }

  trackByFn(index, item) {
    return item.id;
  }

  /**
   * Check whenever current account is admin of group
   * @param group group for which admin will be evaulated
   */
  isGroupAdmin(group: Group): boolean {
    return !!_.find(group.admins, (a) => a.id == this.currentAccount.id);

  }
}
