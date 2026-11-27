<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Your account was converted</title>
    <style>
        * { margin: 0; padding: 0; font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif; font-size: 100%; line-height: 1.6; }
        img { vertical-align: middle; max-width: 100%; display: block; }
        body { width: 100% !important; height: 100%; }
        a { color: #348eda; }
        .header { background-color: #5b78b1 !important; color: white; padding: 5px; font-weight: bold; vertical-align: middle; }
        p { margin-bottom: 10px; font-weight: normal; font-size: 14px; }
        .content { padding: 20px; max-width: 600px !important; display: block; margin: 30px auto 0; background-color: white; }
        .content .main { border: 1px solid lightgray; padding: 10px; }
    </style>
</head>
<body bgcolor="#f6f6f6">
<div class="content">
    <div class="header">
        <img src='cid:logo.png' style="height:50px; padding:2px">
    </div>
    <div class="main">
        <#if name??>
            <p>Hello ${name},</p>
        </#if>
        <p>Your kid account was successfully converted into a regular Gifts Hub account. All your gifts and groups were kept.</p>
        <p>You can sign in here: <a href="${loginLink}">${loginLink}</a></p>
    </div>
</div>
<div class="content">
    <p style="text-align:center; font-size:12px; color:#666">
        This is automatic message sent from <a href="${application!'#'}" target="_blank">${application!'#'}</a>.<br>
        Please do not reply
    </p>
</div>
</body>
</html>
