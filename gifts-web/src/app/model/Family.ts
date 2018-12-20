import {Account} from "@model/Account";

export class Family {
  id: number;
  name: string;
  members: Account[];
  admins: Account[];
}

export class FamilyForm {
  name?: string;
  members?: string[];
  admins?: string[];
  removed?: boolean;
}

