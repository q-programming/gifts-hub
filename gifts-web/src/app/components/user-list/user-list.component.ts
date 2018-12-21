import {Component, OnInit, ViewChild} from '@angular/core';
import {UserService} from "@services/user.service";
import {SortBy} from "@model/Settings";
import {MatButtonToggleGroup, MatDialog} from "@angular/material";
import {NGXLogger} from "ngx-logger";
import {Family} from "@model/Family";
import {Account} from "@model/Account";
import {KidDialogComponent} from "./kid-dialog/kid-dialog.component";
import {AlertService} from "@services/alert.service";
import {AvatarService} from "@services/avatar.service";
import {FamilyDialogComponent} from "./family-dialog/family-dialog.component";

@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['user-list.component.css']
})
export class UserListComponent implements OnInit {
  SortBy = SortBy;
  @ViewChild("sortBy") sortBy: MatButtonToggleGroup;
  family: Family;
  families: Family[] = [];
  withoutFamily: Account[] = [];
  usersByName: Account[] = [];

  constructor(private userSrv: UserService,
              private alertSrv: AlertService,
              private avatarSrv: AvatarService,
              private logger: NGXLogger,
              public dialog: MatDialog) {
  }

  ngOnInit() {
    this.userSrv.getDefaultSorting().subscribe(sorting => {
      this.sortBy.value = sorting;
      this.getUsers();
      this.getFamily();
    });
    this.sortBy.valueChange
      .debounceTime(1) //small delay not to trigger multiple times
      .subscribe(() => {
        this.getUsers();
      })
  }

  getUsers(reload?: boolean) {
    if (reload) {
      this.families = [];
      this.withoutFamily = [];
      this.usersByName = [];
    }
    if (this.sortBy.value == SortBy.FAMILY) {
      this.sortByFamilies()
    } else {
      this.sortByName()
    }
  }

  sortByFamilies() {
    if (this.families.length == 0 && this.withoutFamily.length == 0) {
      this.userSrv.getAllFamilies().subscribe(families => this.families = families);
      this.userSrv.getUsersWithoutFamily().subscribe(users => this.withoutFamily = users)
    }
  }

  sortByName() {
    if (this.usersByName.length == 0) {
      this.userSrv.getAllUsers().subscribe(users => this.usersByName = users);
    }
  }

  getFamily() {
    this.userSrv.getFamily().subscribe(family => this.family = family)
  }

  isUserFamily(family: Family): boolean {
    return this.family && this.family.id === family.id
  }

  createFamilyDailog() {
    const dialogRef = this.dialog.open(FamilyDialogComponent, {
      panelClass: 'gifts-modal-normal',
      autoFocus: true,
      disableClose: true,
      width: '600px',
      data: {
        family: new Family()
      }
    });
    dialogRef.afterClosed().subscribe((form) => {
      if (form) {
        this.userSrv.createFamily(form).subscribe(reply => {
          if (reply.result === 'invites') {
            this.alertSrv.success('user.family.create.success.invites')
          } else {
            this.alertSrv.success('user.family.create.success.text')
          }
          this.getUsers(true);
          this.getFamily();
        }, error => {
          this.logger.error(error);
          this.alertSrv.error('user.family.create.error');
        })
      }
    });
  }


  editFamilyDailog() {
    const dialogRef = this.dialog.open(FamilyDialogComponent, {
      panelClass: 'gifts-modal-normal',
      disableClose: true,
      autoFocus: true,
      width: '600px',
      data: {
        family: this.family
      }
    });
    dialogRef.afterClosed().subscribe((form) => {
      if (form) {
        if (!form.removed) {
          this.userSrv.updateFamily(form).subscribe(result => {
            if (result === 'invites') {
              this.alertSrv.success('user.family.edit.success.invites')
            } else {
              this.alertSrv.success('user.family.edit.success.text')
            }
          }, error => {
            this.logger.error(error);
            this.alertSrv.error('user.family.edit.error');
          })
        }
        this.getUsers(true);
        this.getFamily();
      }
    });

  }

  addKidDialog() {
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
            this.alertSrv.success('user.family.add.kid.success')
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
            this.alertSrv.success('user.family.edit.kid.success')
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
      case 'family':
        this.alertSrv.error('user.register.username.exists');
        break;
      case 'family_admin':
        this.alertSrv.error('user.family.admin.error');
        break;
      case 'username':
        this.alertSrv.error('user.family.admin.error');
        break;
    }
  }

  trackByFn(index, item) {
    return item.id;
  }

}
