package com.bagrov.tgzakupkibot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity (name = "ZakupkiUserTable")
public class User {

    @Id
    private Long chatId;

    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registeredAt;
    private String LastPurchaseId;
    private String keyWords;

}
