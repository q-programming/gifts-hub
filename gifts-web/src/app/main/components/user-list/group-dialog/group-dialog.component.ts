import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {Group, GroupForm} from "../../../../model/Group";
import {FormControl} from "@angular/forms";
import * as _ from "lodash";
import {Account, AccountType} from "../../../../model/Account";
import {AuthenticationService} from "../../../../core/services/authentication.service";
import {UserService} from "../../../services/user.service";
import {AlertService} from "../../../../core/services/alert.service";

@Component({
  selector: 'app-family-dialog',
  templateUrl: './group-dialog.component.html',
  styles: []
})
export class GroupDialogComponent implements OnInit {

  AccountType = AccountType;
  update: boolean;
  group: Group;
  newMemberCtrl: FormControl;
  nameCtrl: FormControl;
  currentAccount: Account;
  identifications: string[] = [];
  admins: string[] = [];
  members: Account[] = [];

  constructor(private dialogRef: MatDialogRef<GroupDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private userSrv: UserService,
              private authSrv: AuthenticationService,
              private alertSrv: AlertService) {
    this.group = data.group;
    this.currentAccount = this.authSrv.currentAccount;
    if (data.group.id) {
      this.update = true;
      //check for admins
      this.group.members.forEach((member) => {
        this.findAndSetAdmins(member);
      }, this);
      this.setMembers()
    }
    this.newMemberCtrl = new FormControl('');
    this.nameCtrl = new FormControl(this.group.name);
  }

  private findAndSetAdmins(member) {
    if (member.groupAdmin) {
      this.admins.push(member.email);
      if (this.currentAccount.id === member.id) {
        this.currentAccount.groupAdmin = true;
      }
    }
  }

  ngOnInit() {
  }

  setMembers() {
    this.members = _.filter(this.group.members, (m) => m.id !== this.currentAccount.id)
  }


  addToInvites() {
    if (this.newMemberCtrl.valid) {
      if (this.newMemberCtrl.value) {
        this.identifications.push(this.newMemberCtrl.value);
      }
      this.newMemberCtrl.setValue('');
    }
  }

  addAdmin(member: Account) {
    member.groupAdmin = true;
    this.group.admins.push(member);
    this.findAndSetAdmins(member);
    this.alertSrv.success("user.group.admin.add.success", {name: member.fullname});
  }

  removeAdmin(member: Account) {
    let removed = _.remove(this.group.admins, m => m.email === member.email);
    _.remove(this.admins, s => s === member.email);
    member.groupAdmin = false;
    if (removed.length > 0) {
      this.alertSrv.success("user.group.admin.remove.success", {name: member.fullname});
    }
  }

  removeMember(member: Account) {
    _.remove(this.group.members, m => m.email === member.email);
    _.remove(this.members, s => s.email === member.email);
    this.removeAdmin(member);
    this.alertSrv.success("user.group.kick.success", {name: member.fullname});
  }

  leaveFamily() {
    this.userSrv.confirmGroupLeave(this.group).subscribe(result => {
      if (result) {
        this.alertSrv.success('user.group.left');
        this.dialogRef.close({removed: true});
      }
    });
  }

  /**
   * Commit any group creation or update
   */
  commit() {
    const form = new GroupForm();
    form.id = this.group.id;
    form.members = this.identifications.concat(_.map(this.group.members, (m) => m.username));
    form.admins = this.admins;
    form.name = this.nameCtrl.value;
    this.dialogRef.close(form);
  }

  cancel() {
    this.dialogRef.close({canceled: true})
  }

}
