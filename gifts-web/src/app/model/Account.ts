import {Group} from "@model/Group";

export class Account {
  id?: string;
  username?: string;
  name?: string;
  surname?: string;
  email?: string;
  language?: string;
  role?: Role;
  authorities?: Authority[];
  avatar?: any;
  token?: string;
  fullname?: string;
  groupAdmin?: boolean;
  type: AccountType;
  giftsCount?: number;
  publicList?: boolean;
  notifications?: boolean;
  seenChangelog?: boolean;
  admin?:boolean;
  groups?:Group[];
}

export class Authority {
  authority: Role
}

export enum Role {
  ROLE_ADMIN = "ROLE_ADMIN", ROLE_USER = "ROLE_USER"
}

export enum AccountType {
  LOCAL = "LOCAL", GOOGLE = "GOOGLE", FACEBOOK = "FACEBOOK", KID = "KID"
}
