import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormControl} from "@angular/forms";
import {Account} from "@model/Account";
import {MatSelect} from "@angular/material";
import {ReplaySubject, Subject} from "rxjs";
import {AuthenticationService} from "@services/authentication.service";
import {UserService} from "@services/user.service";
import {Router} from "@angular/router";
import * as _ from "lodash";
import {take, takeUntil} from "rxjs/operators";

@Component({
  selector: 'account-list',
  templateUrl: './account-list.component.html',
  styles: []
})
export class AccountListComponent implements OnInit, AfterViewInit, OnDestroy {

  currentAccount: Account;
  viewedAccount: Account;
  @Input() identification: string;
  usersControl: FormControl = new FormControl();
  usersFilterControl: FormControl = new FormControl();
  accounts: Account[];
  @ViewChild('accountSelect') accountSelect: MatSelect;
  filteredAccounts: ReplaySubject<Account[]> = new ReplaySubject<Account[]>(1);
  filterTerm: string;
  private _onDestroy = new Subject<void>();


  constructor(private authSrv: AuthenticationService,
              private userSrv: UserService,
              private router: Router) {
  }

  ngOnInit() {
    this.currentAccount = this.authSrv.currentAccount;
    this.getUsers();
    this.usersControl
      .valueChanges.subscribe(value => {
      if (value && value !== this.viewedAccount) {
        this.router.navigate(['/list', value.username])
      }
    });
    this.usersFilterControl.valueChanges
      .pipe(takeUntil(this._onDestroy))
      .subscribe(value => {
        this._filter(value);
      });

  }

  ngAfterViewInit() {
    this.setInitialValue();
  }

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  private getUsers() {
    this.userSrv.getUsers(this.identification)
      .subscribe(accounts => {
        if (this.identification) {
          this.viewedAccount = _.find(accounts, (account) => account.username === this.identification);
        } else {
          this.viewedAccount = _.find(accounts, (account) => account.username === this.currentAccount.username);
        }
        this.accounts = accounts;
        this.filteredAccounts.next(accounts);
        this.usersControl.setValue(this.viewedAccount);
      })
  }

  private _filter(value: string) {
    this.filterTerm = value.toLowerCase();
    this.filteredAccounts.next(value ? _.filter(this.accounts, account => (account.fullname + account.username).toLowerCase().includes(this.filterTerm)) : this.accounts);
  }

  private setInitialValue() {
    this.filteredAccounts
      .pipe(take(1), takeUntil(this._onDestroy))
      .subscribe(() => {
        this.accountSelect.compareWith = (a: Account, b: Account) => a.id === b.id;
      });
  }


}
