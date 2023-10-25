package com.tripyle.repository.board;

import com.tripyle.model.entity.board.Tripyler;
import com.tripyle.model.entity.board.TripylerApply;
import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripylerApplyRepository extends JpaRepository<TripylerApply, Long> {

    List<TripylerApply> findByTripylerId(Long tripylerId); // 트리플러 아이디로 신청된 내역들 불러오기


    TripylerApply findTripylerApplyById(Long id);
    List<TripylerApply> findByTripyler(Tripyler tripyler);

    List<TripylerApply> findByTripylerAndAccepted(Tripyler tripyler, int accepted);


    List<TripylerApply> findByApplicant(User applicant);

    List<TripylerApply> findByApplicantAndAcceptedEquals(User applicant, int accepted);

}
