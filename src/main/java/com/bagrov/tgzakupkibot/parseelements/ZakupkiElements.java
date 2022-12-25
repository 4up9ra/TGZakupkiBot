package com.bagrov.tgzakupkibot.parseelements;
import org.jsoup.nodes.Element;

public class ZakupkiElements {
    public static String getCloseDate(Element element) {
        return element.getElementsByAttributeValue("class", "data-block__value").get(2).text();
    }

    public static String getCustomer(Element element) {
        return element.getElementsByAttributeValue("target", "_blank").get(2).text();
    }

    public static String getLink(Element element) {
        return "https://zakupki.gov.ru" +
                element.getElementsByAttributeValue("target", "_blank").get(1).attr("href");
    }

    public static String getPrice(Element element) {
        if (element.getElementsByAttributeValue("class", "price-block__value").text().isEmpty()){
            return "0";
        }
        return element.getElementsByAttributeValue("class", "price-block__value").text()
                .replaceAll(" ", "").replaceAll("₽", "");
    }

    public static String getPurchaseName(Element element) {
        String name = element.getElementsByAttributeValue("class", "registry-entry__body-value").text();
        if (name.length() > 255)    {
            name = name.substring(0, 255);
        }
        return name;
    }

    public static String getPurchaseNumber(Element element) {
        return element.getElementsByAttributeValue("class", "registry-entry__header-mid__number").text().replaceAll("№", "").trim();
    }

    public static String getStartDate(Element element) {
        return element.getElementsByAttributeValue("class", "data-block__value").get(0).text();
    }

    public static String getType(Element element) {
        return element.getElementsByAttributeValue("class", "col-9 p-0 registry-entry__header-top__title text-truncate").text();
    }
}