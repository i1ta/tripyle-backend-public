package com.tripyle.repository.board;

import com.tripyle.model.entity.board.Review;
import com.tripyle.model.entity.board.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findAllByReview(Review review);

}
