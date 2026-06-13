package com.devpulse.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Raw push event records for time-series queries.
 * "How many pushes happened this week?" queries this table.
 */
@Entity
@Table(name = "push_activity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushActivity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(name = "repository_full_name" , nullable = false)
    private  String repositoryFullName;

    @Column(name = "sender_login")
    private String senderLogin;

    @Column
    private String branch;

    @Column(name = "commit_count" , nullable = false)
    @Builder.Default
    private  Integer commitCount = 0;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "pushed_at" , nullable = false)
    private LocalDateTime pushedAt;

}
