package com.tripyle.repository.board;

import com.tripyle.model.entity.board.Review;
import com.tripyle.model.entity.board.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    List<ReviewComment> findAllByReviewOrderByRegDateTimeDesc(Review review);

    int countByReview(Review review);
}
