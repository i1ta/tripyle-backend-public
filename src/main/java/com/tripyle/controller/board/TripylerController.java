package com.tripyle.controller.board;

import com.tripyle.common.exception.BadRequestException;
import com.tripyle.common.exception.NotFoundException;
import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.model.dto.board.TripylerReq;
import com.tripyle.model.dto.board.TripylerRes;
import com.tripyle.model.entity.board.Tripyler;
import com.tripyle.model.entity.board.TripylerLike;
import com.tripyle.repository.board.TripylerLikeRepository;
import com.tripyle.service.board.TripylerService;
import com.tripyle.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(tags={"07.Tripyler"})
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/tripyler", produces = MediaType.APPLICATION_JSON_VALUE)
public class TripylerController {
    private final TripylerService tripylerService;
    private final UserService userService;

    @ApiOperation(value = "게시물 한 개 조회", notes = "게시물 한 개 조회")
    @GetMapping("/{tripylerId}")
    public HttpRes<TripylerRes.BoardDetailRes> getTripylerBoardDetail(@PathVariable Long tripylerId, HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        TripylerRes.BoardDetailRes result = tripylerService.getTripylerBoardDetail(userId, tripylerId);
        return new HttpRes<>(result);
    }

    @ApiOperation(value = "모든 모집 게시글 보기(필터링)", notes = "모집중(1), 모집 완료(2) / 최신순(1), 좋아요순(2), 댓글순(3), 조회순(4)")
    @PostMapping("/list")
    public Object getTripylerBoard(@RequestBody TripylerReq.BoardOptionDto boardOptionDto,
                                   @RequestParam(name = "isRecruiting") int isRecruiting,
                                   @RequestParam(name = "option") int option) {
        List<Tripyler> tripylers = tripylerService.getTripylerList(boardOptionDto, isRecruiting);

        //최신순(1), 좋아요순(2), 댓글순(3), 조회순(4)
        if (option == 1) {
            return new HttpRes<>(tripylerService.getTripylerListOrderByRegDateTime(tripylers));
        }
        else if (option == 2) {
            return new HttpRes<>(tripylerService.getTripylerListOrderByLikes(tripylers));
        }
        else if (option == 3) {
            return new HttpRes<>(tripylerService.getTripylerListOrderByComments(tripylers));
        }
        else if (option == 4) {
            return new HttpRes<>(tripylerService.getTripylerListOrderByHits(tripylers));
        }

        throw new BadRequestException("isRecruiting 또는 option의 값이 유효하지 않습니다.");
    }

