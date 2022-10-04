import {Account, Role} from "@model/Account";
import * as _ from "lodash"

export function getBase64Image(data: String): String {
  return data.replace(/^data:image\/(png|jpg|jpeg);base64,/, "");
}

export function isAdmin(user: Account) {
  return !!_.find(user.authorities, (o) => o.authority == Role.ROLE_ADMIN)
}

/**
 * Stop propagation of any next event that happens after action
 * @param event event to stop propagation ( so that mat-expansion-panel won't collapse )
 */
export function menuClick(event: Event) {
  event.stopPropagation();
}

const _MS_PER_DAY = 1000 * 60 * 60 * 24;

export const dateDiffInDays = (d1: Date, d2: Date): number => {
  const utc1 = Date.UTC(d1.getFullYear(), d1.getMonth(), d1.getDate());
  const utc2 = Date.UTC(d2.getFullYear(), d2.getMonth(), d2.getDate());

  return Math.floor((utc2 - utc1) / _MS_PER_DAY);
}
