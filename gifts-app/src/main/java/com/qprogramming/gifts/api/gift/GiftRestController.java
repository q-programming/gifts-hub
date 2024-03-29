package com.qprogramming.gifts.api.gift;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.GiftStatus;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryService;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.schedule.AppEventType;
import com.qprogramming.gifts.security.AnonAuthentication;
import com.qprogramming.gifts.settings.SearchEngine;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static com.qprogramming.gifts.support.Utils.not;

/**
 * Created by Khobar on 10.03.2017.
 */
@Controller
@RequestMapping("/api/gift")
public class GiftRestController {

    public static final String BR = "</br>";
    private static final int NAME_CELL = 0;
    private static final int DESCRIPTION_CELL = 1;
    private static final int LINK_CELL = 2;
    private static final int CATEGORY_CELL = 3;
    private static final String COLS = "ABCDEFGHIJKLMNOPRSTUVWXYZ";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final Set<String> prohibitedCategories = new HashSet<>();
    private final AccountService accountService;
    private final GiftService giftService;
    private final SearchEngineService searchEngineService;
    private final CategoryService categoryService;
    private final MessagesService msgSrv;
    private final GroupService groupService;
    private final AppEventService eventService;
    private final MailService mailService;

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService, SearchEngineService searchEngineService, CategoryService categoryService, MessagesService msgSrv, GroupService groupService, AppEventService eventService, MailService mailService) {
        this.accountService = accountService;
        this.giftService = giftService;
        this.searchEngineService = searchEngineService;
        this.categoryService = categoryService;
        this.msgSrv = msgSrv;
        this.groupService = groupService;
        this.eventService = eventService;
        this.mailService = mailService;
        initProhibited();
    }

    private void initProhibited() {
        for (Locale locale : Locale.getAvailableLocales()) {
            String realised = msgSrv.getMessage("gift.category.realised", null, "", locale);
            String other = msgSrv.getMessage("gift.category.other", null, "", locale);
            if (StringUtils.isNotBlank(realised)) {
                prohibitedCategories.add(realised);
            }
            if (StringUtils.isNotBlank(other)) {
                prohibitedCategories.add(other);
            }
        }

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    @RequestMapping("/create")
    public ResponseEntity<?> createGift(@RequestBody Gift giftForm) {
        if (StringUtils.isEmpty(giftForm.getName())) {
            return new ResponseEntity<>("Name field is required", HttpStatus.BAD_REQUEST);
        }
        if (canOperateOnUsernameGifts(giftForm.getUserId())) {
            Gift gift = updateGiftFromForm(giftForm);
            try {
                eventService.addEvent(gift, AppEventType.NEW);
            } catch (AccountNotFoundException e) {
                LOG.debug("Current account not found");
                return ResponseEntity.notFound().build();
            }
            return new ResponseEntity<>(gift, HttpStatus.CREATED);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("group");
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    @RequestMapping("/edit")
    public ResponseEntity<?> editGift(@RequestBody Gift giftForm) {
        Gift gift = giftService.findById(giftForm.getId());
        if (gift == null) {
            return ResponseEntity.notFound().build();
        }
        if (canOperateOnGift(gift)) {
            gift = updateGiftFromForm(giftForm, gift);
            return new ResponseEntity<>(gift, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("group");
    }

    private boolean canOperateOnUsernameGifts(String id) {
        if (StringUtils.isNotBlank(id)) {
            Account giftOwner;
            try {
                giftOwner = accountService.findById(id);
            } catch (AccountNotFoundException e) {
                LOG.debug("Gift owner with id {} was not found", id);
                return false;
            }
            return isGiftOwnerOrGroupMember(giftOwner);
        }
        return false;
    }

    private boolean isGiftOwnerOrGroupMember(Account giftOwner) {
        return giftOwner.equals(Utils.getCurrentAccount())
                || accountService.isAccountGroupMember(giftOwner);

    }

    private boolean canOperateOnGift(Gift gift) {
        Account giftOwner;
        try {
            giftOwner = accountService.findById(gift.getUserId());
        } catch (AccountNotFoundException e) {
            LOG.debug("Account with id {} not found", gift.getUserId());
            return false;
        }
        return Utils.getCurrentAccountId().equals(gift.getCreatedBy())
                || Utils.getCurrentAccount().equals(giftOwner)
                || accountService.isKidAdmin(giftOwner);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/claim/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity<?> claimGift(@PathVariable(value = "giftID") String id) {
        Optional<Account> optionalAccount = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (optionalAccount.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Account claimingAccount = optionalAccount.get();
        Gift gift = giftService.findById(Long.valueOf(id));
        if (Objects.equals(gift.getUserId(), claimingAccount.getId())) {
            return ResponseEntity.badRequest().body("gift.claim.same");
        }
        if(gift.getClaimed() != null ){
            LOG.warn("User {} tried to claim gift {} which was already claimed by {}", claimingAccount.getName(), gift.getName(), gift.getClaimed().getName());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("gift.claim.claimed");
        }
        gift.setClaimed(claimingAccount);
        return ResponseEntity.ok(giftService.update(gift));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/unclaim/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity<?> unClaimGift(@PathVariable(value = "giftID") String id) {
        Optional<Account> optionalAccount = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (optionalAccount.isEmpty()) {
            return ResponseEntity.notFound().build();

        }
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!Objects.equals(gift.getClaimed(), optionalAccount.get())) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.unclaim.error")).build();
        }
        gift.setClaimed(null);
        return ResponseEntity.ok(giftService.update(gift));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/complete/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity<?> completeGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!canOperateOnGift(gift)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        gift.setStatus(GiftStatus.REALISED);
        gift.setRealised(new Date());
        giftService.update(gift);
        try {
            eventService.addEvent(gift, AppEventType.REALISED);
        } catch (AccountNotFoundException e) {
            LOG.debug("Current account not found");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/undo-complete/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity<?> undoCompleteGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!canOperateOnGift(gift)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        gift.setStatus(null);
        gift.setRealised(null);
        giftService.update(gift);
        try {
            eventService.tryToUndoEvent(gift);
        } catch (AccountNotFoundException e) {
            LOG.debug("Current account not found");
            return ResponseEntity.notFound().build();
        }
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.complete.undo.success")).build();
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/delete/{giftID}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (gift == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (!canOperateOnGift(gift)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            eventService.deleteGiftEvents(gift);
            if (gift.getClaimed() != null && gift.getClaimed().getNotifications() && gift.getStatus() != GiftStatus.REALISED) {
                mailService.notifyAboutGiftRemoved(gift);
            }
        } catch (AccountNotFoundException e) {
            LOG.debug("Current account not found");
            return ResponseEntity.notFound().build();
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.debug("Error while trying to send notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        giftService.delete(gift);
        return ResponseEntity.ok().build();
    }

    @Transactional
    @RequestMapping("/image/{giftID}")
    public ResponseEntity<?> giftPicture(@PathVariable(value = "giftID") String giftID) {
        Gift gift = giftService.findById(Long.valueOf(giftID));
        if (gift == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        try {
            Account account = accountService.findById(gift.getUserId());
            if (pictureCanBeViewed(account)) {
                Hibernate.initialize(gift.getImage());
                return ResponseEntity.ok(gift.getImage());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (AccountNotFoundException e) {
            LOG.error("Failed to fetch gift account with id,{}", gift.getUserId());
            return new ResultData.ResultBuilder().notFound().build();
        }
    }

    private boolean pictureCanBeViewed(Account account) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication instanceof AnonAuthentication && account.getPublicList()) ||
                account.equals(Utils.getCurrentAccount()) ||
                account.getGroups()
                        .stream()
                        .anyMatch(group -> group.getMembers().contains(Utils.getCurrentAccount()));
    }


    /**
     * Update giftForm with data from {@link GiftForm}
     *
     * @param giftForm updated  created giftForm
     * @return updated {@link Gift}
     */
    private Gift updateGiftFromForm(Gift giftForm, Gift gift) {
        if (giftForm.getCategory() != null && StringUtils.isNotBlank(giftForm.getCategory().getName())) {
            Category category = categoryService.findByName(giftForm.getCategory().getName());
            giftForm.setCategory(category);
        }
        giftForm.setEngines(searchEngineService.getSearchEngines(
                giftForm.getEngines()
                        .stream()
                        .map(SearchEngine::getId)
                        .collect(Collectors.toList())));
        giftForm.setLinks(giftForm.getLinks().stream().filter(not(String::isEmpty)).collect(Collectors.toSet()));
        //update or create
        if (giftForm.getId() == null) {
            Gift createdGift = giftService.create(giftForm);
            giftService.updateGiftImage(createdGift, giftForm.getImageData());
            return createdGift;
        } else {
            //load image which is lazy loaded and put it in form
            giftForm.setImage(gift.getImage());
            Gift updatedGift = giftService.update(giftForm);
            giftService.updateGiftImage(updatedGift, giftForm.getImageData());
            return updatedGift;
        }
    }

    private Gift updateGiftFromForm(Gift giftForm) {
        return this.updateGiftFromForm(giftForm, null);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/mine")
    public ResponseEntity<?> getMineGifts(@RequestParam(required = false) boolean realised) {
        if (Utils.getCurrentAccount() != null) {
            return new ResponseEntity<>(giftService.findAllByCurrentUser(realised), HttpStatus.OK);
        } else
            return ResponseEntity.ok(Collections.EMPTY_MAP);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/claimed")
    public ResponseEntity<?> getMineClaimedGifts() {
        if (Utils.getCurrentAccount() != null) {
            return new ResponseEntity<>(giftService.findAllClaimedByCurrentUser(), HttpStatus.OK);
        } else
            return ResponseEntity.ok(Collections.EMPTY_MAP);
    }

    @RequestMapping("/user/{usernameOrId}")
    public ResponseEntity<?> getUserGifts(@PathVariable String usernameOrId, @RequestParam(required = false) boolean realised) {
        Account account;
        Optional<Account> optionalAccount = accountService.findByUsername(usernameOrId);
        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
        } else {
            try {
                account = accountService.findById(usernameOrId);
            } catch (AccountNotFoundException e1) {
                return ResponseEntity.notFound().build();
            }
        }
        //check if anonymous user can view user gifts
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonAuthentication)) {
            if (!account.getPublicList()) {
                return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.list.public.error")).build();
            }
        } else if (!accountService.isAccountGroupMember(account)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return new ResponseEntity<>(giftService.findAllByUser(account.getId(), realised), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/categories")
    public ResponseEntity<Collection<Category>> getCategories(@RequestParam(required = false) String term) {
        if (StringUtils.isBlank(term)) {
            return ResponseEntity.ok(categoryService.findAll());
        } else {
            return ResponseEntity.ok(categoryService.findByNameContainingIgnoreCase(term));
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/allowed-category")
    public ResponseEntity<?> checkProhibited(@RequestParam String category) {
        if (!allowedCategoryName(category)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean allowedCategoryName(String category) {
        return prohibitedCategories.stream().noneMatch(s -> s.equalsIgnoreCase(category));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public ResponseEntity<?> importGifts(@RequestParam(value = "file") MultipartFile importFile, @RequestParam(value = "user", required = false) String username) {
        if (StringUtils.isNotBlank(username) && !canOperateOnUsernameGifts(username)) {
            ResponseEntity.status(HttpStatus.CONFLICT).body("group");
        }
        StringBuilder logger = new StringBuilder();
        Workbook workbook;
        try (InputStream importFileInputStream = importFile.getInputStream()) {
            workbook = WorkbookFactory.create(importFileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            processImportSheet(sheet, logger, username);
        } catch (InvalidFormatException e) {
            LOG.error("Failed to determine excel type");
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.import.wrongType")).build();
        } catch (IOException e) {
            LOG.error("IOException: ", e);
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("error.fileIO")).build();
        } catch (AccountNotFoundException e) {
            LOG.error("Errors while trying to find account ", e);
            return new ResultData.ResultBuilder().badReqest().error().message("Errors while trying to find account").build();
        }
        return new ResultData.ResultBuilder().ok().message(logger.toString()).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/get-template", method = RequestMethod.GET)
    public void getTemplate(HttpServletResponse response) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            Row row = sheet.createRow(0);
            row.createCell(NAME_CELL).setCellValue(msgSrv.getMessage("gift.name") + " *");
            row.createCell(DESCRIPTION_CELL).setCellValue(msgSrv.getMessage("gift.description"));
            row.createCell(LINK_CELL).setCellValue(msgSrv.getMessage("gift.link"));
            row.createCell(CATEGORY_CELL).setCellValue(msgSrv.getMessage("gift.category"));
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "template" + ".xls");
            out.flush();
            out.close();
        } catch (IOException e) {
            LOG.error("IOException: ", e);
        }
    }

    private void processImportSheet(Sheet sheet, StringBuilder logger, String id) throws AccountNotFoundException {
        int rowNo = 1;//start from row
        while (sheet.getRow(rowNo) != null) {
            Row row = sheet.getRow(rowNo);
            //Get cells
            String cellAddress = "" + COLS.charAt(NAME_CELL) + rowNo;
            Cell nameCell = row.getCell(NAME_CELL);
            Cell descriptionCell = row.getCell(DESCRIPTION_CELL);
            Cell linkCell = row.getCell(LINK_CELL);
            Cell categoryCell = row.getCell(CATEGORY_CELL);
            if (nameCell != null && nameCell.getCellType() != CellType.BLANK) {
                Gift giftForm = new Gift();
                giftForm.setUserId(id);
                //NAME
                giftForm.setName(nameCell.getStringCellValue());
                //DESCRIPTION
                if (descriptionCell != null) {
                    giftForm.setDescription(row.getCell(DESCRIPTION_CELL, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
                }
                //LINK
                if (linkCell != null) {
                    setLinkFromRow(logger, rowNo, cellAddress, linkCell, giftForm);
                }
                //CATEGORIES
                if (categoryCell != null) {
                    setCategoryFromRow(logger, rowNo, cellAddress, categoryCell, giftForm);
                }
                giftForm = updateGiftFromForm(giftForm);
                String added = msgSrv.getMessage("gift.import.added"
                        , new Object[]{rowNo, giftForm.getName()}
                        , ""
                        , Utils.getCurrentLocale());
                logger.append(added);
            } else {
                String notEmpty = msgSrv.getMessage("gift.import.nameEmpty"
                        , new Object[]{rowNo, cellAddress}
                        , ""
                        , Utils.getCurrentLocale());
                logger.append(notEmpty);
            }
            logger.append(BR);
            rowNo++;
        }
    }

    private void setCategoryFromRow(StringBuilder logger, int rowNo, String cellAddress, Cell categoryCell, Gift giftForm) {
        String category = categoryCell.getStringCellValue();
        if (allowedCategoryName(category)) {
            giftForm.setCategory(new Category(category));
        } else {
            String wrongCategory = msgSrv.getMessage("gift.import.category.prohibited"
                    , new Object[]{rowNo, cellAddress}
                    , ""
                    , Utils.getCurrentLocale());
            logger.append(wrongCategory);
            logger.append(BR);
        }
    }

    private void setLinkFromRow(StringBuilder logger, int rowNo, String cellAddress, Cell linkCell, Gift giftForm) {
        String link = linkCell.getStringCellValue();
        if (Utils.validUrlLink(link)) {
            giftForm.addLink(link);
        } else {
            String wrongLink = msgSrv.getMessage("gift.import.wrongLink"
                    , new Object[]{rowNo, cellAddress}
                    , ""
                    , Utils.getCurrentLocale());
            logger.append(wrongLink);
            logger.append(BR);
        }
    }
}
