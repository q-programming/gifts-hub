import {Pipe, PipeTransform} from '@angular/core';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";

@Pipe({name: 'highlight'})
export class HighlightPipe implements PipeTransform {

    constructor(public sanitizer: DomSanitizer) {
    }

    transform(text: string, search): SafeHtml {
        if (search && typeof search === 'string') {
            let pattern = search.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
            pattern = pattern.split(' ').filter((t) => {
                return t.length > 0;
            }).join('|');
            let textParts = text.split(/\ /g);
            let regex = new RegExp(pattern, 'gi');
            for (let i = 0; i < textParts.length; i++) {
                textParts[i] = textParts[i].replace(regex, (match) => `<b>${match}</b>`);
            }
            return this.sanitizer.bypassSecurityTrustHtml(textParts.join(" "));
        }
        return text;
    }
}
