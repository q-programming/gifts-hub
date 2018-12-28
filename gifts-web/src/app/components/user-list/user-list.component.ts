import {Component, OnInit, ViewChild} from '@angular/core';
import {UserService} from "@services/user.service";
import {SortBy} from "@model/Settings";
import {MatButtonToggleGroup, MatDialog, MatMenuTrigger} from "@angular/material";
import {NGXLogger} from "ngx-logger";
import {Group} from "@model/Group";
import {Account} from "@model/Account";
import {KidDialogComponent} from "./kid-dialog/kid-dialog.component";
import {AlertService} from "@services/alert.service";
import {AvatarService} from "@services/avatar.service";
import {GroupDialogComponent} from "./group-dialog/group-dialog.component";
import {AuthenticationService} from "@services/authentication.service";
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
      // this.getUsers();
      // this.getGroup();
    });
    this.sortBy.valueChange
      .debounceTime(1) //small delay not to trigger multiple times
      .subscribe(() => {
        this.getUsers();
      })
  }

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

  sortByGroups() {
    if (this.groups.length == 0 && this.withoutFamily.length == 0) {
      this.userSrv.getAllGroups().subscribe(groups => this.groups = groups);
    }
  }

  sortByName() {
    if (this.usersByName.length == 0) {
      this.userSrv.getRelatedUsers(undefined, true).subscribe(users => this.usersByName = users);
    }
  }

  getGroup() {
    this.userSrv.getGroup().subscribe(group => this.group = group)
  }


  isUserFamily(family: Group): boolean {
    return this.group && this.group.id === family.id
  }

  createGroupDailog() {
    const dialogRef = this.dialog.open(GroupDialogComponent, {
      panelClass: 'gifts-modal-normal',
      autoFocus: true,
      disableClose: true,
      width: '600px',
      data: {
        group: new Group()
      }
    });
    dialogRef.afterClosed().subscribe((form) => {
      if (form) {
        this.userSrv.createGroup(form).subscribe(reply => {
          if (reply.result === 'invites') {
            this.alertSrv.success('user.group.create.success.invites')
          } else {
            this.alertSrv.success('user.group.create.success.text')
          }
          this.getUsers(true);
          this.getGroup();
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
   * @param event event to stop propagation ( so that mat-expansion-panel won't collapse )
   */
  editGroupDailog(group: Group) {
    this.trigger.closeMenu();
    const dialogRef = this.dialog.open(GroupDialogComponent, {
      panelClass: 'gifts-modal-normal',
      disableClose: true,
      autoFocus: true,
      width: '600px',
      data: {
        group: group
      }
    });
    dialogRef.afterClosed().subscribe((form) => {
      if (form) {
        if (!form.removed) {
          this.userSrv.updateGroup(form).subscribe(resp => {
            if (resp.result === 'invites') {
              this.alertSrv.success('user.group.edit.success.invites')
            } else {
              this.alertSrv.success('user.group.edit.success.text')
            }
          }, error => {
            this.logger.error(error);
            this.alertSrv.error('user.group.edit.error');
          })
        }
        this.getUsers(true);
      }
    });
  }

  /**
   * Shows confirmation dialog and leaves group if confirmed
   * @param group group which current user would like to leave
   * @param event event to stop propagation ( so that mat-expansion-panel won't collapse )
   */
  leaveFamily(group) {
    this.trigger.closeMenu();
    event.stopPropagation();
    this.userSrv.confirmGroupLeave(group).subscribe(result => {
      if (result) {
        this.getUsers(true);
        this.alertSrv.success('user.group.left');
      }
    });
  }

  menuClick(event: Event){
    event.stopPropagation();
  }

  /**
   * Shows add kid dialog to add kid to group
   * @param group group to which kid will be added
   * @param event event to stop propagation ( so that mat-expansion-panel won't collapse )
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
      case 'family_admin':
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

  isGroupAdmin(group: Group):boolean {
    return !!_.find(group.admins, (a) => a.id == this.currentAccount.id);

  }
}
