package com.tripyle.controller.user;

import com.tripyle.common.exception.BadRequestException;
import com.tripyle.common.exception.ServerErrorException;
import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.common.model.dto.SmsDto;
import com.tripyle.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.tripyle.model.dto.user.UserReq;
import com.tripyle.model.dto.user.UserRes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Base64;
import java.util.Map;


@Api(tags={"01.User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "회원가입", notes = "회원가입\n" +
            "여행 스타일의 경우 남는 칸은 반드시 0으로 채워주세요.")
    @PostMapping("/signup")
    public HttpRes<String> signup(@Valid @RequestBody UserReq.UserFormReq userFormReq) {
        Long userId = userService.signup(userFormReq);
        return new HttpRes<>("성공적으로 가입되었습니다.");

    }

    @ApiOperation(value = "카카오 회원가입", notes = "카카오 회원가입 후 추가 정보를 등록하는 API 입니다.\n" +
            "/login/kakao을 통해 받은 accessToken을 입력해야만 해당 API 호출이 가능합니다.")
    @PostMapping("/signup/kakao")
    public HttpRes<String> kakaoSignUp(@RequestBody UserReq.KakaoSignUpDto kakaoSignUpDto,
                                       HttpServletRequest httpServletRequest) {
        Long userId = userService.getUserId(httpServletRequest);
        userService.doKakaoAdditionalSignUp(userId, kakaoSignUpDto);
        return new HttpRes<>("카카오 회원가입이 완료되었습니다.");
    }

    @ApiOperation(value = "아이디 중복 체크", notes = "중복이면 true 를, 중복이 아니면 false 를 반환한다.")
    @GetMapping("/username/check/{username}")
    public HttpRes<Boolean> checkUsernameDuplicate(@PathVariable String username){
        return new HttpRes<>(userService.checkUsernameDuplicate(username));
    }

    @ApiOperation(value = "일반 로그인", notes = "ID와 비밀번호로 로그인을 합니다.")
    @PostMapping("/login")
    public HttpRes<UserRes.UserInfoWithToken> doLogIn(@RequestBody UserReq.GeneralLogInDto generalLogInDto) {
        UserRes.UserInfoWithToken userInfoWithToken = userService.doLogIn(generalLogInDto);
        if(userInfoWithToken.getId().equals(0L)) {
            throw new BadRequestException("ID 또는 비밀번호를 확인해주세요.");
        }
        return new HttpRes<>(userInfoWithToken);
    }

    @ApiOperation(value = "카카오 로그인", notes = "카카오 계정으로 로그인을 합니다.")
    @PostMapping("/login/kakao")
    public HttpRes<UserRes.UserInfoWithToken> doKakaoLogIn(@RequestBody UserReq.KakaoLogInDto kakaoLogInDto) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        final String payload = new String(decoder.decode(kakaoLogInDto.getSnsId().split("\\.")[1]));
        JsonParser jsonParser = new BasicJsonParser();
        Map<String, Object> jsonArray;
        try {
            jsonArray = jsonParser.parseMap(payload);
        }
        catch(RuntimeException e) {
            throw new BadRequestException("Token 값을 parsing 하지 못하였습니다. 관리자에게 문의해주세요.");
        }
        if (!jsonArray.containsKey("sub")) {
            throw new ServerErrorException();
        }
        String snsId = jsonArray.get("sub").toString();
        boolean needsAdditionalSignUp = false;
        if(!userService.existsSnsUser("kakao", snsId)) {
            userService.doKakaoSignUp(snsId, kakaoLogInDto.getSnsToken());
            needsAdditionalSignUp = true;
        }
        UserRes.UserInfoWithToken userInfoWithToken = userService.doSocialLogIn("kakao", snsId);
        userInfoWithToken.setNeedsAdditionalSignUp(needsAdditionalSignUp);
        if(userInfoWithToken.getId().equals(0L)) {
            throw new BadRequestException("회원가입을 먼저 진행해주세요.");
        }
        return new HttpRes<>(userInfoWithToken);
    }

    @ApiOperation(value = "네이버 로그인", notes = "네이버 계정으로 로그인을 합니다.")
    @PostMapping("/login/naver")
    public HttpRes<UserRes.UserInfoWithToken> doNaverLogIn(@RequestBody UserReq.NaverLogInDto naverLogInDto) {
        String naverAccessToken = userService.getNaverAccessToken(naverLogInDto);
        String snsId = userService.doNaverSignUp(naverAccessToken);
        UserRes.UserInfoWithToken userInfoWithToken = userService.doSocialLogIn("naver", snsId);
        if(userInfoWithToken.getId().equals(0L)) {
            throw new BadRequestException("회원가입을 먼저 진행해주세요.");
        }
        return new HttpRes<>(userInfoWithToken);
    }

    @ApiOperation(value = "문자 발송", notes = "휴대폰 인증 SMS를 발송합니다.\n")
    @PostMapping("/authentication-code/send")
    public HttpRes<Integer> sendAuthenticationCode(@RequestBody SmsDto.PhoneNumDto phoneNumDto) {
        Integer checkCode = userService.sendAuthenticationCode(phoneNumDto);
        return new HttpRes<>(checkCode);
    }

    @ApiOperation(value = "아이디 찾기", notes = "관리자 | 01012345678\n")
    @PostMapping("/auth/name")
    public HttpRes<UserRes.findUserByName> findUserByName(@RequestBody UserReq.FindUserByName findUserByName) {
        UserRes.findUserByName result = userService.findUserByName(findUserByName);
        return new HttpRes<>(result);
    }


    @ApiOperation(value = "비밀번호 변경 전 본인 인증", notes = "admin | 01012345678\n")
    @PostMapping("/auth/username")
    public HttpRes<String> findUserByUsername(@RequestBody UserReq.FindUserByUsername findUserByUsername) {
        String result = userService.findUserByUsername(findUserByUsername);
        return new HttpRes<>(result);
    }


    @ApiOperation(value = "비밀번호 변경", notes = "아이디, 비밀번호, 비밀번호체크")
    @PostMapping("/password/change")
    public HttpRes<String> changePassword(@RequestBody UserReq.ChangePassword changePassword) {
        if(!changePassword.getNewPassword().equals(changePassword.getNewPasswordCheck()))
            throw new BadRequestException("비밀번호가 동일한지 확인해주세요");

        String result = userService.changePassword(changePassword);
        return new HttpRes<>(result);
    }

}
