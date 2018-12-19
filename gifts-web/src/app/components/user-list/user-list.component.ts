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

  getUsers() {
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

  addKidDialog() {
    let kid = new Account();
    const dialogRef = this.dialog.open(KidDialogComponent, {
      panelClass: 'gifts-modal-normal', //TODO class needed
      data: {
        account: kid
      }
    });
    dialogRef.afterClosed().subscribe((kid) => {
      if (kid) {
        this.userSrv.addKid(kid).subscribe(newKid => {
          if (newKid) {
            this.getUsers();
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
