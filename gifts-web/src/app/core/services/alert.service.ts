import {Injectable} from '@angular/core';
import {Message, MessageType} from "@model/Message";
import {Observable, Subscriber, timer} from "rxjs";
import {TranslateService} from "@ngx-translate/core";
import * as _ from 'lodash';

@Injectable()
export class AlertService {

    private messages: Message[] = [];
    private normal_timeout: number = 4000; //4 seconds
    private undo_timeout: number = 5000; //5 seconds
    private error_timeout: number = 6000; //6 seconds

    constructor(private translate: TranslateService) {
        this.translate.setDefaultLang('en');
    }

    /**
     * Return all currently added messages
     */
    getMessages(): Message[] {
        return this.messages;
    }

    /**
     * Add success message
     *
     * @param message text of success message
     * @param timeout (optional) timeout when message with disappear
     */
    successMessage(message: string, timeout: number = this.normal_timeout) {
        this.addMessage(message, MessageType.SUCCESS, timeout);
    }

    /**
     * Add error message
     *
     * @param message text of error message
     * @param timeout (optional) timeout when message with disappear
     */
    errorMessage(message: string, timeout: number = this.error_timeout) {
        this.addMessage(message, MessageType.ERROR, timeout);
    }

    /**
     * Add warning message
     *
     * @param message text of warning message
     * @param timeout (optional) timeout when message with disappear
     */
    warningMessage(message: string, timeout: number = this.normal_timeout) {
        this.addMessage(message, MessageType.WARNING, timeout);
    }

    /**
     * Add info message
     *
     * @param message text of info message
     * @param timeout (optional) timeout when message with disappear
     */
    infoMessage(message: string, timeout: number = this.normal_timeout) {
        this.addMessage(message, MessageType.INFO, timeout);
    }

    /**
     * Add success message with translated key
     *
     * @param key key of messages which will be translated
     * @param params (optional) optional parameters which will be passed to translation service
     * @param timeout (optional) timeout when message with disappear
     */
    success(key: string, params?: Object, timeout: number = this.normal_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.SUCCESS, timeout);
        })
    }

    /**
     * Add error message with translated key
     *
     * @param key key of messages which will be translated
     * @param params (optional) optional parameters which will be passed to translation service
     * @param timeout (optional) timeout when message with disappear
     */
    error(key: string, params?: Object, timeout: number = this.error_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.ERROR, timeout);
        })
    }

    /**
     * Add warning message with translated key
     *
     * @param key key of messages which will be translated
     * @param params (optional) optional parameters which will be passed to translation service
     * @param timeout (optional) timeout when message with disappear
     */
    warning(key: string, params?: Object, timeout: number = this.normal_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.WARNING, timeout);
        })

    }

    /**
     * Add info message with translated key
     *
     * @param key key of messages which will be translated
     * @param params (optional) optional parameters which will be passed to translation service
     * @param timeout (optional) timeout when message with disappear
     */
    info(key: string, params?: Object, timeout: number = this.error_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.INFO, timeout);
        })
    }

    /**
     * Shows "success kind of" message which waits 5 seconds before actually executing futher operations
     * Can be used for various deletion with undo operation
     *
     * @param key key of messages which will be translated
     * @param params (optional) optional parameters which will be passed to translation service
     */
    undoable(key: string, params?: Object): Observable<boolean> {
        return new Observable((observable) => {
            this.translate.get(key, params).subscribe(txt => {
                this.addMessage(txt, MessageType.UNDOABLE, this.undo_timeout, observable);
            })
        });
    }

    /**
     * Adds message with given text and type
     * Can have observable to hold any pending operations
     * @see AlertService.undoable
     *
     * @param text text of message
     * @param type message type
     * @param timeout when message will disappear
     * @param observable (optional) observable object to hold any pending operations
     */
    private addMessage(text: string, type: MessageType, timeout: number, observable?: Subscriber<boolean>) {
        let message = new Message(text, type, observable);
        if (!this.exists(message)) {
            this.messages.push(message);
            timer(timeout).subscribe(() => {
                this.dismiss(message)
            });
        }
    }

    /**
     * Dismiss message by removing it from all messages
     * If message is type UNDOABLE and has observable, there are two possible actions:
     * 1. Rush in pending operation if user just dismissed operation
     * 2. Cancel pending operation if user clicked 'Undo'
     *
     * @param message message to be dismissed
     */
    dismiss(message: Message) {
        _.remove(this.messages, (m) => {
            return m.id === message.id
        });
        if (message.type == MessageType.UNDOABLE && message.undoObservable) {
            if (message.undo) {
                message.undoObservable.next(true);
            } else {
                message.undoObservable.next(false);
            }
            message.undoObservable.complete();
        }
    }

    private exists(message: Message) {
        return _.find(this.messages, (m) => {
            return m.type === message.type && m.text === message.text
        })
    }

    /**
     * Undo any pending operations by dismissing message but marking pending operation as canceled
     *
     * @see AlertService.dismiss
     * @see AlertService.undoable
     * @param message
     */
    undo(message: Message) {
        message.undo = true;
        this.dismiss(message)
    }
}
