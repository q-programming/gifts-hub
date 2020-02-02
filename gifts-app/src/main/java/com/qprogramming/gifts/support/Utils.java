package com.qprogramming.gifts.support;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.config.mail.Mail;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.category.Category;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final Comparator<Account> ACCOUNT_COMPARATOR = Comparator.comparing(Account::getName).thenComparing(Account::getSurname).thenComparing(Account::getUsername).thenComparing(Account::getId);
    public static final Comparator<Gift> GIFT_COMPARATOR = Comparator.nullsFirst(Comparator.comparing(Gift::getRealised)).thenComparing(Gift::getName);
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_TIME = "dd-MM-yyyy HH:mm";
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final Pattern VALID_HTML_TAG_REGEX = Pattern.compile("<(\\/)?([A-Za-z][A-Za-z0-9]*)\\b[^>]*>");
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_LINK_REGEX = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

    public static Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && !(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return (Account) authentication.getPrincipal();
        }
        return null;
    }

    public static String getCurrentAccountId() {
        Account currentAccount = getCurrentAccount();
        if (currentAccount != null) {
            return currentAccount.getId();
        }
        return StringUtils.EMPTY;
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
        Matcher matcher = VALID_HTML_TAG_REGEX.matcher(contents);
        return matcher.matches() || matcher.find();
    }

    /**
     * Returns tru if passed link is a valid url
     *
     * @param link url string to be checked
     * @return true if link is a valid url
     */
    public static boolean validUrlLink(String link) {
        Matcher matcher = VALID_LINK_REGEX.matcher(link);
        return matcher.matches() || matcher.find();
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
        return getDate(date, DATE_FORMAT);
    }

    private static Date getDate(String date, String dateFormat) {
        Date result = null;
        try {
            result = new SimpleDateFormat(dateFormat).parse(date);
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
        return getDate(date, DATE_FORMAT_TIME);
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

    /**
     * Converts gifts list to TreeMap based on their's category
     *
     * @param gifts list of gifts to put into TreeMap
     * @return TreeMap with gifts sorted by categories ( based on priorities)
     */
    public static Map<Category, List<Gift>> toGiftTreeMap(List<Gift> gifts) {
        Map<Category, List<Gift>> result = new TreeMap<>();
        gifts.forEach(gift -> {
            result.computeIfAbsent(gift.getCategory(), k -> new ArrayList<>());
            result.get(gift.getCategory()).add(gift);
        });
        return result;
    }

    /**
     * Validate if passed emailStr is valid email address
     *
     * @param emailStr string to be evaluated
     * @return true if address is valid
     */
    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    /**
     * Creates mail out of accountTo
     *
     * @param accountTo accountTo for which mail will be created
     * @param owner     Owner accountTo which triggered mail
     * @return list of {@link Mail}
     */
    public static Mail createMail(Account accountTo, Account owner) {
        Mail mail = new Mail();
        mail.setMailTo(accountTo.getEmail());
        mail.setLocale(accountTo.getLanguage());
        mail.addToModel("name", accountTo.getFullname());
        if (owner != null) {
            mail.addToModel("owner", owner.getFullname());
        }
        return mail;
    }

    public static Mail createMail(Account account) {
        return createMail(account, null);
    }

    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }


    public static String decodeTypeFromBytes(byte[] bytes) {
        String type = "";
        try {
            type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            LOG.error("Failed to determine type from bytes, presuming jpg");
        }
        if (StringUtils.isEmpty(type)) {
            type = MediaType.IMAGE_JPEG_VALUE;
        }
        return type;
    }

}
