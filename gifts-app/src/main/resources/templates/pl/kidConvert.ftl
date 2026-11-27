<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Zaproszenie do Gifts Hub</title>
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
                <p>Witaj ${kidName},</p>
                <p>${owner!''} zaprasza Ci&#281; do zamiany konta dziecka na zwyk&#322;e konto w aplikacji Gifts Hub.
                    Zachowasz wszystkie swoje prezenty i grupy. Mo&#380;esz logowa&#263; si&#281; kontem Google lub wybra&#263; w&#322;asn&#261; nazw&#281; u&#380;ytkownika i has&#322;o.</p>
                <p>Kliknij link poni&#380;ej, lub skopiuj i wklej go w przegl&#261;darce aby przyj&#261;&#263; zaproszenie. Link jest wa&#380;ny przez 7 dni.</p>
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
        To jest automatyczna wiadomo&#347;&#263; wys&#322;ana z <a href="${application!'#'}" target="_blank">${application!'#'}</a>.<br>
        Prosimy nie odpowiada&#263;
    </p>
</div>
</body>
</html>
