package com.tripyle.repository.board;

import com.tripyle.model.entity.board.Tripyler;
import com.tripyle.model.entity.board.TripylerComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripylerCommentRepository extends JpaRepository<TripylerComment, Long> {
    int countByTripyler(Tripyler tripyler);

    List<TripylerComment> findAllByTripylerOrderByRegDateTimeDesc(Tripyler tripyler);
}
