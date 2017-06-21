<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Invitation to ${familyName} family</title>
</head>
<body>
<#if name??>
<p>Hello ${name},</p>
</#if>
<div>
${owner} wanted to invite you to his family "${familyName}" in Gifts Hub application
</div>
<p>Click link below to confirm invitation </p>
<p>
    <a href="${confirmLink}">${confirmLink}</a>
</p>
<div>
    This is automatic message send from <a href="${application!'#'}" target="_blank">${application!'#'}</a>.
    Please do not reply
</div>
</body>
</html>