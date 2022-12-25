package com.bagrov.tgzakupkibot.model.repository;

import com.bagrov.tgzakupkibot.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;


public interface UserRepository extends CrudRepository<User, Long>{

    @Modifying
    @Transactional
    @Query("UPDATE ZakupkiUserTable u SET u.keyWords = CONCAT(:word, ' ', u.keyWords) WHERE u.chatId=:chatId")
    int setUserKeyWord(String word, long chatId);


    @Modifying
    @Transactional
    @Query("UPDATE ZakupkiUserTable u SET u.keyWords = '' WHERE u.chatId=:chatId")
    int deleteUserKeyWord(long chatId);
}

