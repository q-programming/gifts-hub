<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Podsumowanie ostatniego tygodnia</title>
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

        .btn-primary, .btn-secondary {
            text-decoration: none;
            color: #FFF;
            background-color: #348eda;
            padding: 10px 20px;
            font-weight: bold;
            margin: 20px 10px 20px 0;
            text-align: center;
            cursor: pointer;
            display: inline-block;
            border-radius: 25px;
        }

        .btn-secondary {
            background: #aaa;
        }

        .last {
            margin-bottom: 0;
        }

        .first {
            margin-top: 0;
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
            background-color: rgba(44, 90, 160, 0.8) !important;
            color: white;
            padding: 5px;
            font-weight: bold;
            vertical-align: middle;
        }

        .text {
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

        table.worklog_table {
            width: 100%;
            border-collapse: collapse;
            border: 1px lightgray;
            border-bottom-style: solid;
        }

        table.worklog_table td {
            width: 50%;
            padding: 1px 1px 1px 5px;
            border: 1px solid lightgray;
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
            <#--<img src='cid:avatar' style="height:50px; padding:2px">-->
            </td>
            <td>
                <p>
                </p>
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
                                    href="${application!'#'}" target="_blank">${application!'#'}"</a>.</br>
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