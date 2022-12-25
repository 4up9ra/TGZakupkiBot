package com.bagrov.tgzakupkibot.service;

import com.bagrov.tgzakupkibot.model.Purchase;
import com.bagrov.tgzakupkibot.model.repository.PurchaseRepository;
import com.bagrov.tgzakupkibot.model.repository.UserRepository;
import com.bagrov.tgzakupkibot.parseelements.ZakupkiElements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class ZakupkiParser {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;


    private static final String BLOCK_ELEMENT = "search-registry-entrys-block";
    private static final String PURCHASE_ELEMENT = "row no-gutters registry-entry__form mr-0";
    public Set<String> searchWords;


    public void doParse(long chatId) {
        String dateFrom = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        searchWords = new HashSet<>(Arrays.asList(userRepository.findById(chatId).get().getKeyWords().split(" ")));

        try {
            for (String searchWord : searchWords) {
                int pageNumber = 1;
                boolean pagesAvailable = true;
                while (pagesAvailable && !searchWord.equals("")) {
                    String url = "https://zakupki.gov.ru/epz/order/extendedsearch/results.html?searchString=" + searchWord + "&morphology=on&search-filter=%D0%94%D0%B0%D1%82%D0%B5+%D1%80%D0%B0%D0%B7%D0%BC%D0%B5%D1%89%D0%B5%D0%BD%D0%B8%D1%8F&pageNumber=" + pageNumber + "&sortDirection=false&recordsPerPage=_10&showLotsInfoHidden=false&sortBy=PUBLISH_DATE&fz223=on&af=on&currencyIdGeneral=-1&publishDateFrom=" + dateFrom;
                    Document document = Jsoup.connect(url).get();
                    Elements outerElements = document.getElementsByAttributeValue("class", BLOCK_ELEMENT);
                    for (Element outerElement : outerElements) {
                        if (outerElement.getElementsByAttributeValue("class", PURCHASE_ELEMENT).isEmpty()) {
                            pagesAvailable = false;
                        }
                        Elements elements = outerElement.getElementsByAttributeValue("class", PURCHASE_ELEMENT);
                        for (Element element : elements) {
                            Purchase purchase = new Purchase();
                            purchase.setType(ZakupkiElements.getType(element));
                            purchase.setPurchaseNumber(ZakupkiElements.getPurchaseNumber(element));
                            purchase.setPurchaseName(ZakupkiElements.getPurchaseName(element));
                            purchase.setLink(ZakupkiElements.getLink(element));
                            purchase.setCustomer(ZakupkiElements.getCustomer(element));
                            purchase.setPrice(ZakupkiElements.getPrice(element));
                            purchase.setStartDate(ZakupkiElements.getStartDate(element));
                            purchase.setCloseDate(ZakupkiElements.getCloseDate(element));

                            purchaseRepository.save(purchase);
                        }
                        pageNumber++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public List<Purchase> showPurchases(Set<String> searchWords)   {

        List<Purchase> result = new ArrayList<>();
        var purchases = purchaseRepository.findPurchasesByStartDate(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        for (Purchase p : purchases)   {
            for (String s : searchWords)    {
                if (p.getPurchaseName().indexOf(s) > 0)    {
                    System.out.println(p.getPurchaseNumber());
                    result.add(p);
                }
            }
        }
        return result;
    }
}

