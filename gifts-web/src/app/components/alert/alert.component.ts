import {Component, OnInit} from '@angular/core';
import {AlertService} from "@services/alert.service";
import {animate, keyframes, query, stagger, style, transition, trigger} from '@angular/animations';
import {Message, MessageType} from "@model/Message";

@Component({
    selector: 'alert',
    templateUrl: './alert.component.html',
    styleUrls: ['./alert.component.css'],
    animations: [
        trigger('alertsAnimation', [
            transition('* => *', [

                query(':enter', style({opacity: 0}), {optional: true}),

                query(':enter', stagger('300ms', [
                    animate('.6s ease-in', keyframes([
                        style({opacity: 0, transform: 'translateY(-75%)', offset: 0}),
                        style({opacity: .5, transform: 'translateY(35px)', offset: 0.3}),
                        style({opacity: 1, transform: 'translateY(0)', offset: 1.0}),
                    ]))]), {optional: true})
                ,
                query(':leave', stagger('300ms', [
                    animate('.6s ease-out', keyframes([
                        style({opacity: 1, transform: 'translateY(0)', offset: 0}),
                        style({opacity: .5, transform: 'translateY(35px)', offset: 0.3}),
                        style({opacity: 0, transform: 'translateY(-75%)', offset: 1.0}),
                    ]))]), {optional: true})
            ])
        ])
    ]
})
export class AlertComponent implements OnInit {
    messages: Message[];
    MessageType = MessageType;

    constructor(private alertService: AlertService) {
    }

    ngOnInit() {
        this.messages = this.alertService.getMessages();
    }

    undo(message: Message) {
        this.alertService.undo(message);
    }


    dismiss(message: Message) {
        this.alertService.dismiss(message);
    }
}
