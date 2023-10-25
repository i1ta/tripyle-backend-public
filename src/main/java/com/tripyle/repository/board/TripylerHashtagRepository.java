package com.tripyle.repository.board;

import com.tripyle.model.entity.board.Tripyler;
import com.tripyle.model.entity.board.TripylerHashtag;
import com.tripyle.model.entity.hashtag.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripylerHashtagRepository extends JpaRepository<TripylerHashtag, Long> {

    List<TripylerHashtag> findByHashtag(Hashtag hashtag);

    List<TripylerHashtag> findByTripyler(Tripyler tripyler);

    TripylerHashtag findByTripylerAndHashtag_Id(Tripyler tripyler, Long hashtagId);

}
