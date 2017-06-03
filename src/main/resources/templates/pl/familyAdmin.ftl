<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Administrator rodziny ${familyName}</title>
</head>
<body>
<#if name??>
<p>Witaj ${name},</p>
</#if>
<div>
${owner} oznaczy&#322; Ci&#281; jako administratora rodziny "${familyName}" w aplikacji Gifts Hub
</div>
<p>Kliknij link poni&#380;ej aby potwierdzi&#263;</p>
<p>
    <a href="${confirmLink}">${confirmLink}</a>
</p>
<div>
    To jest automatyczna wiadomo&#347;&#263; wys&#322;ana z <a href="${application!'#'}"
                                                               target="_blank">${application!'#'}</a>.
    Prosz&#281; nie odpowiada&#263;
</div>
</body>
</html>