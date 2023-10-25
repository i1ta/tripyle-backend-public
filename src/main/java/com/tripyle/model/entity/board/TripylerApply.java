package com.tripyle.model.entity.board;

import com.tripyle.common.model.BaseTimeEntity;
import com.tripyle.model.entity.user.User;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
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
@Table(name = "tripyler_apply")
@Where(clause = "delete_yn = 0")
public class TripylerApply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripyler_id")
    private Tripyler tripyler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    private String content;

    @Column(name = "accepted")
    @ColumnDefault("0")
    private int accepted;


    @Column(name = "delete_yn")
    private boolean deleteYn;

}
