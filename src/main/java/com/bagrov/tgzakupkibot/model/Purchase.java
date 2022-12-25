package com.bagrov.tgzakupkibot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "purchaseTable")
public class Purchase {

    @Id
    private String purchaseNumber;

    private String type;
    private String purchaseName;
    private String link;
    private String customer;
    private String price;
    private String startDate;
    private String closeDate;

    @Override
    public String toString() {
        return type + "\n" +
                "№ " + purchaseNumber +
                " " + purchaseName + "\n" +
                link + "\n" + customer + "\n" +
                "НМЦ: " + price + " ₽" + "\n" +
                "Дата размещения: " + startDate + "\n" +
                "Дата закрытия: " + closeDate + "\n" +
                "-----------------------------------------------------------------------\n"
                ;
    }

}
