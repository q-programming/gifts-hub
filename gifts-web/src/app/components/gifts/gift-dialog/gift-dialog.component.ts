import {Component, Inject, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {Gift} from "@model/Gift";
import {ApiService} from "@services/api.service";
import {environment} from "@env/environment";
import {SearchEngine} from "@model/SearchEngine";
import {Category} from "@model/Category";
import {Observable} from "rxjs";
import {map, startWith} from "rxjs/operators";
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
  familyUser: boolean;

  // categories
  filterTerm: string;
  filteredCategories: Observable<Category[]>;

  constructor(private dialogRef: MatDialogRef<GiftDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private apiSrv: ApiService,
              private formBuilder: FormBuilder) {
    this.gift = data.gift;
    this.familyUser = data.familyUser;
    if (data.gift.id) {
      this.update = true;
    }
    this.form = this.formBuilder.group({
      name: new FormControl(this.gift.name, [Validators.required]),
      description: new FormControl(this.gift.description),
      category: new FormControl(this.gift.category),
      hidden: new FormControl(this.gift.hidden),
      engines: this.formBuilder.array([]),
      links: this.formBuilder.array([])
    });
    if (this.gift.links.length > 0) {
      this.gift.links.forEach(link => {
        this.addLink(link);
      });
    } else {
      this.addLink();
    }
    this.getSearchEngines();
    this.getCategories();
  }

  ngOnInit() {
  }

  // SEARCH ENGINES
  getSearchEngines() {
    this.apiSrv.get(`${environment.app_url}/search-engines`).subscribe(engines => {
      this.searchEngines = engines;
      if (this.searchEngines.length > 0) {
      }
      let that = this;
      this.searchEngines.forEach((engine) => {
        const enginesForm = <FormArray>that.form.controls['engines'];
        enginesForm.push(this.addEngine(engine));
        that.form.addControl(engine.name, new FormControl(true))
      })

    })
  }

  addEngine(engine: SearchEngine) {
    const g_engine = _.find(this.gift.engines, g_eng => g_eng.id === engine.id);
    return this.formBuilder.group({
      name: [engine.name],
      id: [engine.id],
      icon: [engine.icon],
      selected: [g_engine !== undefined || !this.update]
    });
  }

  get EnginesControls() {
    return (this.form.get('engines') as FormArray).controls
  }

  // LINKS
  addLink(link?: string) {
    const control = <FormArray>this.form.controls['links'];
    control.push(this.formBuilder.group(
      {
        link: [link ? link : '']
      }
    ));
  }

  removeLink(i: number) {
    const control = <FormArray>this.form.controls['links'];
    control.removeAt(i);
  }

  get LinksControls() {
    return (this.form.get('links') as FormArray).controls
  }

// CATEGORIES
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


  // COMMIT
  commitGift() {
    if (this.form.valid) {
      this.gift.name = this.form.get('name').value;
      this.gift.description = this.form.get('description').value;
      this.gift.description = this.form.get('description').value;
      let categoryValue = this.form.get('category').value;
      this.gift.category = typeof categoryValue === 'string' ? {name: categoryValue} : categoryValue;
      this.gift.hidden = this.form.get('hidden').value;
      this.gift.links = _.map((this.form.get('links') as FormArray).controls, (linkCtr) => linkCtr.value['link']);
      const engines = <FormArray>this.form.get('engines');
      const enabledEngines = _.filter(engines.controls, ctrl => ctrl.get('selected').value);
      this.gift.engines = _.map(enabledEngines, eng => {
        return {id: eng.get('id').value}
      });
      this.dialogRef.close(this.gift)
    }
  }
}
