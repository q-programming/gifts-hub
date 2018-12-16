import {SearchEngine} from "@model/SearchEngine";

export class AppSettings {
  language: string;
  email: EmailSettings;
  appUrl: string;
  giftAge:number;
  searchEngines:SearchEngine[];

  constructor() {
    this.email = new EmailSettings();
  }

}

export class EmailSettings {
  host: string = '';
  port: number = 25;
  username: string = '';
  password: string = '';
  encoding: string = '';
  from: string = '';
}

