import {Component, OnInit} from '@angular/core';
import {AlertService} from "@services/alert.service";
import {UserService} from "@services/user.service";
import {Account} from "@model/Account";
import {isAdmin} from "../../../utils/utils";

@Component({
  selector: 'manage-admins',
  templateUrl: './admins.component.html',
  styles: []
})
export class AdminsComponent implements OnInit {
  users: Account[];
  admins: number = 0;

  constructor(private alertSrv: AlertService, private userSrv: UserService) {
  }

  ngOnInit() {
    this.userSrv.getAllUsers(true).subscribe(users => {
      users.forEach(user => {
        user.admin = isAdmin(user);
        if (user.admin) {
          this.admins++;
        }
      });
      this.users = users;
    })
  }

  addAdmin(user: Account) {
    this.userSrv.addAdmin(user).subscribe(() => {
      user.admin = true;
      this.admins++;
      this.alertSrv.success('app.manage.admin.added');
    });
  }

  removeAdmin(user: Account) {
    this.userSrv.removeAdmin(user).subscribe(() => {
      user.admin = false;
      this.admins--;
      this.alertSrv.success('app.manage.admin.removed');
    }, error => {
      this.alertSrv.error('error.lastAdmin');

    });
  }
}
