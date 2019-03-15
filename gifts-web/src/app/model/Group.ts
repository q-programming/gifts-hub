import {Account} from "@model/Account";

export class Group {
  id: number;
  name: string;
  members: Account[];
  admins: Account[];
}

export class GroupForm {
  id?: number;
  name?: string;
  members?: string[];
  admins?: string[];
  removed?: boolean;
}

