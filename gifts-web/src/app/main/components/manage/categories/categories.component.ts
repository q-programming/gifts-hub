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

@Component({
  selector: 'manage-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['categories.component.css']
})
export class CategoriesComponent implements OnInit {

  @Input() settings: AppSettings;
  @Output() commit: EventEmitter<boolean> = new EventEmitter();

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

    const dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig)
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
}
