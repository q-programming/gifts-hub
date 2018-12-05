import {Subscriber} from "rxjs";

export class Message {
    id: string;
    type: MessageType;
    text: string;
    undo: boolean;
    undoObservable?: Subscriber<Boolean>;

    constructor(text: string, type: MessageType, observable?: Subscriber<Boolean>) {
        this.id = Math.round((Math.random() * 36 ** 12)).toString(36);
        this.text = text;
        this.type = type;
        this.undoObservable = observable;
    }
}


export enum MessageType {
    SUCCESS, ERROR, WARNING, INFO, UNDOABLE
}