<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${familyName} family administrator</title>
</head>
<body>
<#if name??>
<p>Hello ${name},</p>
</#if>
<div>
${owner} designated you as administrator of "${familyName}" family in Gifts Hub application
</div>
<p>Click link below to confirm</p>
<p>
    <a href="${confirmLink}">${confirmLink}</a>
</p>
<div>
    This is automatic message send from <a href="${application!'#'}" target="_blank">${application!'#'}</a>.
    Please do not reply
</div>
</body>
</html>