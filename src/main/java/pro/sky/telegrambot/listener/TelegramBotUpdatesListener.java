package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.TelegramBotSender;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final TelegramBotSender telegramBotSender;
    private final NotificationTaskRepository repository;
    // "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)" - another regexp, but it can mismatch DATE_TIME_PATTERN
    private final String INPUT_NOTIFICATION_PATTERN = "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)";
    private final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
    private final String WELCOMING_TEXT = "Привет! Я бот для создания уведомлений.\nДобавить уведомление можно сообщением:\n24.03.2024 20:55 Текст уведомления";
    private final String NOTIFICATION_ADDED = "Уведомление успешно добавлено.";
    private final String LOGGING_MESSAGE = "Received message: {}";
    private final String NOTIFICATION_MISMATCH = "Уведомление не соответствует шаблону -\nДД.ММ.ГГГГ ЧЧ:ММ Текст уведомления";
    private final String LOGGING_MISMATCH = "Received message: {} does not matches the pattern";

    public TelegramBotUpdatesListener(TelegramBot telegramBot, TelegramBotSender telegramBotSender, NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.telegramBotSender = telegramBotSender;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info(LOGGING_MESSAGE, update);
            // Process your updates here
            String message = update.message().text();
            Long chatId = update.message().chat().id();
            Pattern pattern = Pattern.compile(INPUT_NOTIFICATION_PATTERN);
            Matcher matcher = pattern.matcher(message);

            if (message.equals("/start") || message.equals("/help")) {
                telegramBotSender.send(chatId, WELCOMING_TEXT);

            } else if (matcher.matches()) {
                String date = matcher.group(1);
                String item = matcher.group(3);

                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                    if (localDateTime.isBefore(LocalDateTime.now()))
                        throw new DateTimeException("Дата и время не должны быть в прошлом");
                    NotificationTask task = new NotificationTask(chatId, localDateTime, item);
                    repository.save(task);
                    telegramBotSender.send(chatId, NOTIFICATION_ADDED);

                } catch (DateTimeParseException e) {
                    logger.info(LOGGING_MISMATCH, update.message().text());
                    telegramBotSender.send(chatId, NOTIFICATION_MISMATCH);

                } catch (DateTimeException e) {
                    logger.info(LOGGING_MISMATCH + " " + e.getMessage(), update.message().text());
                    telegramBotSender.send(chatId, NOTIFICATION_MISMATCH + " " + e.getMessage());
                }
            } else {
                logger.info(LOGGING_MISMATCH, update.message().text());
                telegramBotSender.send(chatId, NOTIFICATION_MISMATCH);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
