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
${owner} chcia&#322; si&#281; z Tob&#261; podzieli&#263; swoja list&#261; &#380;ycze&#324; stworzon&#261; w aplikacji
    gifts hub
</div>
<p>Kliknij poni&#380;ej aby j&#261; obejrze&#263;</p>
<p>
    <a href="${publicLink}">${publicLink}</a>
</p>
<div>
    To jest automatyczna wiadomo&#347;&#263; wys&#322;ana z <a href="${application!'#'}"
                                                               target="_blank">${application!'#'}</a>.
    Prosz&#281; nie odpowiada&#263;
</div>
</body>
</html>