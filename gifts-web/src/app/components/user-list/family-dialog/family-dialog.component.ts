import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {Family, FamilyForm} from "@model/Family";
import {FormControl, Validators} from "@angular/forms";
import * as _ from "lodash";
import {Account, AccountType} from "@model/Account";
import {AuthenticationService} from "@services/authentication.service";
import {UserService} from "@services/user.service";
import {AlertService} from "@services/alert.service";
import {ConfirmDialog, ConfirmDialogComponent} from "../../dialogs/confirm/confirm-dialog.component";

@Component({
  selector: 'app-family-dialog',
  templateUrl: './family-dialog.component.html',
  styles: []
})
export class FamilyDialogComponent implements OnInit {

  AccountType = AccountType;
  update: boolean;
  family: Family;
  newMemberCtrl: FormControl;
  nameCtrl: FormControl;
  currentAccount: Account;
  identifications: string[] = [];
  admins: string[] = [];

  constructor(private dialogRef: MatDialogRef<FamilyDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              public dialog: MatDialog,
              private userSrv: UserService,
              private authSrv: AuthenticationService,
              private alertSrv: AlertService) {
    this.family = data.family;
    this.currentAccount = this.authSrv.currentAccount;
    if (data.family.id) {
      this.update = true;
      //check for admins
      this.family.members.forEach((member) => {
        this.findAndSetAdmins(member);
      }, this);
    }
    this.newMemberCtrl = new FormControl('');
    this.nameCtrl = new FormControl(this.family.name);
  }

  private findAndSetAdmins(member) {
    member.familyAdmin = !!_.find(this.family.admins, (a) => a.id === member.id);
    if (member.familyAdmin) {
      this.admins.push(member.email);
      if (this.currentAccount.id === member.id) {
        this.currentAccount.familyAdmin = true;
      }
    }
  }

  ngOnInit() {
  }

  get Members(): Account[] {
    return _.filter(this.family.members, (m) => m.id !== this.currentAccount.id)
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
    this.family.admins.push(member);
    this.findAndSetAdmins(member);
  }

  removeAdmin(member: Account) {
    _.remove(this.family.admins, m => m === member.email);
    _.remove(this.admins, s => s === member.email);
    member.familyAdmin = false;
  }

  leaveFamily() {
    const data: ConfirmDialog = {
      title_key: 'user.family.leave.text',
      message_key: 'user.family.leave.confirm',
      action_key: 'user.family.leave.text',
      action_class: 'warn'
    };
    const dialogConfig: MatDialogConfig = {
      disableClose: true,
      panelClass: 'gifts-dialog-modal',
      data: data
    };
    let dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userSrv.leaveFamily().subscribe(() => {
          this.alertSrv.success('user.family.left');
          this.dialogRef.close({removed: true});
        }, error1 => {

        });
      }
    });
  }

  /**
   * Commit any family creation or update
   */
  commit() {
    const form = new FamilyForm();
    form.members = this.identifications.concat(_.map(this.family.members, (m) => m.username));
    form.admins = this.admins;
    form.name = this.nameCtrl.value;
    this.dialogRef.close(form);
  }
}
