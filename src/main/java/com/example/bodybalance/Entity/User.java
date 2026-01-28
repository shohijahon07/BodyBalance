package com.example.bodybalance.Entity;

import com.example.bodybalance.Entity.Status;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;
    private String phoneNumber;
    private String userName;
    private Long chatId;
    private String language;
    private String height;
    private String weight;

    @Enumerated(EnumType.STRING)
    private Status status = Status.START;

    private String messageId;

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}
