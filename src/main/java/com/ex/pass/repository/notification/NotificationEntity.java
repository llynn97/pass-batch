package com.ex.pass.repository.notification;


import com.ex.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "notification")
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationSeq;

    private String uuid;

    @Enumerated(EnumType.STRING)
    private NotificationEvent event;

    private String text;
    private boolean sent;
    private LocalDateTime sentAt;
}
