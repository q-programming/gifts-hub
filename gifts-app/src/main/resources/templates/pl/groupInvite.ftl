<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Zaproszenie do grupy ${groupName}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
            font-size: 100%;
            line-height: 1.6;
        }

        img {
            vertical-align: middle;
            max-width: 100%;
            display: block;
        }

        body {
            -webkit-font-smoothing: antialiased;
            -webkit-text-size-adjust: none;
            width: 100% !important;
            height: 100%;
        }

        a {
            color: #348eda;
        }

        table.body-wrap {
            width: 100%;
            padding: 20px;
        }

        table.body-wrap .container {
            border: 1px solid #f0f0f0;
        }

        table.footer-wrap {
            width: 100%;
            clear: both !important;
        }

        .footer-wrap .container p {
            font-size: 12px;
            color: #666;
        }

        table.footer-wrap a {
            color: #999;
        }

        .header {
            background-color: #567bb3 !important;
            color: white;
            padding: 5px;
            font-weight: bold;
            vertical-align: middle;
        }

        h1, h2, h3 {
            font-family: "Helvetica Neue", Helvetica, Arial, "Lucida Grande",
            sans-serif;
            line-height: 1.1;
            color: #000;
            margin: 40px 0 10px;
            font-weight: 200;
        }

        h1 {
            font-size: 36px;
        }

        h2 {
            font-size: 28px;
        }

        h3 {
            font-size: 22px;
        }

        p, ul {
            margin-bottom: 10px;
            font-weight: normal;
            font-size: 14px;
        }

        ul li {
            margin-left: 5px;
            list-style-position: inside;
        }

        .container {
            display: block !important;
            max-width: 600px !important;
            margin: 0 auto !important; /* makes it centered */
            clear: both !important;
        }

        .content {
            padding: 20px;
            max-width: 600px !important;
            display: block;
            margin: 30px auto 0;
            background-color: white;
        }

        .content table {
            width: 100%;
        }

        .content .main {
            border: 1px solid lightgray;
            padding: 10px;
        }

        .avatar {
            height: 50px;
            padding: 2px;
            border-radius: 50%
        }
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
                <#if name??>
                    <p>Witaj ${name},</p>
                </#if>
                <div>
                    ${owner} zaprosi&#322; Ci&#281; do grupy "${groupName}" w aplikacji Gifts Hub
                </div>
                <#if confirmLink??>
                    <p>Kliknij link poni&#380;ej, lub skopiuj i wklej go w prz&#281;gl&#261;darce aby potwierdzi&#263; zaproszenie</p>
                    <p>
                        <a href="${confirmLink}">${confirmLink}</a>
                    </p>
                <#else>
                    <p>Poniewa&#380; jeszcze nie posiadasz konta, mo&#380;esz je za&#322;o&#380;y&#263; klikaj&#261;&#263; na link poni&#380;ej:</p>
                    <p>
                        <a href="${registerLink}">${registerLink}</a>
                    </p>
                </#if>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<!-- /content -->
<table class="footer-wrap">
    <tbody>
    <tr>
        <td></td>
        <td class="container">
            <!-- content -->
            <div class="content">
                <table>
                    <tbody>
                    <tr>
                        <td align="center">
                            <p>
                                To jest automatyczna wiadomo&#347;&#263; wys&#322;ana z <a
                                        href="${application!'#'}" target="_blank">${application!'#'}</a>.</br>
                                Je&#380;eli nie chcesz otrzymywa&#263; wi&#281;cej powiadomie&#324; poprzez
                                e-mail, odznacz prosz&#281; opcj&#281; w paneu sterowania.
                            </p>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div> <!-- /content -->
        </td>
        <td></td>
    </tr>
    </tbody>
</table>
<!-- /footer -->


</body>
</html>