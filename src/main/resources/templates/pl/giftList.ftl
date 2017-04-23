<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <title>Lista \u017cycze\u0144</title>
</head>
<body>
<#if name??>
<p>Witaj ${name}</p>
</#if>
<div>
${owner} chcia\u0142 si\u0119 z Tob\u0105 podzieli\u0107 swoja list\u0105 \u017cycze\u0144 stworzon\u0105 w aplikacji
    gifts hub
</div>
<p>Poni\u017cej mo\u017cesz zobaczy\u0107 aktualna list\u0119 prezent\u00f3w, jednak mog\u0142a si\u0119 ona w
    mi\u0119dzy czasie zmieni\u0107</p>
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
    To jest automatyczna wiadomo\u015b\u0107 wys\u0142ana z <a href="${application!'#'}"
                                                               target="_blank">${application!'#'}</a>.
    Prosz\u0119 nie odpowiada\u0107
</div>
</body>
</html>