package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TelegramBotSender {
    private final TelegramBot bot;
    private final NotificationTaskRepository repository;

    public TelegramBotSender(TelegramBot bot, NotificationTaskRepository repository) {
        this.bot = bot;
        this.repository = repository;
    }

    public void send(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        SendResponse response = bot.execute(message);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendScheduled() {
        List<NotificationTask> taskList = repository.findByNotificationDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        taskList.forEach(notificationTask -> send(notificationTask.getChatId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm")) + " " + notificationTask.getMessage()));
    }
}
