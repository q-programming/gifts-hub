import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AlertService} from "@core-services/alert.service";
import {ApiService} from "@core-services/api.service";
import {AppSettings, CategoryDTO} from "@model/AppSettings";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {environment} from "@env/environment";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {CategoryEditType, EditCategoryDialogComponent} from "./edit-category-dialog/edit-category-dialog.component";
import {
  ConfirmDialogComponent,
  ConfirmDialogData
} from "../../../../components/dialogs/confirm/confirm-dialog.component";
import * as _ from 'lodash';

@Component({
  selector: 'manage-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['categories.component.css']
})
export class CategoriesComponent implements OnInit {

  @Input() settings: AppSettings;
  @Output() commit: EventEmitter<boolean> = new EventEmitter();
  merge: boolean;

  constructor(private alertSrv: AlertService, private apiSrv: ApiService, public dialog: MatDialog,) {
  }

  ngOnInit() {
  }

  onDrop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.settings.categories, event.previousIndex, event.currentIndex);
  }


  saveConfiguration() {
    this.apiSrv.put(`${environment.app_url}/settings`, this.settings).subscribe(() => {
      this.alertSrv.success('app.manage.saved');
      this.commit.emit(true);
    }, error => {
      this.alertSrv.error('app.manage.error');
    })
  }

  openUpdateCategory(categoryDTO: CategoryDTO) {
    const dialogRef = this.dialog.open(EditCategoryDialogComponent, {
      minWidth: '400px',
      panelClass: 'gifts-modal-normal',
      data: {
        categoryDTO: categoryDTO,
        operation: CategoryEditType.UPDATE
      }
    });
    dialogRef.afterClosed().subscribe(category => {
      if (category) {
        this.apiSrv.put(`${environment.app_url}/update-category`, category).subscribe(() => {
          this.alertSrv.success("app.manage.categories.updated");
          this.commit.emit(true);
        }, error => {
          this.alertSrv.error('app.manage.error');
        })
      }
    })

  }

  confirmDelete(categoryDTO: CategoryDTO) {
    const data: ConfirmDialogData = {
      title_key: 'app.manage.categories.remove.text',
      message_key: 'app.manage.categories.remove.confirm',
      action_key: 'app.general.delete',
      action_class: 'primary'
    };
    const dialogConfig: MatDialogConfig = {
      disableClose: true,
      panelClass: 'gifts-dialog-modal',
      data: data
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.apiSrv.delete(`${environment.app_url}/remove-category`, categoryDTO.category).subscribe(() => {
          this.alertSrv.success("app.manage.categories.remove.removed");
          this.commit.emit(true);
        }, error => {
          this.alertSrv.error('app.manage.error');
        });
      }
    })
  }

  mergeClicked() {
    if (this.merge) {
      const selected = _.filter(this.settings.categories, (c) => c.selected).map((c) => c.category);
      if (selected.length > 0) {
        this.processMerge(selected);
      }
      this.merge = false
    } else {
      this.merge = true
    }
  }

  private processMerge(selected) {
    const dialogRef = this.dialog.open(EditCategoryDialogComponent, {
      panelClass: 'gifts-modal-normal',
      data: {
        categories: selected,
        operation: CategoryEditType.MERGE
      }
    });
    dialogRef.afterClosed().subscribe(value => {
      if (value) {
        this.apiSrv.put(`${environment.app_url}/merge-categories`, {
            name: value,
            categories: selected
          }
        ).subscribe(() => {
          this.alertSrv.success("app.manage.categories.merge.merged");
          this.commit.emit(true);
        }, error1 => {
          this.alertSrv.error('app.manage.error');
        });
      } else {
        selected.forEach((category) => {
          category.selected = false;
        })
      }
    })
  }
}
