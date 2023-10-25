package com.tripyle.model.entity.message;

import com.tripyle.common.model.BaseTimeEntity;
import com.tripyle.model.entity.user.User;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "message")
@Where(clause = "delete_yn = 0")
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false, referencedColumnName = "id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false, referencedColumnName = "id")
    private User recipient;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "delete_yn")
    private boolean deleteYn;
}
