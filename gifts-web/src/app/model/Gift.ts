import {SearchEngine} from "@model/SearchEngine";
import {Category} from "@model/Category";
import {Account} from "@model/Account";

export class Gift {
  id: number;
  name: string;
  description: string;
  links: string[] = [];
  userId: string;
  createdBy: string;
  engines: SearchEngine[] = [];
  category: Category;
  claimed: Account;
  created: Date;
  realised: Date;
  status: GiftStatus;
  hidden: boolean;
}

export enum GiftStatus {
  NEW = "NEW", CLAIMED = "CLAIMED", REALISED = "REALISED"
}
