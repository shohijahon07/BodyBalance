package com.example.bodybalance.Bot;

import com.example.bodybalance.Entity.Status;
import com.example.bodybalance.Entity.User;
import com.example.bodybalance.Repository.UserRepo;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class BodyBalanceBot extends TelegramLongPollingBot {
    private final UserRepo userRepo;

    public BodyBalanceBot(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public String getBotToken() {
        return "BOT_TOKEN";
    }

    @Override
    public String getBotUsername() {
        return "BOT_USERNAME";
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";
        SendMessage sendMessage = new SendMessage();

        // Telegram user
        org.telegram.telegrambots.meta.api.objects.User tgUser = message.getFrom();

        // DB'dan qidiramiz
        User foundUser = userRepo.findByChatId(chatId);

        // Agar user yo‘q bo‘lsa — yaratamiz
        if (foundUser == null) {
            foundUser = new User();
            foundUser.setChatId(chatId);
        }

        // ===== Telegram ma'lumotlarini saqlaymiz =====
        if (tgUser != null) {
            // fullName
            String fullName =
                    (tgUser.getFirstName() != null ? tgUser.getFirstName() : "") +
                            (tgUser.getLastName() != null ? " " + tgUser.getLastName() : "");
            foundUser.setFullName(fullName.trim());

            // username
            foundUser.setUserName(tgUser.getUserName());

        }

        // Agar contact yuborilgan bo‘lsa — phone number saqlaymiz
        if (message.hasContact()) {
            foundUser.setPhoneNumber(message.getContact().getPhoneNumber());
        }

        // Default status
        if (foundUser.getStatus() == null) {
            foundUser.setStatus(Status.HOME_PAGE);
        }

        // DB ga saqlaymiz (create yoki update)
        userRepo.save(foundUser);

        // ===== /start komandasi =====
        if ("/start".equalsIgnoreCase(text) || text.equalsIgnoreCase("🏠 Boshidan boshlash")) {
            sendMessage.setChatId(chatId.toString());
            sendMessage.setText("📏 Menga bo‘yingizni yuboring (masalan: 170)");
            foundUser.setStatus(Status.ADD_HEIGHT);
            userRepo.save(foundUser);
            execute(sendMessage);
        } else if (foundUser.getStatus().equals(Status.ADD_HEIGHT)) {
            if (!text.matches("\\d+(\\.\\d+)?")) {
                sendMessage.setChatId(chatId.toString());
                sendMessage.setText("❗ Iltimos bo‘yingizni faqat SON bilan kiriting.\nMasalan: 170 yoki 170.5");
                execute(sendMessage);
                return; // 🔴 MUHIM: pastga tushmasin
            }

// Agar to‘g‘ri bo‘lsa — saqlaymiz
            foundUser.setHeight(text);


            sendMessage.setText("⚖️ *Endi vazningizni yuboring*, masalan: `70` kg!");
            sendMessage.setChatId(chatId);
            foundUser.setStatus(Status.SHOW_RESULT);
            userRepo.save(foundUser);
            execute(sendMessage);
        } else if (foundUser.getStatus().equals(Status.SHOW_RESULT)) {
            if (!text.matches("\\d+(\\.\\d+)?")) {
                sendMessage.setChatId(chatId.toString());
                sendMessage.setText("❗ Iltimos vazningizni faqat SON bilan kiriting.\nMasalan: 70 yoki 70.5");
                execute(sendMessage);
                return;
            }

            foundUser.setWeight(text);
            userRepo.save(foundUser);

            double weight = Double.parseDouble(foundUser.getWeight());
            double heightCm = Double.parseDouble(foundUser.getHeight());
            double heightM = heightCm / 100;

            double bmi = weight / (heightM * heightM);

            String result;
            double idealMinWeight = 18.5 * heightM * heightM;
            double idealMaxWeight = 24.9 * heightM * heightM;

            if (bmi < 18.5) {
                double needGain = idealMinWeight - weight;
                result = String.format(
                        "📊 BMI: %.2f\n" +
                                "Holat: Ozg‘in ⚠️\n" +
                                "Ideal vaznga yetish uchun ≈ %.1f kg vazn olish kerak",
                        bmi, needGain
                );
            } else if (bmi < 25) {
                result = String.format(
                        "📊 BMI: %.2f\n" +
                                "Holat: Normal vazn ✅\n \n" +
                                "A’lo! Shu holatda davom eting 💪",
                        bmi
                );
            } else if (bmi < 30) {
                double needLose = weight - idealMaxWeight;
                result = String.format(
                        "📊 BMI: %.2f\n" +
                                "Holat: Ortiqcha vazn ⚠️\n \n" +
                                "Ideal vaznga tushish uchun ≈ %.1f kg ozish kerak",
                        bmi, needLose
                );
            } else {
                double needLose = weight - idealMaxWeight;
                result = String.format(
                        "📊 BMI: %.2f\n" +
                                "Holat: Semizlik ❌\n \n" +
                                "Sog‘lom vaznga tushish uchun ≈ %.1f kg ozish kerak",
                        bmi, needLose
                );
            }

            sendMessage.setText(result);
            sendMessage.setReplyMarkup(selectRole());
            sendMessage.setChatId(chatId);
            execute(sendMessage);


        }





























    }

    private ReplyKeyboard selectRole() {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("🏠 Boshidan boshlash"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);

//        foundUser.setStatus(status);
//        userRepo.save(foundUser);

        return markup;
    }

}

