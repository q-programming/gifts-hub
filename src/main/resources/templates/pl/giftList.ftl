<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Lista \u017cycze\u0144</title>
</head>
<body>
<#if name??>
<p>Witaj ${name}</p>
</#if>
<div>
${owner} chcia\u0142 si\u0119 z Tob\u0105 podzieli\u0107 swoja list\u0105 \u017cycze\u0144 stworzon\u0105 w aplikacji gifts hub
</div>
<p>Kliknij poni\u017cej aby j\u0105 obejrze\u0107</p>
<p>
    <a href="${publicLink}">${publicLink}</a>
</p>
<div>
    To jest automatyczna wiadomo\u015b\u0107 wys\u0142ana z <a href="${application!'#'}" target="_blank">${application!'#'}</a>.
    Prosz\u0119 nie odpowiada\u0107
</div>
</body>
</html>