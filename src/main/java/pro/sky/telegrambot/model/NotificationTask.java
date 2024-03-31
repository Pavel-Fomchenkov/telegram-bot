package pro.sky.telegrambot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity(name = "notification_tasks")
public class NotificationTask {
    @Id
    @GeneratedValue
    private Long id;
    private Long chatId;
    private LocalDateTime notificationDateTime;
    private String message;

    public NotificationTask() {
    }

    public NotificationTask(Long chatId, LocalDateTime notificationDateTime, String message) {
        this.chatId = chatId;
        this.notificationDateTime = notificationDateTime;
        this.message = message;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }
}
