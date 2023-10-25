package com.tripyle.repository.board;

import com.tripyle.model.entity.board.Review;
import com.tripyle.model.entity.board.ReviewLike;
import com.tripyle.model.entity.board.Tripyler;
import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    ReviewLike findByReviewAndUser(Review review, User user);

    List<ReviewLike> findByUser(User user);

    List<ReviewLike> findByReview(Review review);

    int countByReview(Review review);
}
