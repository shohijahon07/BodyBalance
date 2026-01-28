package com.example.bodybalance;

import com.example.bodybalance.Bot.BodyBalanceBot;
import com.example.bodybalance.Repository.UserRepo;
import jakarta.activation.DataHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class BodyBalanceBotApplication {

    public static void main(String[] args) throws TelegramApiException {
        ConfigurableApplicationContext context = SpringApplication.run(BodyBalanceBotApplication.class, args);
        UserRepo userRepo = context.getBean(UserRepo.class);


        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        BodyBalanceBot balanceBot = new BodyBalanceBot(userRepo);
        telegramBotsApi.registerBot(balanceBot);


    }

}
