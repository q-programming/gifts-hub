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
