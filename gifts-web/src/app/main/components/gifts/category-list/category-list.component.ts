import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {UntypedFormControl} from "@angular/forms";
import {MatSelect} from "@angular/material/select";
import {ReplaySubject, Subject} from "rxjs";
import {CategoryOption} from "@model/Category";
import {take, takeUntil} from "rxjs/operators";
import * as _ from "lodash"

@Component({
  selector: 'category-list-filter',
  templateUrl: './category-list.component.html',
  styles: []
})
export class CategoryListComponent implements OnInit, AfterViewInit, AfterViewChecked, OnDestroy {

  categoryControl: UntypedFormControl = new UntypedFormControl();
  categoryFilterControl: UntypedFormControl = new UntypedFormControl();
  @ViewChild('categorySelect',{static:true}) categorySelect: MatSelect;
  filteredCategories: ReplaySubject<CategoryOption[]> = new ReplaySubject<CategoryOption[]>(1);
  filterTerm: string;
  private _onDestroy = new Subject<void>();

  @Input() categories: CategoryOption[] = [];
  @Input() filteredCategory: string;
  @Output() category = new EventEmitter<string>();
  @Output() close = new EventEmitter<boolean>();

  constructor(private ref: ChangeDetectorRef) {
  }

  ngOnInit() {
    this.categoryControl.valueChanges
      .subscribe(value => this.category.emit(value.key));
    this.categoryFilterControl.valueChanges
      .pipe(takeUntil(this._onDestroy))
      .subscribe(value => {
        this._filter(value);
      });
    this.filteredCategories.next(this.categories);
    if (this.filteredCategory) {
      this.categoryControl.setValue(_.find(this.categories, (cat) => cat.key === this.filteredCategory));
    }
  }

  ngAfterViewInit() {
    this.setInitialValue();
    this.categorySelect.focus()
  }

  ngAfterViewChecked(): void {
    this.ref.detectChanges();
  }

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  private setInitialValue() {
    this.filteredCategories
      .pipe(take(1), takeUntil(this._onDestroy))
      .subscribe((val) => {
        this.categorySelect.compareWith = (a: CategoryOption, b: CategoryOption) => b && a.key === b.key
      });
  }


  private _filter(value: string) {
    this.filterTerm = value.toLowerCase();
    this.filteredCategories.next(value ? _.filter(this.categories, cat => (cat.name).toLowerCase().includes(this.filterTerm)) : this.categories);
  }

  clear() {
    this.categoryControl.setValue('');
    this.close.emit(false);
  }

  get CurrentCategory() {
    return this.categoryControl.value
  }
}
