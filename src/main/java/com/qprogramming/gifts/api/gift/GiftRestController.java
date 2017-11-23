package com.qprogramming.gifts.api.gift;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.family.Family;
import com.qprogramming.gifts.account.family.FamilyService;
import com.qprogramming.gifts.gift.Gift;
import com.qprogramming.gifts.gift.GiftForm;
import com.qprogramming.gifts.gift.GiftService;
import com.qprogramming.gifts.gift.GiftStatus;
import com.qprogramming.gifts.gift.category.Category;
import com.qprogramming.gifts.gift.category.CategoryRepository;
import com.qprogramming.gifts.gift.link.Link;
import com.qprogramming.gifts.gift.link.LinkRepository;
import com.qprogramming.gifts.messages.MessagesService;
import com.qprogramming.gifts.schedule.AppEventService;
import com.qprogramming.gifts.schedule.AppEventType;
import com.qprogramming.gifts.settings.SearchEngineService;
import com.qprogramming.gifts.support.ResultData;
import com.qprogramming.gifts.support.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private List<String> prohibitedCategories = new ArrayList<>();
    private AccountService accountService;
    private GiftService giftService;
    private SearchEngineService searchEngineService;
    private CategoryRepository categoryRepository;
    private MessagesService msgSrv;
    private FamilyService familyService;
    private AppEventService eventService;
    private LinkRepository linkRepository;

    @Autowired
    public GiftRestController(AccountService accountService, GiftService giftService, SearchEngineService searchEngineService, CategoryRepository categoryRepository, MessagesService msgSrv, FamilyService familyService, AppEventService eventService, LinkRepository linkRepository) {
        this.accountService = accountService;
        this.giftService = giftService;
        this.searchEngineService = searchEngineService;
        this.categoryRepository = categoryRepository;
        this.msgSrv = msgSrv;
        this.familyService = familyService;
        this.eventService = eventService;
        this.linkRepository = linkRepository;
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

    @Transactional
    @RequestMapping("/create")
    public ResponseEntity createGift(@RequestBody GiftForm giftForm) {
        Gift newGift = new Gift();
        if (StringUtils.isEmpty(giftForm.getName())) {
            return new ResponseEntity<>("Name field is required", HttpStatus.BAD_REQUEST);
        }
        if (canOperateOnUsernameGifts(giftForm.getUsername()) || StringUtils.isBlank(giftForm.getUsername())) {
            Gift gift = updateGiftFromForm(giftForm, newGift);
            eventService.addEvent(gift, AppEventType.NEW);
            return new ResponseEntity<>(gift, HttpStatus.CREATED);
        }
        return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.admin.error")).build();
    }

    @Transactional
    @RequestMapping("/edit")
    public ResponseEntity editGift(@RequestBody GiftForm giftForm) {
        Gift gift = giftService.findById(giftForm.getId());
        if (gift == null) {
            return ResponseEntity.notFound().build();
        }
        if (canOperateOnGift(gift)) {
            gift = updateGiftFromForm(giftForm, gift);
            return new ResponseEntity<>(gift, HttpStatus.OK);
        }
        return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.admin.error")).build();
    }

    private boolean canOperateOnUsernameGifts(String username) {
        if (StringUtils.isNotBlank(username)) {
            Account giftOwner = accountService.findByUsername(username);
            if (giftOwner == null) {
                return false;
            }
            Family family = familyService.getFamily(giftOwner);
            return family != null && (family.getAdmins().contains(Utils.getCurrentAccount()));
        }
        return false;
    }

    private boolean canOperateOnGift(Gift gift) {
        Account giftOwner = accountService.findById(gift.getUserId());
        if (giftOwner == null) {
            return false;
        }
        Family family = familyService.getFamily(giftOwner);
        return family == null
                || (family.getAdmins().contains(Utils.getCurrentAccount()))
                || giftOwner.equals(Utils.getCurrentAccount());
    }


    @RequestMapping(value = "/claim/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity clameGift(@PathVariable(value = "giftID") String id) {
        Account account = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        Gift gift = giftService.findById(Long.valueOf(id));
        if (Objects.equals(gift.getUserId(), account.getId())) {
            return ResponseEntity.badRequest().body(msgSrv.getMessage("gift.claim.same"));
        }
        gift.setClaimed(account);
        giftService.update(gift);
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.claim.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale())).build();
    }

    @RequestMapping(value = "/unclaim/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity unClameGift(@PathVariable(value = "giftID") String id) {
        Account account = accountService.findByUsername(Utils.getCurrentAccount().getUsername());
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!Objects.equals(gift.getClaimed(), account)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.unclaim.error")).build();
        }
        gift.setClaimed(null);
        giftService.update(gift);
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.unclaim.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build();
    }

    @RequestMapping(value = "/complete/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity completeGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!canOperateOnGift(gift)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.complete.error")).build();
        }
        gift.setStatus(GiftStatus.REALISED);
        giftService.update(gift);
        eventService.addEvent(gift, AppEventType.REALISED);
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.complete.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale()))
                .build();
    }

    @RequestMapping(value = "/undo-complete/{giftID}", method = RequestMethod.PUT)
    public ResponseEntity undoCompleteGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (!canOperateOnGift(gift)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.complete.error")).build();
        }
        gift.setStatus(null);
        giftService.update(gift);
        eventService.tryToUndoEvent(gift);
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.complete.undo.success")).build();
    }

    @RequestMapping(value = "/delete/{giftID}", method = RequestMethod.DELETE)
    public ResponseEntity deleteGift(@PathVariable(value = "giftID") String id) {
        Gift gift = giftService.findById(Long.valueOf(id));
        if (gift == null) {
            return new ResultData.ResultBuilder().notFound().build();
        }
        if (!canOperateOnGift(gift)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.delete.error")).build();
        }
        linkRepository.delete(gift.getLinks());
        eventService.deleteGiftEvents(gift);
        giftService.delete(gift);
        return new ResultData.ResultBuilder().ok().message(msgSrv.getMessage("gift.delete.success", new Object[]{gift.getName()}, "", Utils.getCurrentLocale())).build();
    }


    /**
     * Update gift with data from {@link GiftForm}
     *
     * @param giftForm form from which data will updated
     * @param gift     updated  created gift
     * @return updated {@link Gift}
     */
    private Gift updateGiftFromForm(GiftForm giftForm, Gift gift) {
        gift.setName(giftForm.getName());
        gift.setDescription(giftForm.getDescription());
//        gift.setLink(giftForm.getLink());
        updateLinks(giftForm, gift);
        if (StringUtils.isNotBlank(giftForm.getCategory())) {
            String name = giftForm.getCategory();
            Category category = categoryRepository.findByName(name);
            if (category == null) {
                category = categoryRepository.save(new Category(name));
            }
            gift.setCategory(category);
        } else {
            gift.setCategory(null);
        }
        gift.setEngines(searchEngineService.getSearchEngines(giftForm.getSearchEngines()));
        //update or create
        if (gift.getId() == null) {
            if (StringUtils.isNotBlank(giftForm.getUsername())) {
                Account account = accountService.findByUsername(giftForm.getUsername());
                gift.setUserId(account.getId());
            } else {
                Account currentAccount = Utils.getCurrentAccount();
                gift.setUserId(currentAccount.getId());
            }
            return giftService.create(gift);
        } else {
            return giftService.update(gift);
        }
    }

    private void updateLinks(GiftForm giftForm, Gift gift) {
        //find all links that potentially were deleted in form
        List<Link> newLinks = giftForm.getLinks().stream().filter(link -> link.getId() == null && StringUtils.isNotBlank(link.getUrl())).collect(Collectors.toList());
        List<Link> linksToDelete = CollectionUtils.disjunction(giftForm.getLinks(), gift.getLinks())
                .stream()
                .filter(link -> link.getId() != null)
                .collect(Collectors.toList());
        gift.getLinks().removeAll(linksToDelete);
        linkRepository.delete(linksToDelete);
        //update urls
        giftForm.getLinks().removeAll(newLinks);
        Map<Long, String> updatedLinks = giftForm.getLinks().stream().collect(Collectors.toMap(Link::getId, Link::getUrl));
        gift.getLinks().forEach(link -> {
            link.setUrl(updatedLinks.get(link.getId()));
        });
        newLinks.forEach(link -> {
                gift.addLink(linkRepository.save(link));
        });
    }

    @RequestMapping("/convertLinks")
    @Deprecated
    @Transactional
    public ResponseEntity convertLinks() {
        List<Gift> gifts = giftService.findAll();
        gifts.stream().filter(gift -> StringUtils.isNotBlank(gift.getLink())).forEach(gift -> {
            Link link = new Link(gift.getLink());
            gift.addLink(linkRepository.save(link));
            gift.setLink(null);
            giftService.update(gift);
            LOG.info("Link for gift {} converted to entity");
        });
        return ResponseEntity.ok("Links converted");
    }

    @RequestMapping("/mine")
    public ResponseEntity getUserGifts() {
        if (Utils.getCurrentAccount() != null) {
            return new ResponseEntity<>(giftService.findAllByCurrentUser(), HttpStatus.OK);
        } else
            return ResponseEntity.ok(Collections.EMPTY_LIST);
    }

    @RequestMapping("/user/{usernameOrId}")
    public ResponseEntity getUserGifts(@PathVariable String usernameOrId) {
        Account account = accountService.findByUsername(usernameOrId);
        if (account == null) {
            account = accountService.findById(usernameOrId);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
        }
        //check if anonymous user can view user gifts
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            if (!account.getPublicList()) {
                return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.list.public.error")).build();
            }
        }
        return new ResponseEntity<>(giftService.findAllByUser(account.getId()), HttpStatus.OK);
    }

    @RequestMapping("/categories")
    public ResponseEntity getCategories(@RequestParam(required = false) String term) {
        if (StringUtils.isBlank(term)) {
            return ResponseEntity.ok(categoryRepository.findAll());
        } else {
            return ResponseEntity.ok(categoryRepository.findByNameContainingIgnoreCase(term));
        }
    }

    @RequestMapping(value = "/allowed-category")
    public ResponseEntity checkProhibited(@RequestParam String category) {
        if (!allowedCategoryName(category)) {
            return new ResultData.ResultBuilder().error().message(msgSrv.getMessage("gift.category.prohibited", new Object[]{category}, "", Utils.getCurrentLocale())).build();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean allowedCategoryName(String category) {
        return prohibitedCategories.stream().noneMatch(s -> s.equalsIgnoreCase(category));
    }


    @Transactional
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public ResponseEntity importGifts(@RequestParam(value = "file") MultipartFile importFile, @RequestParam(value = "user", required = false) String username) {
        if (StringUtils.isNotBlank(username) && !canOperateOnUsernameGifts(username)) {
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("user.family.admin.error")).build();
        }
        StringBuilder logger = new StringBuilder();
        Workbook workbook;
        try (InputStream importFileInputStream = importFile.getInputStream()) {
            workbook = WorkbookFactory.create(importFileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            processImportSheet(sheet, logger, username);
        } catch (InvalidFormatException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            LOG.error("Failed to determine excel type");
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("gift.import.wrongType")).build();
        } catch (IOException e) {
            LOG.error("IOException: {}", e);
            return new ResultData.ResultBuilder().badReqest().error().message(msgSrv.getMessage("error.fileIO")).build();
        }
        return new ResultData.ResultBuilder().ok().message(logger.toString()).build();
    }

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
            LOG.error("IOException: {}", e);
        }
    }

    private void processImportSheet(Sheet sheet, StringBuilder logger, String username) {
        int rowNo = 1;//start from row
        while (sheet.getRow(rowNo) != null) {
            Row row = sheet.getRow(rowNo);
            //Get cells
            String cellAddress = "" + COLS.charAt(NAME_CELL) + rowNo;
            Cell nameCell = row.getCell(NAME_CELL);
            Cell descriptionCell = row.getCell(DESCRIPTION_CELL);
            Cell linkCell = row.getCell(LINK_CELL);
            Cell categoryCell = row.getCell(CATEGORY_CELL);
            if (nameCell != null && nameCell.getCellTypeEnum() != CellType.BLANK) {
                Gift newGift = new Gift();
                GiftForm giftForm = new GiftForm();
                giftForm.setUsername(username);
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
                newGift = updateGiftFromForm(giftForm, newGift);
                String added = msgSrv.getMessage("gift.import.added"
                        , new Object[]{rowNo, newGift.getName()}
                        , ""
                        , Utils.getCurrentLocale());
                logger.append(added);
                logger.append(BR);
            } else {
                String notEmpty = msgSrv.getMessage("gift.import.nameEmpty"
                        , new Object[]{rowNo, cellAddress}
                        , ""
                        , Utils.getCurrentLocale());
                logger.append(notEmpty);
                logger.append(BR);
            }
            rowNo++;
        }
    }

    private void setCategoryFromRow(StringBuilder logger, int rowNo, String cellAddress, Cell categoryCell, GiftForm giftForm) {
        String category = categoryCell.getStringCellValue();
        if (allowedCategoryName(category)) {
            giftForm.setCategory(category);
        } else {
            String wrongCategory = msgSrv.getMessage("gift.import.category.prohibited"
                    , new Object[]{rowNo, cellAddress}
                    , ""
                    , Utils.getCurrentLocale());
            logger.append(wrongCategory);
            logger.append(BR);
        }
    }

    private void setLinkFromRow(StringBuilder logger, int rowNo, String cellAddress, Cell linkCell, GiftForm giftForm) {
        String link = linkCell.getStringCellValue();
        if (Utils.validUrlLink(link)) {
            giftForm.setLink(link);
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
