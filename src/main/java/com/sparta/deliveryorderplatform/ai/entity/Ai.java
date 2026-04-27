package com.sparta.deliveryorderplatform.ai.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "p_ai_request_log")
public class Ai extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_log_id", columnDefinition = "uuid")
    private UUID aiLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private User user;

    @Column(name = "request_text", nullable = false, length = 100)
    String requestText;

    @Column(name = "response_text", columnDefinition = "TEXT")
    String responseText;

    @Column(name = "request_type", nullable = false, length = 30)
    String requestType;

    public Ai(User user, String requestText, String responseText, String requestType) {
        this.user = user;
        this.requestText = requestText;
        this.responseText = responseText;
        this.requestType = requestType;
    }
}
