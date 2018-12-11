import {Component, OnInit, ViewChild} from '@angular/core';
import {UserService} from "@services/user.service";
import {SortBy} from "@model/Settings";
import {MatButtonToggleGroup} from "@angular/material";
import {NGXLogger} from "ngx-logger";
import {Family} from "@model/Family";
import {Account} from "@model/Account";

@Component({
  selector: 'user-list',
  templateUrl: './user-list.component.html',
  styles: []
})
export class UserListComponent implements OnInit {
  SortBy = SortBy;
  @ViewChild("sortBy") sortBy: MatButtonToggleGroup;
  family: Family;
  families: Family[] = [];
  withoutFamily: Account[] = [];
  usersByName: Account[] = [];

  constructor(private userSrv: UserService, private logger: NGXLogger) {
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

  trackByFn(index, item) {
    return item.id;
  }
}
