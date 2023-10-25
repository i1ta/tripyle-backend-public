package com.tripyle.service.board;

import com.tripyle.common.exception.NotFoundException;
import com.tripyle.common.service.S3Service;
import com.tripyle.model.dto.board.ReviewReq;
import com.tripyle.model.dto.board.ReviewRes;
import com.tripyle.model.dto.board.TripylerRes;
import com.tripyle.model.entity.board.*;
import com.tripyle.model.entity.destination.Nation;
import com.tripyle.model.entity.destination.Region;
import com.tripyle.model.entity.hashtag.Hashtag;
import com.tripyle.model.entity.user.User;
import com.tripyle.repository.board.*;
import com.tripyle.repository.destination.ContinentRepository;
import com.tripyle.repository.destination.NationRepository;
import com.tripyle.repository.destination.RegionRepository;
import com.tripyle.repository.hashtag.HashtagRepository;
import com.tripyle.repository.user.UserRepository;
import com.tripyle.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final UserService userService;

    private final S3Service s3Service;

    private final UserRepository userRepository;

    private final ContinentRepository continentRepository;

    private final NationRepository nationRepository;

    private final RegionRepository regionRepository;

    private final HashtagRepository hashtagRepository;

    private final TripylerRepository tripylerRepository;

    private final TripylerLikeRepository tripylerLikeRepository;

    private final TripylerCommentRepository tripylerCommentRepository;

    private final TripylerHashtagRepository tripylerHashtagRepository;

    private final TripylerApplyRepository tripylerApplyRepository;

    private final ReviewRepository reviewRepository;

    private final ReviewCommentRepository reviewCommentRepository;

    private final ReviewLikeRepository reviewLikeRepository;

    private final ReviewImageRepository reviewImageRepository;

    private final TripylerService tripylerService;


    public Review getReviewByReviewId(Long reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if(optionalReview.isEmpty()) {
            throw new NotFoundException("존재하지 않는 후기입니다.");
        }
        return optionalReview.get();
    }


    public Long createReview(Long userId, ReviewReq.ReviewCreateDto reviewCreateDto) {
        Tripyler tripyler = tripylerService.getTripylerByTripylerId(reviewCreateDto.getTripylerId());
        User user = userService.getUserByUserId(userId);

        Review review = Review.builder()
                .tripyler(tripyler)
                .writer(user)
                .title(reviewCreateDto.getTitle())
                .content(reviewCreateDto.getContent())
                .oneLine(reviewCreateDto.getOneLine())
                .build();
        reviewRepository.save(review);
        return review.getId();
    }

    public void uploadReviewImage(Long reviewId, MultipartFile multipartFile) {
        Review review = getReviewByReviewId(reviewId);
        String url = s3Service.uploadImage("review", review.getId().toString(), multipartFile);

        ReviewImage e = ReviewImage.builder()
                .review(review)
                .url(url)
                .build();

        reviewImageRepository.save(e);
    }



    public List<ReviewRes.CommentRes> getReviewComments(Long reviewId) {
        List<ReviewRes.CommentRes> result = new ArrayList<>();
        Review review = getReviewByReviewId(reviewId);
        List<ReviewComment> reviewComments = reviewCommentRepository.findAllByReviewOrderByRegDateTimeDesc(review);

        for(ReviewComment reviewComment : reviewComments){
            ReviewRes.CommentRes e = ReviewRes.CommentRes.builder()
                    .userId(reviewComment.getCommenter().getId())
                    .nickname(reviewComment.getCommenter().getNickname())
                    .content(reviewComment.getContent())
                    .build();
            result.add(e);
        }

        return result;
    }

    public void comment(Long reviewId, Long userId, String content) {
        User user = userService.getUserByUserId(userId);
        Review review = getReviewByReviewId(reviewId);
        ReviewComment reviewComment = ReviewComment.builder()
                .review(review)
                .commenter(user)
                .content(content)
                .build();
        reviewCommentRepository.save(reviewComment);
    }

    public void like(Long reviewId, Long userId) {
        User user = userService.getUserByUserId(userId);
        Review review = getReviewByReviewId(reviewId);
        Optional<ReviewLike> optionalReviewLike = Optional.ofNullable(reviewLikeRepository.findByReviewAndUser(review, user));

        if(optionalReviewLike.isPresent()) {
            ReviewLike reviewLike = optionalReviewLike.get();
            reviewLike.setDeleteYn(true);
        }
        else {
            ReviewLike reviewLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(reviewLike);
        }
    }

    public ReviewRes.ReviewDetailRes getReviewDetail(Long tokenUserId, Long reviewId) {
        Review review = getReviewByReviewId(reviewId);
        Tripyler tripyler = review.getTripyler();
        List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByTripyler(tripyler);


        String nationName;
        try {
            nationName = tripyler.getNation().getName();
        }
        catch(NullPointerException e) {
            nationName = null;
        }

        String regionName;
        try {
            regionName = tripyler.getRegion().getName();
        }
        catch(NullPointerException e) {
            regionName = null;
        }


        //좋아요, 댓글수, 같이 간 사람 리스트, 리뷰 이미지 리스트,
        int likes = reviewLikeRepository.countByReview(review);
        int commentsCnt = reviewCommentRepository.countByReview(review);

        List<String> reviewImageList = new ArrayList<>();
        List<ReviewImage> reviewImages = reviewImageRepository.findAllByReview(review);
        for(ReviewImage reviewImage : reviewImages){
            String url = reviewImage.getUrl();
            reviewImageList.add(url);
        }



        //동행한 사람 리스트
        List<ReviewRes.TripylerWith> tripylerWithList = new ArrayList<>();
        List<TripylerApply> tripylerApplyList = tripylerApplyRepository.findByTripyler(tripyler);

        for(TripylerApply tripylerApply : tripylerApplyList){
            if(tripylerApply.getAccepted() == 1){
                int age;
                User applicant = tripylerApply.getApplicant();
                if(applicant.getBirthDate() == null || applicant.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
                    age = 0;
                } else {
                    age = LocalDate.now().getYear() - applicant.getBirthDate().getYear() + 1;
                }

                ReviewRes.TripylerWith e = ReviewRes.TripylerWith.builder()
                        .userId(applicant.getId())
                        .profileUrl(applicant.getProfileUrl())
                        .nickname(applicant.getNickname())
                        .age(age)
                        .gender(applicant.getGender())
                        .build();

                tripylerWithList.add(e);
            }
        }


        //리턴 객체 생성(수동으로 set)
        ReviewRes.ReviewDetailRes result = ReviewRes.ReviewDetailRes.toDto(review);
        result.setNationName(nationName);
        result.setRegionName(regionName);
        result.setLikes(likes);
        result.setCommentsCnt(commentsCnt);
        result.setReviewImageList(reviewImageList);
        result.setTripylerWithList(tripylerWithList);

        result.setMyReview(review.getWriter().getId().equals(tokenUserId));



        //토큰을 가지고 접근한 유저가 좋아요를 눌렀는지 안눌렀는지
        List<ReviewLike> reviewLikeList = reviewLikeRepository.findByReview(review);
        for(ReviewLike reviewLike : reviewLikeList){
            if(reviewLike.getUser().getId().equals(tokenUserId)){
                result.setTokenUserLiked(true);
                break;
            }
        }








        //해시태그 불러오기
        if(tripylerHashtags.size() == 5){
            result.setHashtag1(tripylerHashtags.get(0).getHashtag().getName());
            result.setHashtag2(tripylerHashtags.get(1).getHashtag().getName());
            result.setHashtag3(tripylerHashtags.get(2).getHashtag().getName());
            result.setHashtag4(tripylerHashtags.get(3).getHashtag().getName());
            result.setHashtag5(tripylerHashtags.get(4).getHashtag().getName());
        }
        else if(tripylerHashtags.size() == 4){
            result.setHashtag1(tripylerHashtags.get(0).getHashtag().getName());
            result.setHashtag2(tripylerHashtags.get(1).getHashtag().getName());
            result.setHashtag3(tripylerHashtags.get(2).getHashtag().getName());
            result.setHashtag4(tripylerHashtags.get(3).getHashtag().getName());
        }
        else if(tripylerHashtags.size() == 3){
            result.setHashtag1(tripylerHashtags.get(0).getHashtag().getName());
            result.setHashtag2(tripylerHashtags.get(1).getHashtag().getName());
            result.setHashtag3(tripylerHashtags.get(2).getHashtag().getName());
        }
        else if(tripylerHashtags.size() == 2){
            result.setHashtag1(tripylerHashtags.get(0).getHashtag().getName());
            result.setHashtag2(tripylerHashtags.get(1).getHashtag().getName());
        }
        else if(tripylerHashtags.size() == 1){
            result.setHashtag1(tripylerHashtags.get(0).getHashtag().getName());
        }




        //이전, 다음 게시글 정보
        List<Review> reviewList = reviewRepository.findAll();

        for(Review r : reviewList){
            int nowIdx = reviewList.indexOf(r);
            if(r.getId().equals(reviewId)){
                if(nowIdx == 0){
                    Review next = reviewList.get(nowIdx+1);

                    result.setNextReviewId(next.getId());
                    result.setNextTitle(next.getTitle());
                } else if(nowIdx == reviewList.size()-1){
                    Review previous = reviewList.get(nowIdx-1);

                    result.setPreviousReviewId(previous.getId());
                    result.setPreviousTitle(previous.getTitle());
                } else{
                    Review previous = reviewList.get(nowIdx-1);
                    Review next = reviewList.get(nowIdx+1);

                    result.setPreviousReviewId(previous.getId());
                    result.setPreviousTitle(previous.getTitle());
                    result.setNextReviewId(next.getId());
                    result.setNextTitle(next.getTitle());
                }
                break;
            }
        }






        //조회수 1상승
        reviewRepository.incrementHits(reviewId);

        return result;
    }

    public List<TripylerRes.MyTripylerApplyListDto> findReviewByLike(Long userId) {
        User user = userService.getUserByUserId(userId);
        List<ReviewLike> reviewLikes = reviewLikeRepository.findByUser(user);
        List<TripylerRes.MyTripylerApplyListDto> myTripylerApplyListDtos = new ArrayList<>();

        for(ReviewLike reviewLike : reviewLikes) {
            Tripyler tripyler = reviewLike.getReview().getTripyler();

            Nation nation = tripyler.getNation();
            String nationName = nation != null ? nation.getName() : null;
            Region region = tripyler.getRegion();
            String regionName = region != null ? region.getName() : null;

            List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByTripyler(tripyler);

            String imageUrl;
            if(tripyler.getImage() == null) {
                imageUrl = regionName == null ? null : tripyler.getRegion().getImageUrl();
            }
            else {
                imageUrl = tripyler.getImage();
            }

            myTripylerApplyListDtos.add(TripylerRes.MyTripylerApplyListDto.builder()
                            .userId(tripyler.getWriter().getId())
                            .tripylerId(reviewLike.getReview().getId())
                            .nationName(nationName)
                            .regionName(regionName)
                            .startDate(tripyler.getStartDate())
                            .endDate(tripyler.getEndDate())
                            .totalPeopleNum(tripyler.getTotalPeopleNum())
                            .likes(tripylerLikeRepository.countByTripyler(tripyler))
                            .comments(tripylerCommentRepository.countByTripyler(tripyler))
                            .hashtag1(tripylerHashtags.get(0).getHashtag().getName())
                            .hashtag2(tripylerHashtags.get(1).getHashtag().getName())
                            .hashtag3(tripylerHashtags.get(2).getHashtag().getName())
                            .hashtag4(tripylerHashtags.get(3).getHashtag().getName())
                            .hashtag5(tripylerHashtags.get(4).getHashtag().getName())
                            .imageUrl(imageUrl)
                            .build());
        }
        return myTripylerApplyListDtos;
    }

    public List<ReviewRes.MyReviewListDto> myReviewWithYear(int year, Long userId) {
        List<ReviewRes.MyReviewListDto> myReviewListDtos = new ArrayList<>();
        List<Review> reviews = reviewRepository.findByYearAndUserId(year, userId);
        for(Review review : reviews) {
            Tripyler tripyler = review.getTripyler();
            Nation nation = tripyler.getNation();
            String nationName = nation != null ? nation.getName() : null;
            Region region = tripyler.getRegion();
            String regionName = region != null ? region.getName() : null;

            List<ReviewImage> reviewImages = reviewImageRepository.findAllByReview(review);
            List<String> imageUrls = new ArrayList<>();
            for(ReviewImage reviewImage : reviewImages) {
                imageUrls.add(reviewImage.getUrl());
            }

            myReviewListDtos.add(ReviewRes.MyReviewListDto.builder()
                    .reviewId(review.getId())
                    .nationName(nationName)
                    .regionName(regionName)
                    .tripylerTitle(tripyler.getTitle())
                    .reviewTitle(review.getTitle())
                    .likes(tripylerLikeRepository.countByTripyler(tripyler))
                    .comments(tripylerCommentRepository.countByTripyler(tripyler))
                    .hits(review.getHits())
                    .startDate(tripyler.getStartDate())
                    .endDate(tripyler.getEndDate())
                    .imageUrls(imageUrls)
                    .build());
        }
        return myReviewListDtos;
    }

    public List<Review> getReviewList(ReviewReq.ReviewOptionDto reviewOptionDto) {
        Long continentId = reviewOptionDto.getContinentId();
        if(continentId.equals(0L)) {
            return reviewRepository.findAll();
        }

        List<Review> tempReviews = new ArrayList<>();
        List<Review> returnReviews = new ArrayList<>();

        List<Tripyler> tripylers;
        List<Tripyler> returnTripylers = new ArrayList<>();

        Long nationId = null;
        if (reviewOptionDto.getNationId() != null) {
            Nation nation = nationRepository.findNationById(reviewOptionDto.getNationId());
            if (nation != null) {
                nationId = nation.getId();
            }
        }

        Long regionId = null;
        if (reviewOptionDto.getRegionId() != null) {
            Region region = regionRepository.findRegionById(reviewOptionDto.getRegionId());
            if (region != null) {
                regionId = region.getId();
            }
        }

        int startMonth = reviewOptionDto.getStartMonth();
        int endMonth = reviewOptionDto.getEndMonth();
        int totalNum = reviewOptionDto.getTotalPeopleNum();
        String keyword = reviewOptionDto.getKeyWord();

        List<Hashtag> hashtags = hashtagRepository.findByNameContains(keyword); // 키워드가 포함된 해시태그 객체를 불러옴

        List<Tripyler> tripylersWithKeyword = new ArrayList<>();


        for (Hashtag hashtag : hashtags) {
            List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByHashtag(hashtag);
            for (TripylerHashtag tripylerHashtag : tripylerHashtags) {
                Tripyler tripyler = tripylerHashtag.getTripyler();
                tripylersWithKeyword.add(tripyler);
            }
        }

        if (nationId != null && regionId != null) {
            tripylers = tripylerRepository.findByContinentIdAndNationIdAndRegionId(continentId, nationId, regionId);
        } else if (nationId != null) {
            tripylers = tripylerRepository.findByContinentIdAndNationId(continentId, nationId);
        } else {
            tripylers = tripylerRepository.findByContinentId(continentId);
        }

        for (Tripyler tripyler : tripylers) {
            int tripStartMonthValue = tripyler.getStartDate().getMonthValue();
            int tripEndMonthValue = tripyler.getEndDate().getMonthValue();
            if (tripStartMonthValue >= startMonth && tripEndMonthValue <= endMonth &&
                    tripyler.getTotalPeopleNum() == totalNum) {
                returnTripylers.add(tripyler);
            }
        }


        for (Tripyler tripyler : returnTripylers) {
            List<Review> reviewList = reviewRepository.findByTripyler(tripyler);
            tempReviews.addAll(reviewList);
        }

        for (Review review : tempReviews) {
            if (review.getTitle().contains(keyword) ||
                    review.getContent().contains(keyword)) {
                returnReviews.add(review);
            }
        }

        return returnReviews;
    }

    public List<ReviewRes.ReviewListDtoOrderByRegDateTime> getReviewListOrderByRegDateTime(List<Review> reviews) {
        List<ReviewRes.ReviewListDtoOrderByRegDateTime> reviewList = new ArrayList<>();
        for(Review review: reviews){
            Tripyler tripyler = tripylerRepository.findTripylerById(review.getTripyler().getId());
            List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByTripyler(tripyler);
            List<String> hashtagList = new ArrayList<>();
            for (TripylerHashtag tripylerHashtag: tripylerHashtags){
                hashtagList.add(tripylerHashtag.getHashtag().getName());
            }
            User writer = review.getWriter();
            int age;
            if(writer.getBirthDate() == null || writer.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
                age = 0;
            } else {
                age = LocalDate.now().getYear() - writer.getBirthDate().getYear() + 1;
            }

            String regionName = tripyler.getRegion() != null ? tripyler.getRegion().getName() : null;
            String imageUrl;
            if(tripyler.getImage() == null) {
                imageUrl = regionName == null ? null : tripyler.getRegion().getImageUrl();
            }
            else {
                imageUrl = tripyler.getImage();
            }
            reviewList.add(ReviewRes.ReviewListDtoOrderByRegDateTime.builder()
                    .reviewId(review.getId())
                    .nationName(tripyler.getNation().getName())
                    .regionName(regionName)
                    .title(review.getTitle())
                    .content(review.getContent())
                    .image(imageUrl)
                    .hits(review.getHits())
                    .hashtags(hashtagList)
                    .regDateTime(review.getRegDateTime())
                    .likes(reviewLikeRepository.countByReview(review))
                    .comments(reviewCommentRepository.countByReview(review))
                    .userProfileUrl(writer.getProfileUrl())
                    .username(writer.getUsername())
                    .age(age)
                    .gender(writer.getGender())
                    .startDate(tripyler.getStartDate())
                    .endDate(tripyler.getEndDate())
                    .build());
        }
        Collections.sort(reviewList);
        return reviewList;
    }

    public List<ReviewRes.ReviewListDtoOrderByLikes> getReviewListOrderByLikes(List<Review> reviews) {
        List<ReviewRes.ReviewListDtoOrderByLikes> reviewList = new ArrayList<>();
        for(Review review: reviews){
            Tripyler tripyler = tripylerRepository.findTripylerById(review.getTripyler().getId());
            List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByTripyler(tripyler);
            List<String> hashtagList = new ArrayList<>();
            for (TripylerHashtag tripylerHashtag: tripylerHashtags){
                hashtagList.add(tripylerHashtag.getHashtag().getName());
            }
            User writer = review.getWriter();
            int age;
            if(writer.getBirthDate() == null || writer.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
                age = 0;
            } else {
                age = LocalDate.now().getYear() - writer.getBirthDate().getYear() + 1;
            }
            String regionName = tripyler.getRegion() != null ? tripyler.getRegion().getName() : null;
            String imageUrl;
            if(tripyler.getImage() == null) {
                imageUrl = regionName == null ? null : tripyler.getRegion().getImageUrl();
            }
            else {
                imageUrl = tripyler.getImage();
            }
            reviewList.add(ReviewRes.ReviewListDtoOrderByLikes.builder()
                    .reviewId(review.getId())
                    .nationName(tripyler.getNation().getName())
                    .regionName(regionName)
                    .title(review.getTitle())
                    .content(review.getContent())
                    .image(imageUrl)
                    .hits(review.getHits())
                    .hashtags(hashtagList)
                    .regDateTime(review.getRegDateTime())
                    .likes(reviewLikeRepository.countByReview(review))
                    .comments(reviewCommentRepository.countByReview(review))
                    .userProfileUrl(writer.getProfileUrl())
                    .username(writer.getUsername())
                    .age(age)
                    .gender(writer.getGender())
                    .startDate(tripyler.getStartDate())
                    .endDate(tripyler.getEndDate())
                    .build());
        }
        Collections.sort(reviewList);
        return reviewList;
    }

    public List<ReviewRes.ReviewListDtoOrderByComments> getReviewListOrderByComments(List<Review> reviews) {
        List<ReviewRes.ReviewListDtoOrderByComments> reviewList = new ArrayList<>();
        for(Review review: reviews){
            Tripyler tripyler = tripylerRepository.findTripylerById(review.getTripyler().getId());
            List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByTripyler(tripyler);
            List<String> hashtagList = new ArrayList<>();
            for (TripylerHashtag tripylerHashtag: tripylerHashtags){
                hashtagList.add(tripylerHashtag.getHashtag().getName());
            }
            User writer = review.getWriter();
            int age;
            if(writer.getBirthDate() == null || writer.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
                age = 0;
            } else {
                age = LocalDate.now().getYear() - writer.getBirthDate().getYear() + 1;
            }
            String regionName = tripyler.getRegion() != null ? tripyler.getRegion().getName() : null;
            String imageUrl;
            if(tripyler.getImage() == null) {
                imageUrl = regionName == null ? null : tripyler.getRegion().getImageUrl();
            }
            else {
                imageUrl = tripyler.getImage();
            }
            reviewList.add(ReviewRes.ReviewListDtoOrderByComments.builder()
                    .reviewId(review.getId())
                    .nationName(regionName)
                    .regionName(tripyler.getRegion().getName())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .image(imageUrl)
                    .hits(review.getHits())
                    .hashtags(hashtagList)
                    .regDateTime(review.getRegDateTime())
                    .likes(reviewLikeRepository.countByReview(review))
                    .comments(reviewCommentRepository.countByReview(review))
                    .userProfileUrl(writer.getProfileUrl())
                    .username(writer.getUsername())
                    .age(age)
                    .gender(writer.getGender())
                    .startDate(tripyler.getStartDate())
                    .endDate(tripyler.getEndDate())
                    .build());
        }
        Collections.sort(reviewList);
        return reviewList;
    }

    public List<ReviewRes.ReviewListDtoOrderByHits> getReviewListOrderByHits(List<Review> reviews) {
        List<ReviewRes.ReviewListDtoOrderByHits> reviewList = new ArrayList<>();
        for(Review review: reviews){
            Tripyler tripyler = tripylerRepository.findTripylerById(review.getTripyler().getId());
            List<TripylerHashtag> tripylerHashtags = tripylerHashtagRepository.findByTripyler(tripyler);
            List<String> hashtagList = new ArrayList<>();
            for (TripylerHashtag tripylerHashtag: tripylerHashtags){
                hashtagList.add(tripylerHashtag.getHashtag().getName());
            }
            User writer = review.getWriter();
            int age;
            if(writer.getBirthDate() == null || writer.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
                age = 0;
            } else {
                age = LocalDate.now().getYear() - writer.getBirthDate().getYear() + 1;
            }
            String regionName = tripyler.getRegion() != null ? tripyler.getRegion().getName() : null;
            String imageUrl;
            if(tripyler.getImage() == null) {
                imageUrl = regionName == null ? null : tripyler.getRegion().getImageUrl();
            }
            else {
                imageUrl = tripyler.getImage();
            }
            reviewList.add(ReviewRes.ReviewListDtoOrderByHits.builder()
                    .reviewId(review.getId())
                    .nationName(tripyler.getNation().getName())
                    .regionName(regionName)
                    .title(review.getTitle())
                    .content(review.getContent())
                    .image(imageUrl)
                    .hits(review.getHits())
                    .hashtags(hashtagList)
                    .regDateTime(review.getRegDateTime())
                    .likes(reviewLikeRepository.countByReview(review))
                    .comments(reviewCommentRepository.countByReview(review))
                    .userProfileUrl(writer.getProfileUrl())
                    .username(writer.getUsername())
                    .age(age)
                    .gender(writer.getGender())
                    .startDate(tripyler.getStartDate())
                    .endDate(tripyler.getEndDate())
                    .build());
        }
        Collections.sort(reviewList);
        return reviewList;
    }
}