    @ApiOperation(value = "여행자 찾기 게시물 작성", notes = "여행자 찾기 게시물을 작성합니다.\n" +
            "body에 json을 넣을 땐 \"application/json\"으로,\n" +
            "이미지 파일을 넣을 땐 \"image/png\" 또는 \"image/jpeg\"로\n" +
            "Content Type을 따로 지정해서 API 호출 해야 함\n\n" +
            "body 예시:\n" +
            "{\n" +
            "  \"content\": \"내용내용 동행자 찾는 내용\",\n" +
            "  \"continentId\": 5,\n" +
            "  \"endDate\": \"2023-07-22\",\n" +
            "  \"fifthTripStyleId\": 1,\n" +
            "  \"firstTripStyleId\": 2,\n" +
            "  \"fourthTripStyleId\": 3,\n" +
            "  \"nationId\": 46,\n" +
            "  \"regionId\": 199,\n" +
            "  \"secondTripStyleId\": 5,\n" +
            "  \"startDate\": \"2023-07-21\",\n" +
            "  \"thirdTripStyleId\": 4,\n" +
            "  \"title\": \"동행자 찾아요\",\n" +
            "  \"totalPeopleNum\": 4\n" +
            "}")
    @PostMapping(value = "", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public HttpRes<String> createTripyler(@RequestPart(name = "tripyler") TripylerReq.TripylerCreateDto tripylerCreateDto,
                                          @RequestPart(name = "images", required = false) MultipartFile multipartFile,
                                          HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        tripylerService.createTripyler(userId, tripylerCreateDto, multipartFile);
        return new HttpRes<>("게시물 등록이 성공적으로 완료되었습니다.");
    }

    @ApiOperation(value = "여행자 찾기 좋아요 기능", notes = "여행자 찾기 게시물에 좋아요를 누릅니다.")
    @PostMapping("/like")
    public HttpRes<String> createLike(@RequestBody TripylerReq.TripylerLikeDto tripylerLikeDto, HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        tripylerService.like(tripylerLikeDto.getTripylerId(), userId);
        return new HttpRes<>("좋아요 등록이 성공적으로 완료되었습니다.");
    }

//     @GetMapping("")
//     public HttpRes<List<TripylerLikeRepository.TripylerLikeCountWhereIsRecruiting>> getTripylerBoardCount(
//             @RequestParam(name = "isRecruiting") int isRecruiting) {
//         return new HttpRes<>(tripylerService.countTripylerIdWhereIsRecruiting(isRecruiting));
//     }

    @ApiOperation(value = "여행자 찾기 댓글 기능", notes = "여행자 찾기 게시물에 댓글을 작성합니다.")
    @PostMapping("/comment")
    public HttpRes<String> createComment(@RequestBody TripylerReq.TripylerCommentDto tripylerCommentDto, HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        tripylerService.comment(tripylerCommentDto.getTripylerId(), userId, tripylerCommentDto.getContent());
        return new HttpRes<>("댓글 등록이 성공적으로 완료되었습니다.");
    }


    @ApiOperation(value = "트리플러 신청하기", notes = "해당 트리플러 게시물을 신청할 수 있습니다. ")
    @PostMapping("/apply")
    public HttpRes<String> applyTripyler(@RequestBody TripylerReq.TripylerApplyDto tripylerApplyDto, HttpServletRequest httpServletRequest){
        Long userId = userService.getUserId(httpServletRequest);
        Long tripylerId = tripylerApplyDto.getTripylerId();
        String content = tripylerApplyDto.getContent();
        tripylerService.applyTripyler(userId,tripylerId, content);
        return new HttpRes<>("신청이 완료되었습니다.");
    }

    @ApiOperation(value = "신청 내역 보기", notes = "나에게 신청된 내역을 모두 볼 수 있습니다.")
    @GetMapping("/apply")
    public HttpRes<Map<Long, List<TripylerRes.AppliedListDto>>> getAllAppliedTripyler(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        Map<Long, List<TripylerRes.AppliedListDto>> appliedListDtoMap = tripylerService.getAppliedList(userId);
        return new HttpRes<>(appliedListDtoMap);
    }

    @ApiOperation(value = "신청 내역 상세보기", notes="신청된 상세 내역을 확인할 수 있습니다.")
    @GetMapping("/apply/{tripylerApplyId}")
    public HttpRes<TripylerRes.AppliedDetailDto> getAppliedDetail(@PathVariable Long tripylerApplyId){
        TripylerRes.AppliedDetailDto appliedDetailDto = tripylerService.getAppliedDetail(tripylerApplyId);
        return new HttpRes<>(appliedDetailDto);
    }

    @ApiOperation(value ="신청 수락", notes = "나에게 온 신청 수락하기")
    @GetMapping("/apply/accept/{tripylerApplyId}")
    public HttpRes<String> acceptTripyler(@PathVariable Long tripylerApplyId, HttpServletRequest httpServletRequest){ // 해당 게시물 번호, 로그인한 유저의 정보
        Long loginUserId = userService.getUserId(httpServletRequest);
        if(tripylerService.acceptTripyler(tripylerApplyId)){
            return new HttpRes<>("수락하였습니다.");
        }
        else{
            throw new NotFoundException("존재하지 않거나 권한이 없는 게시물입니다.");
        }
    }

    @ApiOperation(value ="신청 거절", notes = "나에게 온 신청 거절하기")
    @GetMapping("/apply/refuse/{tripylerApplyId}")
    public HttpRes<String> refuseTripyler(@PathVariable Long tripylerApplyId, HttpServletRequest httpServletRequest){
        Long userId = userService.getUserId(httpServletRequest);
        if(tripylerService.refuseTripyler(tripylerApplyId)){
            return new HttpRes<>("거절하였습니다.");
        }
        else{
            throw new NotFoundException("존재하지 않거나 권한이 없는 게시물입니다.");
        }
    }


    @ApiOperation(value = "여행자 찾기 댓글 리스트 반환", notes = "여행자 찾기 댓글 리스트 반환")
    @GetMapping("/{tripylerId}/comment/list")
    public HttpRes<List<TripylerRes.CommentRes>> getComments(@PathVariable Long tripylerId, HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        List<TripylerRes.CommentRes> result =  tripylerService.getTripylerComments(tripylerId);
        return new HttpRes<>(result);
    }

    @ApiOperation(value = "여행자 찾기 게시물 수정", notes = "여행자 찾기 게시물을 수정합니다.")
    @PatchMapping("/{tripylerId}")
    public HttpRes<String> updateTripyler(@PathVariable Long tripylerId,
                                          @RequestPart TripylerReq.TripylerCreateDto tripylerCreateDto,
                                          @RequestPart(name = "images", required = false) MultipartFile multipartFile,
                                          HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        Tripyler tripyler = tripylerService.findUsersTripyler(userId, tripylerId);
        if(tripyler == null) {
            throw new NotFoundException("존재하지 않거나 권한이 없는 게시물입니다.");
        }
        tripylerService.updateTripyler(tripyler, tripylerCreateDto, multipartFile);
        return new HttpRes<>("게시물 수정이 완료되었습니다.");
    }

    @ApiOperation(value = "여행 지역 검색", notes = "검색한 여행지에 대한 정보를 반환합니다.")
    @GetMapping("/search")
    public HttpRes<TripylerRes.searchDestinationDto> searchDestination(@RequestParam(name = "regionName") String regionName){
        TripylerRes.searchDestinationDto searchDestinationDto = tripylerService.searchDestination(regionName);
        return new HttpRes<>(searchDestinationDto);
    }

}
