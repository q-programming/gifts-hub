package com.qprogramming.gifts.support;

import com.qprogramming.gifts.account.Account;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_TIME = "dd-MM-yyyy HH:mm";
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final String HTML_TAG_PATTERN = "<(\\/)?([A-Za-z][A-Za-z0-9]*)\\b[^>]*>";
    private static final String ESTIMATES_PATTENR = "\\s?(\\d*d)?\\s?(\\d*h)?\\s?(\\d*m)?";
    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
    private static String baseURL;
    private static HttpServletRequest request;
    @Value("${default.locale}")
    private String defaultLang;

    public static Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && !(authentication instanceof OAuth2Authentication)) {
            return (Account) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Returns milis for timestamp of given uuid
     *
     * @param uuid
     * @return
     */
    public static long getTimeFromUUID(UUID uuid) {
        return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
    }

    /**
     * Returns true if contents have at least one html tag
     *
     * @return
     */
    public static boolean containsHTMLTags(String contents) {
        Pattern pattern = Pattern.compile(HTML_TAG_PATTERN);
        Matcher matcher = pattern.matcher(contents);
        return matcher.matches() || matcher.find();
    }

    public static String getBaseURL() {
        if (baseURL == null) {
            int port = request.getServerPort();
            if (port == 80) {
                baseURL = String.format("%s://%s/tasq", request.getScheme(), request.getServerName());
            } else {
                baseURL = String.format("%s://%s:%d/tasq", request.getScheme(), request.getServerName(), port);
            }
        }
        return baseURL;
    }

    public static Locale getCurrentLocale() {
        Account currentAccount = getCurrentAccount();
        if (currentAccount == null) {
            return getDefaultLocale();
        }
        return new Locale(currentAccount.getLanguage());
    }

    /**
     * Use this method only if locale was previously set!!
     *
     * @return
     */
    public static Locale getDefaultLocale() {
        return LocaleContextHolder.getLocale();
    }

    public static boolean contains(Collection<?> coll, Object o) {
        return coll.contains(o);
    }

    /**
     * Eliminates underscores and capitalizes first letter of given string
     *
     * @param s
     * @return
     */
    public static String capitalizeFirst(String s) {
        s = s.replaceAll("_", " ");
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * Returns date in simple format
     *
     * @param date
     * @return
     */
    public static Date convertStringToDate(String date) {
        Date result = null;
        try {
            result = new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public static boolean endsWithIgnoreCase(String input, String substring) {
        return StringUtils.endsWithIgnoreCase(input, substring);

    }

    /**
     * Returns date in simple format
     *
     * @param date
     * @return
     */
    public static Date convertStringToDateAndTime(String date) {
        Date result = null;
        try {
            result = new SimpleDateFormat(DATE_FORMAT_TIME).parse(date);
        } catch (ParseException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    /**
     * Returns strng with date and time
     *
     * @param date
     * @return
     */
    public static String convertDateTimeToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT_TIME).format(date);
    }

    public static String convertDateToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static String stripHtml(String string) {
        return Jsoup.parse(string).text();
    }

    /**
     * Coppy file from source path to destination file Used mostly for getting
     * files from resources etc. and coping to some destFile
     *
     * @param sc
     * @param sourcePath
     * @param destFile
     */
    public static void copyFile(ServletContext sc, String sourcePath, File destFile) {
        try {
            InputStream in = new FileInputStream(sc.getRealPath(sourcePath));
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * If user is logged in he will be forecebly logged out
     *
     * @param request HttpServletRequest
     */
    public static void forceLogout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
    }
}