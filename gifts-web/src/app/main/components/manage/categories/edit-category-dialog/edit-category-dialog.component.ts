import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {ApiService} from "@core-services/api.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {CategoryDTO} from "@model/AppSettings";

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
    if (this.operation == CategoryEditType.UPDATE) {
      this.update = true;
      this.categoryDTO = data.categoryDTO;
      this.form = this.formBuilder.group({
        name: new FormControl(this.categoryDTO.category.name, [Validators.required])
      });
    } else if (this.operation == CategoryEditType.MERGE) {
      this.categories = data.categories;
    }
  }

  ngOnInit() {
  }

  commitAction() {
    if (this.operation == CategoryEditType.UPDATE) {
      this.categoryDTO.category.name = this.form.controls['name'].value
    } else if (this.operation == CategoryEditType.MERGE) {

    }
    this.dialogRef.close(this.categoryDTO.category);
  }
}

export enum CategoryEditType {
  UPDATE = "app.manage.categories.update", MERGE = "app.manage.categories.merge"
}

