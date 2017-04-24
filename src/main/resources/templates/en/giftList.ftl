<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Wish list</title>
</head>
<body>
<#if name??>
<p>Hello ${name}</p>
</#if>
<div>
${owner} wanted to share with you his wish list in Gifts Hub application
</div>
<p>Click below to view it </p>
<p>
    <a href="${publicLink}">${publicLink}</a>
</p>
<div>
    To jest automatyczna wiadomość wysłana z <a href="${application!'#'}" target="_blank">${application!'#'}</a>.
    Proszę nie odpowiadać
</div>
</body>
</html>