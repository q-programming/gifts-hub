import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
    templateUrl: './confirm-dialog.component.html'
})
export class ConfirmDialogComponent {

    title: string;
    message: string;
    action: string;
    action_class: string;

    constructor(private dialogRef: MatDialogRef<ConfirmDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: ConfirmDialog) {
        this.title = data.title_key;
        this.message = data.message_key;
        this.action = data.action_key;
        this.action_class = data.action_class;
    }

    confirm() {
        this.dialogRef.close(true);
    }

}
export class ConfirmDialog {
    title_key: string;
    message_key: string;
    action_key: string;
    action_class: string
}
