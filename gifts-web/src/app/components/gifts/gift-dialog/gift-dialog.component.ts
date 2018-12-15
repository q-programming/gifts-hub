import {AfterViewInit, Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef, MatSelect} from "@angular/material";
import {Gift} from "@model/Gift";
import {ApiService} from "@services/api.service";
import {environment} from "@env/environment";
import {SearchEngine} from "@model/SearchEngine";
import {Category} from "@model/Category";
import {Observable, ReplaySubject, Subject} from "rxjs";
import {map, startWith, take, takeUntil} from "rxjs/operators";
import * as _ from "lodash";

@Component({
  selector: 'app-gift-dialog',
  templateUrl: './gift-dialog.component.html',
  styles: []
})
export class GiftDialogComponent implements OnInit {
  gift: Gift;
  form: FormGroup;
  update: boolean;
  searchEngines: SearchEngine[] = [];//TODO needed?
  categories: Category[];
  links: string[] = [];

  // categories
  filterTerm: string;
  filteredCategories: Observable<Category[]>;

  constructor(private dialogRef: MatDialogRef<GiftDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private apiSrv: ApiService,
              private formBuilder: FormBuilder) {
    this.gift = data.gift;
    if (data.gift.id) {
      this.update = true;
    }
    this.form = this.formBuilder.group({
      name: new FormControl(this.gift.name, [Validators.required]),
      description: new FormControl(this.gift.description, [Validators.required]),
      category: new FormControl(this.gift.category),
      hidden: new FormControl(this.gift.hidden),
      links: this.formBuilder.array([
        this.initLink(),
      ])
    });
    this.getSearchEngines();
    this.getCategories();
  }

  ngOnInit() {
  }

  getSearchEngines() {
    this.apiSrv.get(`${environment.app_url}/search-engines`).subscribe(engines => {
      this.searchEngines = engines;
      let that = this;
      this.searchEngines.forEach((engine) => {
        that.form.addControl(engine.name, new FormControl(true))
      })

    })
  }

  initLink() {
    return this.formBuilder.group({
      link: ['']
    });
  }

  addLink() {
    const control = <FormArray>this.form.controls['links'];
    control.push(this.initLink());
  }

  removeLink(i: number) {
    const control = <FormArray>this.form.controls['links'];
    control.removeAt(i);
  }

  categoryDisplay(category?: Category): string | undefined {
    return category ? category.name : undefined;
  }

  private getCategories() {
    this.apiSrv.get(`${environment.gift_url}/categories`).subscribe(categories => {
      this.categories = categories;
      this.filteredCategories = this.form.controls.category
        .valueChanges
        .pipe(
          startWith<string | Category>(''),
          map(value => {
            return typeof value === 'string' ? value : value.name
          }),
          map(name => {
            return name ? this._filter(name) : this.categories
          })
        );

    })
  }

  resetCategory() {
    this.filterTerm = '';
    this.form.controls.category.setValue('');
  }

  private _filter(value: string) {
    this.filterTerm = value.toLowerCase();
    return value ? _.filter(this.categories, category => (category.name).toLowerCase().includes(this.filterTerm)) : this.categories;
  }
}
