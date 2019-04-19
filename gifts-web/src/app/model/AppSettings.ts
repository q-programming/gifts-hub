import {SearchEngine} from "@model/SearchEngine";
import {Category} from "@model/Category";

export class AppSettings {
  language: string;
  email: EmailSettings;
  appUrl: string;
  giftAge: number;
  searchEngines: SearchEngine[];
  sort: SortBy;
  categories: CategoryDTO[];

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

export enum SortBy {
  GROUP = "GROUP", NAME = "NAME"
}

export class CategoryDTO {
  category: Category;
  selected: boolean;
  count: number;
}


