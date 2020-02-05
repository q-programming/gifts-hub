import {SearchEngine} from "@model/SearchEngine";
import {Category} from "@model/Category";
import {Account} from "@model/Account";
import {GiftImage} from "@model/GiftImage";

export class Gift {
  id: number;
  name: string;
  description: string;
  links?: string[] = [];
  userId?: string;
  createdBy?: string;
  engines?: SearchEngine[] = [];
  category?: Category;
  claimed?: Account;
  created?: Date;
  realised?: Date;
  status?: GiftStatus;
  hidden?: boolean;
  image?: GiftImage;
  imageData?: String;
  hasImage?: boolean;
}

export enum GiftStatus {
  NEW = "NEW", CLAIMED = "CLAIMED", REALISED = "REALISED"
}
