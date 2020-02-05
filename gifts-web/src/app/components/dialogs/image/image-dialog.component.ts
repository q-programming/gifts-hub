import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GiftImage} from "@model/GiftImage";

@Component({
  templateUrl: './image-dialog.component.html',
  styleUrls: ['./image-dialog.component.css']
})
export class ImageDialogComponent {

  image: string;

  constructor(private dialogRef: MatDialogRef<ImageDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: GiftImage) {
    if (data) {
      const dataType = "data:" + data.type + ";base64,";
      this.image = dataType + data.image;
    }
  }

  confirm() {
    this.dialogRef.close(true);
  }

}
