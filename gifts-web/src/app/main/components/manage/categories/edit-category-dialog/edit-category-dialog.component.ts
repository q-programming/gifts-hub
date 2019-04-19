import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {ApiService} from "@core-services/api.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {CategoryDTO} from "@model/AppSettings";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {environment} from "@env/environment";

@Component({
  selector: 'category-edit-dialog',
  templateUrl: './edit-category-dialog.component.html',
  styles: []
})
export class EditCategoryDialogComponent implements OnInit {

  form: FormGroup;
  update: boolean;
  operation: CategoryEditType;
  categoryDTO: CategoryDTO;
  categories: CategoryDTO[];

  constructor(private dialogRef: MatDialogRef<EditCategoryDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private apiSrv: ApiService,
              private formBuilder: FormBuilder) {
    this.operation = data.operation;
    this.form = this.formBuilder.group({
      name: new FormControl('', [Validators.required])
    });
    if (this.operation == CategoryEditType.UPDATE) {
      this.update = true;
      this.categoryDTO = data.categoryDTO;
      this.form.controls['name'].setValue(this.categoryDTO.category.name);
    } else if (this.operation == CategoryEditType.MERGE) {
      this.categories = data.categories;
    }
  }

  ngOnInit() {
    this.form.controls['name'].valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(value => {
      this.apiSrv.get(`${environment.gift_url}/allowed-category`, {category: value}).subscribe(() => {
        this.form.controls['name'].setErrors(undefined);
      }, error => {
        this.form.controls['name'].setErrors({prohibited: true});
      });
    })

  }

  commitAction() {
    const name = this.form.controls['name'].value;
    if (this.operation == CategoryEditType.UPDATE) {
      this.categoryDTO.category.name = name;
      this.dialogRef.close(this.categoryDTO.category);
    } else if (this.operation == CategoryEditType.MERGE) {
      this.dialogRef.close(name);
    }
  }
}

export enum CategoryEditType {
  UPDATE = "app.manage.categories.update", MERGE = "app.manage.categories.merge.text"
}

