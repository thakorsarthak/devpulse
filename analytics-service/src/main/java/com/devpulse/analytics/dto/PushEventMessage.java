package com.devpulse.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushEventMessage {

    private Long eventId;
    private String repositoryName;
    private String repositoryFullName;
    private String senderLogin;
    private String branch;
    private Integer commitCount;
    private LocalDateTime pushedAt;
}
