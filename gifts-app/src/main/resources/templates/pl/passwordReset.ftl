<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Instrukcja resetowania has&#322;a</title>
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

        .text-center {
            text-align: center;
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
                    <p>Witaj ${name}</p>
                </#if>
                <p>
                    Poprosi&#322;es o zresetowanie swojego has&#322;a w aplikacji Gifts Hub
                </p>
                <#if linkGoogle??>
                    <p>
                        Twoje konto zosta&#322;o stworzon&#281; przy pomocy zalogowania si&#281; z Google i nie ma
                        &#380;adnego przypisanego
                        has&#322;a<br>
                        Przejdz prosz&#281; na stron&#281; logowania i uzyj opcji Zaloguj si&#281; z Google, lub kliknij
                        na guzik
                        poni&#380;ej
                    </p>
                    <p>
                        <a href="${linkGoogle}">
                            <img src="cid:signInGoogle.png" height="25px" alt="Zaloguj si&#281; z Google">
                        </a>
                    </p>
                <#elseif linkFacebook??>
                    <p>
                        Twoje konto zosta&#322;o stworzon&#281; przy pomocy zalogowania si&#281; z Facebookiem i nie ma
                        &#380;adnego
                        przypisanego has&#322;a<br>
                        Przejdz prosz&#281; na stron&#281; logowania i uzyj opcji Zaloguj si&#281; z przez Facebooka,
                        lub kliknij na
                        guzik poni&#380;ej
                    </p>
                    <p>
                        <a href="${linkFacebook}">
                            <img src="cid:signInFacebook.png" height="25px" alt="Zaloguj si&#281; przez Facebooka">
                        </a>
                    </p>
                <#else>
                    <p>
                        Kliknij link poni&#380;ej, lub skopiuj i wklej go w prz&#281;gl&#261;darce aby potwierdzi&#263;
                        resetowanie i stworzy&#263; nowe has&#322;o
                    </p>
                    <p>Link jest wa&#380;ny tylko przez nast&#281;pne 12 godzin</p>
                    <p>
                        <a href="${confirmLink}">${confirmLink}</a>
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
                                Prosz&#281; nie odpowiada&#263;
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