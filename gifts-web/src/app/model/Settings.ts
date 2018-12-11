import {SearchEngine} from "@model/SearchEngine";
import {Category} from "@model/Category";

export class Settings {
  language: string;
  searchEngines: SearchEngine[];
  giftAge: number;
  sort: SortBy;
  email: EmailSettings;
  appUrl: string;
  categories: CategoryDTO[];


}

export enum SortBy {
  FAMILY = "FAMILY", NAME = "NAME"
}

export class EmailSettings {
  host: string = '';
  port: number = 25;
  username: string = '';
  password: string = '';
  encoding: string = '';
  from: string = '';
}

export class CategoryDTO {
  category: Category;
  count: number;
}
