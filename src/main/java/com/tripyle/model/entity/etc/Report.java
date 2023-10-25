package com.tripyle.model.entity.etc;

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
@Table(name = "report")
@Where(clause = "delete_yn = 0")
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportee_id")
    private User reportee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_reason_id")
    private ReportReason reportReason;

    private String content;

    @Column(name = "delete_yn")
    private boolean deleteYn;
}
