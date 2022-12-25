package com.bagrov.tgzakupkibot.service;

import com.bagrov.tgzakupkibot.config.BotConfig;
import com.bagrov.tgzakupkibot.model.Purchase;
import com.bagrov.tgzakupkibot.model.User;
import com.bagrov.tgzakupkibot.model.repository.UserRepository;
import com.bagrov.tgzakupkibot.model.repository.PurchaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    final BotConfig botConfig;
    final ZakupkiParser zakupkiParser;


    public TelegramBot(BotConfig botConfig, ZakupkiParser zakupkiParser) {
        this.botConfig = botConfig;
        this.zakupkiParser = zakupkiParser;
        List<BotCommand> listOfCommands = new ArrayList<>();//создани меню у бота
        listOfCommands.add(new BotCommand("/start", "Начать работу с ботом"));
        listOfCommands.add(new BotCommand("/help", "Получить информацию по работе бота"));
        listOfCommands.add(new BotCommand("/add", "Для добавления ключевого слова наберите /add \"слово\" (или ключевые слова через запятую)"));
        listOfCommands.add(new BotCommand("/keywords", "Показать используемые ключевые слова"));
        listOfCommands.add(new BotCommand("/delete", "Для удаления всех ключевых слов наберите /delete"));
        listOfCommands.add(new BotCommand("/show", "Показать закупки по ключевым словам от " + new SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis())));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            long chatId = message.getChatId();
            if (message.getText().startsWith("/add ")) {
                userRepository.setUserKeyWord(message.getText().substring(message.getText().indexOf(" ")), chatId);
                sendMessage(chatId, "<" + message.getText().substring(message.getText().indexOf(" ")) + "> добавлено в список ключевых слов");
            } else if (message.getText().startsWith("/delete")) {
                userRepository.deleteUserKeyWord(chatId);
                sendMessage(chatId, "Список ключевых слов пуст");
            } else {
                switch (message.getText()) {
                    case "/start":
                        if (userRepository.findById(chatId).isEmpty()) {
                            registerUser(message);
                            sendMessage(chatId, "Привет, " + message.getChat().getFirstName() + "! Меня зовут ZakupkiBot! " +
                                    "Я помогу тебе отслеживать появление новых закупок на сайте zakupki.gov.ru. " +
                                    "Для того чтобы узнать что я могу, набери команду /help");
                        } else sendMessage(chatId, "Я уже работаю на тебя");
                        break;
                    case "/help":
                        sendMessage(chatId, "Для запуска бота введите команду /start\n" +
                                "Для добавления ключевого слова введите команду /add и через пробел введите ключевые слова\n" +
                                "Для удаления всех ключевых слов из списка введите команду /delete\n" +
                                "Для просмотра закупок по ключевым словам от текущей даты введите команду /show\n" +
                                "Для просмотра используемых ключевых слов введите команду /keywords\n");
                        break;
                    case "/show":
                        sendMessage(chatId, "Закупки от " + new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
                        showPurchases(chatId);
                        break;
                    case "/keywords":
                        if (userRepository.findById(chatId).get().getKeyWords().isEmpty()) {
                            sendMessage(chatId, "Список ключевых слов пуст");
                        } else {
                            sendMessage(chatId, userRepository.findById(chatId).get().getKeyWords());
                        }
                        break;
                    default:
                        sendMessage(chatId, "Sorry i dont know any of these words, try /help");
                }
            }
        }
    }

    public void sendMessage(long chatId, String answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(answer);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred from sendMessage: " + e.getMessage());
        }
    }

    private void registerUser(Message message) {
        long chatId = message.getChatId();//присваиваем ему id
        Chat chat = message.getChat();//присваиваем ему чат

        User user = new User();//создаем юзера
        user.setChatId(chatId);//присваиваем поля из конструктора
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        user.setLastPurchaseId("");
        user.setKeyWords("");

        userRepository.save(user);//сохраняем юзера в таблицу БД
        log.info("User saved " + user);
    }

    private void showPurchases(long chatId) {
        zakupkiParser.doParse(chatId);
        List<Purchase> list = zakupkiParser.showPurchases(zakupkiParser.searchWords);
        for (int i = 1; i <= list.size(); i++) {
            sendMessage(chatId, i + " - " + list.get(i - 1));
        }
    }

    @Scheduled(cron = "${cron.doparse.scheduler}")
    private void doParseJob() {
        var users = userRepository.findAll();
        for (User u : users) {
            if (!u.getKeyWords().equals("")) {
                zakupkiParser.doParse(u.getChatId());
            }
        }
    }

    @Scheduled(cron = "${cron.showpurchases.scheduler}")
    private void showPurchasesJob() {
        var users = userRepository.findAll();
        var purchases = purchaseRepository.findPurchasesByStartDate(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        for (User u : users) {
            String[] words = u.getKeyWords().split(" ");
            for (String word : words) {
                for (Purchase p : purchases) {
                    if (p.getPurchaseName().indexOf(word) > 0) {
                        sendMessage(u.getChatId(), p.toString());
                    }
                }
            }
        }
    }
}
