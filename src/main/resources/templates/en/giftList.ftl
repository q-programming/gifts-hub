<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Lista życzeń</title>
</head>
<body>
<#if name??>
<p>Witaj ${name}</p>
</#if>
<div>
${owner} chciał się z Tobą podzielić swoja listą życzeń stworzoną w aplikacji gifts hub
</div>
<p>Poniżej możesz zobaczyć aktualna listę prezentów, jednak mogła się ona w między czasie zmienić</p>
<div>
<#list gifts?keys as category>
    <h4><strong>${category}</strong></h4>
    <ul>
        <#list gifts.get(category) as gift>
            <li>
            ${gift.name} <a href="${gift.link!'#'}"></a>
            </li>
        </#list>
    </ul>
</#list>
</div>
<div>
    To jest automatyczna wiadomość wysłana z <a href="${application!'#'}" target="_blank">${application!'#'}</a>.
    Proszę nie odpowiadać
</div>
</body>
</html>