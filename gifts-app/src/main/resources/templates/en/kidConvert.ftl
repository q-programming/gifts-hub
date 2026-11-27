<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Invitation to Gifts Hub</title>
    <style>
        * { margin: 0; padding: 0; font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif; font-size: 100%; line-height: 1.6; }
        img { vertical-align: middle; max-width: 100%; display: block; }
        body { width: 100% !important; height: 100%; }
        a { color: #348eda; }
        .header { background-color: #5b78b1 !important; color: white; padding: 5px; font-weight: bold; vertical-align: middle; }
        p { margin-bottom: 10px; font-weight: normal; font-size: 14px; }
        .content { padding: 20px; max-width: 600px !important; display: block; margin: 30px auto 0; background-color: white; }
        .content table { width: 100%; }
        .content .main { border: 1px solid lightgray; padding: 10px; }
        .avatar { height: 50px; padding: 2px; border-radius: 50% }
    </style>
</head>
<body bgcolor="#f6f6f6">
<div class="content">
    <div class="header">
        <img src='cid:logo.png' style="height:50px; padding:2px">
    </div>
    <table class="main">
        <tbody>
        <tr>
            <td valign="top" style="vertical-align: top; width:70px">
                <img class="avatar" src='cid:userAvatar.png'>
            </td>
            <td>
                <p>Hello ${kidName},</p>
                <p>${owner!''} invites you to convert your kid account into a regular Gifts Hub account.
                    You will keep all your gifts and groups. You can sign in with your Google account or choose your own username and password.</p>
                <p>Click link below, or copy and paste it in browser to accept the invitation. The link is valid for 7 days.</p>
                <p>
                    <a href="${confirmLink}">${confirmLink}</a>
                </p>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div class="content">
    <p style="text-align:center; font-size:12px; color:#666">
        This is automatic message sent from <a href="${application!'#'}" target="_blank">${application!'#'}</a>.<br>
        Please do not reply
    </p>
</div>
</body>
</html>
