package com.tripyle.controller.user;

import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.model.dto.user.UserReq;
import com.tripyle.model.dto.user.UserRes;
import com.tripyle.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags={"02.Profile"})
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {
    private final UserService userService;

    @ApiOperation(value = "프로필 사진 업로드", notes = "프로필 사진을 업로드합니다.")
    @PostMapping("/profile-picture")
    public HttpRes<String> uploadProfileImage(@RequestPart(name = "images", required = true) MultipartFile multipartFile,
                                              HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.uploadProfileImage(userId, multipartFile);
        return new HttpRes<>("프로필 사진 등록이 성공적으로 완료되었습니다.");
    }

    @ApiOperation(value = "프로필 사진 삭제", notes = "프로필 사진을 삭제합니다.")
    @DeleteMapping("/profile-picture")
    public HttpRes<String> deleteProfileImage(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.deleteProfileImage(userId);
        return new HttpRes<>("프로필 사진 삭제가 성공적으로 완료되었습니다.");
    }

    @ApiOperation(value = "프로필 읽기", notes = "자신의 프로필 정보를 읽습니다.\n" +
            "나이를 알 수 없는 경우 0을 리턴합니다.\n" +
            "여행 스타일의 갯수가 3개 미만일 때는 없는 값에 대해 \"\"을 리턴합니다.\n" +
            "프로필 사진이 없는 경우엔 null을 리턴합니다.")
    @GetMapping("/my-profile")
    public HttpRes<UserRes.ProfileDto> getMyProfile(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        return new HttpRes<>(userService.getMyProfile(userId));
    }

    @ApiOperation(value = "상대 프로필 읽기", notes = "상대방의 프로필 정보를 읽습니다.\n")
    @GetMapping("/{userId}")
    public HttpRes<UserRes.ProfileDto> getProfile(@PathVariable Long userId, HttpServletRequest httpServletRequest) {
        return new HttpRes<>(userService.getProfile(userId));
    }

    @ApiOperation(value = "프로필 수정", notes = "자신의 프로필을 수정합니다.\n" +
            "모든 필드들에 대해 원하는 값들을 넣어줍니다\n" +
            "-> 변경되지 않은 값들도 그대로 넣어줍니다.")
    @PatchMapping("/my-profile/update")
    public HttpRes<String> updateProfile(@RequestBody UserReq.ProfileUpdateDto profileUpdateDto,
                                         HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.updateProfile(userId, profileUpdateDto);
        return new HttpRes<>("프로필 수정이 완료되었습니다.");
    }

    @ApiOperation(value = "MBTI 목록 조회", notes = "MBTI 목록을 조회합니다.")
    @GetMapping("/mbti")
    public HttpRes<List<UserRes.MbtiDto>> getMbtiList() {
        return new HttpRes<>(userService.getMbtiList());
    }

    @ApiOperation(value = "이름 공개/비공개 설정", notes = "이름 공개/비공개 여부를 설정합니다.")
    @PostMapping("/name-private")
    public HttpRes<String> makeNamePrivate(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.makeNamePrivate(userId);
        return new HttpRes<>("이름 공개/비공개 여부가 변경되었습니다.");
    }

    @ApiOperation(value = "MBTI 공개/비공개 설정", notes = "MBTI 공개/비공개 여부를 설정합니다.")
    @PostMapping("/mbti-private")
    public HttpRes<String> makeMbtiPrivate(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.makeMbtiPrivate(userId);
        return new HttpRes<>("MBTI 공개/비공개 여부가 변경되었습니다.");
    }

    @ApiOperation(value = "인스타 공개/비공개 설정", notes = "인스타 공개/비공개 여부를 설정합니다.")
    @PostMapping("/instagram-private")
    public HttpRes<String> makeInstagramPrivate(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.makeInstagramPrivate(userId);
        return new HttpRes<>("인스타 공개/비공개 여부가 변경되었습니다.");
    }

    @ApiOperation(value = "휴대폰 공개/비공개 설정", notes = "휴대폰 공개/비공개 여부를 설정합니다.")
    @PostMapping("/phone-private")
    public HttpRes<String> makePhonePrivate(HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.makePhonePrivate(userId);
        return new HttpRes<>("휴대폰 공개/비공개 여부가 변경되었습니다.");
    }
}
