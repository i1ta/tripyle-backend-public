package com.tripyle.service.user;

import com.tripyle.common.exception.BadRequestException;
import com.tripyle.common.exception.NotFoundException;
import com.tripyle.common.exception.ServerErrorException;
import com.tripyle.common.model.dto.SmsDto;
import com.tripyle.common.service.S3Service;
import com.tripyle.common.service.SmsService;
import com.tripyle.config.EnvConfig;
import com.tripyle.model.dto.user.UserReq;
import com.tripyle.model.dto.user.UserRes;
import com.tripyle.model.entity.hashtag.Hashtag;
import com.tripyle.model.entity.user.Mbti;
import com.tripyle.model.entity.user.User;
import com.tripyle.model.entity.user.UserHashtag;
import com.tripyle.repository.hashtag.HashtagRepository;
import com.tripyle.repository.user.MbtiRepository;
import com.tripyle.repository.user.UserHashtagRepository;
import com.tripyle.repository.user.UserRepository;
import com.tripyle.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@DependsOn("env")
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;
    private final SmsService smsService;
    private final S3Service s3Service;
    private final HashtagRepository hashtagRepository;
    private final UserHashtagRepository userHashtagRepository;
    private final MbtiRepository mbtiRepository;

    public Long getUserId(HttpServletRequest httpServletRequest ){
        return Long.valueOf(String.valueOf(httpServletRequest.getAttribute("id")));
    }

    public User getUserByUserId(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }
        return optionalUser.get();
    }

    public Long signup(UserReq.UserFormReq userFormReq){
        userFormReq.setPassword((passwordEncoder.encode(userFormReq.getPassword())));

        User user = User.builder()
                .username(userFormReq.getUsername())
                .password(userFormReq.getPassword())
                .name(userFormReq.getName())
                .nickname(userFormReq.getName())
                .phone(userFormReq.getPhone())
                .email(userFormReq.getEmail())
                .gender(userFormReq.getGender())
                .birthDate(userFormReq.getBirthDate())
                .loginType("phone")
                .userRole("ROLE_USER")
                .firstLogin(true)
                .isNamePrivate(true)
                .isMbtiPrivate(false)
                .isInstagramPrivate(false)
                .isPhonePrivate(true)
                .build();

        userRepository.save(user);
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(userFormReq.getUsername()));
        if(optionalUser.isEmpty()) {
            throw new ServerErrorException();
        }

        //유저 세부내역 등록
        registerTripStyle(optionalUser.get().getId(), userFormReq);

        return optionalUser.get().getId();
    }

    public boolean checkUsernameDuplicate(String username){
        return userRepository.existsByUsername(username);
    }

    public UserRes.UserInfoWithToken doLogIn(UserReq.GeneralLogInDto generalLogInDto) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(generalLogInDto.getUsername()));
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(passwordEncoder.matches(generalLogInDto.getPassword(), user.getPassword())) {
                boolean isFirstLogin = user.isFirstLogin();
                user.setFirstLogin(false);
                return UserRes.UserInfoWithToken.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .alarmNum(0)
                        .accessToken(jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), Collections.singletonList(user.getUserRole())))
                        .isFirstLogin(isFirstLogin)
                        .userRole(user.getUserRole())
                        .build();
            }
        }
        return UserRes.UserInfoWithToken.builder()
                .id(0L)
                .build();
    }

    public UserRes.UserInfoWithToken doSocialLogIn(String loginType, String snsId) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByLoginTypeAndSnsId(loginType, snsId));
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean isFirstLogin = user.isFirstLogin();
            user.setFirstLogin(false);
            return UserRes.UserInfoWithToken.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .alarmNum(0)
                    .accessToken(jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), Collections.singletonList(user.getUserRole())))
                    .isFirstLogin(isFirstLogin)
                    .build();
        }
        return UserRes.UserInfoWithToken.builder()
                .id(0L)
                .build();
    }
    public void doKakaoSignUp(String snsId, String snsToken) {
//        UserRes.NaverProfileResponseDto naverProfileResponse;
//        final String naverProfileUrl = "https://kapi.kakao.com/v2/user/me";
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("Authorization", "Bearer " + kakaoLogInDto.getSnsToken());
//        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//        try {
//            ResponseEntity<UserRes.NaverProfileResponseDto> response = restTemplate.exchange(naverProfileUrl, HttpMethod.GET, httpEntity, UserRes.NaverProfileResponseDto.class);
//            if(response.getStatusCodeValue() != HttpStatus.OK.value()) {
//                throw new ServerErrorException();
//            }
//            naverProfileResponse = response.getBody();
//            if(naverProfileResponse == null){
//                throw new BadRequestException("카카오 회원가입 중 오류가 발생했습니다.");
//            }
//        }
//        catch (RestClientException e) {
//            throw new ServerErrorException();
//        }
//        회원 관련 정보 받아야 함

        User user = User.builder()
                .loginType("kakao")
                .snsId(snsId)
                .snsToken(snsToken)
                .userRole("ROLE_USER")
                .firstLogin(true)
                .build();
        userRepository.save(user);
    }

    public void doKakaoAdditionalSignUp(Long userId, UserReq.KakaoSignUpDto kakaoSignUpDto) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(kakaoSignUpDto.getName());
            user.setBirthDate(kakaoSignUpDto.getBirthDate());
            user.setGender(kakaoSignUpDto.getGender());
            user.setEmail(kakaoSignUpDto.getEmail());
            user.setFirstLogin(true);
            userRepository.save(user);
        }
        else {
            throw new BadRequestException("카카오 회원가입을 먼저 진행해주세요.");
        }
    }

    public String doNaverSignUp(String naverAccessToken) {
        String snsId;
        UserRes.NaverProfileResponseDto naverProfileResponse;
        UserRes.NaverUserProfileDto naverUserProfileDto;
        final String naverProfileUrl = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + naverAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<UserRes.NaverProfileResponseDto> response = restTemplate.exchange(naverProfileUrl, HttpMethod.GET, httpEntity, UserRes.NaverProfileResponseDto.class);
            if(response.getStatusCodeValue() != HttpStatus.OK.value()) {
                throw new ServerErrorException();
            }
            naverProfileResponse = response.getBody();
            if(naverProfileResponse == null){
                throw new BadRequestException("네이버 회원가입 중 오류가 발생했습니다. [0]");
            }
            naverUserProfileDto = naverProfileResponse.getNaverUserProfileDto();
            if(naverUserProfileDto == null) {
                throw new BadRequestException("네이버 회원가입 중 오류가 발생했습니다. [6]");
            }
            snsId = naverUserProfileDto.getSnsId();
            if(snsId == null) {
                throw new BadRequestException("네이버 회원가입 중 오류가 발생했습니다. [1]");
            }
        }
        catch (RestClientException e) {
            throw new ServerErrorException();
        }

        if(!userRepository.existsByLoginTypeAndSnsId("naver", snsId)) {
            String stringBirthDate = naverUserProfileDto.getBirthYear() + "-" + naverUserProfileDto.getBirthDay();
            LocalDate birthDate;
            try {
                birthDate = LocalDate.parse(stringBirthDate, DateTimeFormatter.ISO_DATE);
            }
            catch(DateTimeParseException e) {
                birthDate = LocalDate.of(1900,1,1);
            }
            User user = User.builder()
                    .loginType("naver")
                    .snsId(snsId)
                    .snsToken(naverAccessToken)
                    .userRole("ROLE_USER")
                    .name(naverUserProfileDto.getName())
                    .nickname(naverUserProfileDto.getNickname())
                    .gender(naverUserProfileDto.getGender())
                    .phone(naverUserProfileDto.getPhone().replaceAll("-", ""))
                    .email(naverUserProfileDto.getEmail())
                    .birthDate(birthDate)
                    .firstLogin(true)
                    .build();
            userRepository.save(user);
        }
        return snsId;
    }

    public UserRes.UserInfoWithToken doNaverLogIn(String naverAccessToken) {
        String snsId;
        final String naverProfileUrl = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + naverAccessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<UserRes.NaverProfileResponseDto> response = restTemplate.exchange(naverProfileUrl, HttpMethod.GET, httpEntity, UserRes.NaverProfileResponseDto.class);
            if(response.getStatusCodeValue() != HttpStatus.OK.value()) {
                throw new ServerErrorException();
            }
            UserRes.NaverProfileResponseDto naverProfileResponse = response.getBody();
            if(naverProfileResponse == null){
                throw new BadRequestException("네이버 로그인중 오류가 발생했습니다. [2]");
            }
            UserRes.NaverUserProfileDto naverUserProfileDto = naverProfileResponse.getNaverUserProfileDto();
            if(naverUserProfileDto == null) {
                throw new BadRequestException("네이버 로그인중 오류가 발생했습니다. [7]");
            }
            snsId = naverUserProfileDto.getSnsId();
            if(snsId == null) {
                throw new BadRequestException("네이버 로그인중 오류가 발생했습니다. [3]");
            }
        }
        catch (RestClientException e) {
            throw new ServerErrorException();
        }

        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByLoginTypeAndSnsId("naver", snsId));
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            return UserRes.UserInfoWithToken.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .alarmNum(0)
                    .accessToken(jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), Collections.singletonList(user.getUserRole())))
                    .build();
        }
        return UserRes.UserInfoWithToken.builder()
                .id(0L)
                .build();
    }

    public String getNaverAccessToken(UserReq.NaverLogInDto naverLogInDto) {
        final String clientId = EnvConfig.naverLoginClientId();
        final String clientSecret = EnvConfig.naverLoginClientSecret();
        String accessToken;
        String naverTokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code";
        naverTokenUrl += ("&client_id=" + clientId);
        naverTokenUrl += ("&client_secret=" + clientSecret);
        naverTokenUrl += ("&code=" + naverLogInDto.getCode());
        naverTokenUrl += ("&state=" + naverLogInDto.getState());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<UserRes.NaverAccessTokenResponseDto> response
                    = restTemplate.exchange(naverTokenUrl, HttpMethod.GET, httpEntity, UserRes.NaverAccessTokenResponseDto.class);
            if(response.getStatusCodeValue() != HttpStatus.OK.value()) {
                throw new ServerErrorException();
            }
            UserRes.NaverAccessTokenResponseDto naverAccessTokenResponseDto = response.getBody();
            if(naverAccessTokenResponseDto == null){
                throw new BadRequestException("네이버 로그인중 오류가 발생했습니다. [4]");
            }
            if(naverAccessTokenResponseDto.getAccessToken() == null) {
                throw new BadRequestException("네이버 로그인중 오류가 발생했습니다. [5]");
            }
            accessToken = naverAccessTokenResponseDto.getAccessToken();

        }
        catch (RestClientException e) {
            throw new ServerErrorException();
        }
        return accessToken;
    }

    public boolean existsSnsUser(String loginType, String snsId) {
        return userRepository.existsByLoginTypeAndSnsId(loginType, snsId);
    }

    public int sendAuthenticationCode(SmsDto.PhoneNumDto phoneNumDto) {
        Random random = new Random();
        int checkCode = random.nextInt(888888) + 111111;


        String content = "Tripyle 인증 번호 [";
        content += checkCode;
        content += "]을(를) 입력해 주세요.";

        smsService.sendSms(phoneNumDto.getPhone(), content);

        return checkCode;
    }


    //로그인 타입이 phone이 아니면 소셜로그인으로 하라고 알려주기
    //아이디 찾기
    public UserRes.findUserByName findUserByName(UserReq.FindUserByName findUserByName) {
        User user = userRepository.findByNameAndPhone(findUserByName.getName(), findUserByName.getPhone());

        if(user == null)
            throw new NotFoundException("유저를 찾을 수 없습니다. 소셜로그인으로 접근해주세요");

        UserRes.findUserByName result = new UserRes.findUserByName(user.getUsername());

        return result;
    }

    //비밀번호 찾기
    public String findUserByUsername(UserReq.FindUserByUsername findUserByUsername) {
        User user = userRepository.findByUsernameAndPhone(findUserByUsername.getUsername(), findUserByUsername.getPhone());

        if(user == null)
            throw new NotFoundException("유저를 찾을 수 없습니다.");

        return "존재하는 유저입니다";
    }

    @Transactional
    public String changePassword(UserReq.ChangePassword changePassword) {
        User user = userRepository.findByUsername(changePassword.getUsername());

        user.setPassword(passwordEncoder.encode(changePassword.getNewPassword()));

        return "새로운 비밀번호로 변경되었습니다";
    }

    public void uploadProfileImage(Long userId, MultipartFile multipartFile) {
        String profileUrl = s3Service.uploadImage("profile", Long.toString(userId), multipartFile);

        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setProfileUrl(profileUrl);
            userRepository.save(user);
        }
        else {
//            branch 합친 후에 NotFoundException으로 바꿔야 함
            throw new BadRequestException("존재하지 않는 회원입니다.");
        }
    }

    public void deleteProfileImage(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setProfileUrl(null);
            userRepository.save(user);
        }
        else {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }
    }


    public void registerTripStyle(Long userId, UserReq.UserFormReq userFormReq) {

        Optional<User> optionalUser = userRepository.findById(userId);
        User user;
        if(optionalUser.isPresent()) {
            user = optionalUser.get();
        }
        else {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }

        if(userFormReq.getFirstTripStyleId().equals(0L) && userFormReq.getSecondTripStyleId().equals(0L)
                &&userFormReq.getThirdTripStyleId().equals(0L)) {
            throw new BadRequestException("해시태그를 하나 이상 입력해주세요");
        }

        List<Long> tripStyleList = new ArrayList<>();
        tripStyleList.add(userFormReq.getFirstTripStyleId());
        tripStyleList.add(userFormReq.getSecondTripStyleId());
        tripStyleList.add(userFormReq.getThirdTripStyleId());

        for(Long tripStyleId : tripStyleList) {
            if(tripStyleId.equals(0L)) {
                continue;
            }
            Optional<Hashtag> _hashtag = hashtagRepository.findById(tripStyleId);
            if(_hashtag.isEmpty()) {
                throw new BadRequestException("해당 해시태그를 먼저 등록해주세요.");
            }
            Hashtag hashtag = _hashtag.get();
            UserHashtag userHashtag  = UserHashtag.builder()
                    .user(user)
                    .hashtag(hashtag)
                    .build();
            userHashtagRepository.save(userHashtag);
        }
    }

    public UserRes.ProfileDto getMyProfile(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) {
            throw new NotFoundException("존재하지 않는 유저입니다.");
        }
        User user = optionalUser.get();

        List<UserHashtag> userHashtagList = userHashtagRepository.findByUser(user);
        List<String> hashtagName = new ArrayList<>();
        for(UserHashtag userHashtag : userHashtagList) {
            hashtagName.add(userHashtag.getHashtag().getName());
        }
        int hashtagLen = userHashtagList.size();
        for(int i = 0; i < 3 - hashtagLen; i++) {
            hashtagName.add("");
        }

        int age;
        if(user.getBirthDate() == null || user.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
            age = 0;
        }
        else {
            age = LocalDate.now().getYear() - user.getBirthDate().getYear() + 1;
        }
        String mbti = user.getMbti() == null ? null : user.getMbti().getName();

        return UserRes.ProfileDto.builder()
                .name(user.getName())
                .username(user.getUsername())
                .age(age)
                .instagram(user.getInstagram())
                .phone(user.getPhone())
                .gender(user.getGender())
                .firstBio(user.getFirstBio())
                .secondBio(user.getSecondBio())
                .thirdBio(user.getThirdBio())
                .mbti(mbti)
                .firstTripStyle(hashtagName.get(0))
                .secondTripStyle(hashtagName.get(1))
                .thirdTripStyle(hashtagName.get(2))
                .profileUrl(user.getProfileUrl())
                .isPhonePrivate(user.isPhonePrivate())
                .isNamePrivate(user.isNamePrivate())
                .isInstagramPrivate(user.isInstagramPrivate())
                .isPhonePrivate(user.isPhonePrivate())
                .isMbtiPrivate(user.isMbtiPrivate())
                .build();
    }

    public UserRes.ProfileDto getProfile(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) {
            throw new NotFoundException("존재하지 않는 유저입니다.");
        }
        User user = optionalUser.get();

        List<UserHashtag> userHashtagList = userHashtagRepository.findByUser(user);
        List<String> hashtagName = new ArrayList<>();
        for(UserHashtag userHashtag : userHashtagList) {
            hashtagName.add(userHashtag.getHashtag().getName());
        }
        int hashtagLen = userHashtagList.size();
        for(int i = 0; i < 3 - hashtagLen; i++) {
            hashtagName.add("");
        }

        int age;
        if(user.getBirthDate() == null || user.getBirthDate().isEqual(LocalDate.of(1900, 1, 1))) {
            age = 0;
        }
        else {
            age = LocalDate.now().getYear() - user.getBirthDate().getYear() + 1;
        }
        String mbti = user.getMbti() == null ? null : user.getMbti().getName();

        return UserRes.ProfileDto.builder()
                .name(user.isNamePrivate() ? null : user.getName())
                .username(user.getUsername())
                .age(age)
                .instagram(user.isInstagramPrivate() ? null : user.getInstagram())
                .phone(user.isPhonePrivate() ? null : user.getPhone())
                .gender(user.getGender())
                .firstBio(user.getFirstBio())
                .secondBio(user.getSecondBio())
                .thirdBio(user.getThirdBio())
                .mbti(user.isMbtiPrivate() ? null : mbti)
                .firstTripStyle(hashtagName.get(0))
                .secondTripStyle(hashtagName.get(1))
                .thirdTripStyle(hashtagName.get(2))
                .profileUrl(user.getProfileUrl())
                .build();
    }

    public List<UserRes.MbtiDto> getMbtiList() {
        List<Mbti> mbtiList = mbtiRepository.findAll();
        List<UserRes.MbtiDto> mbtiDtoList = new ArrayList<>();
        for(Mbti mbti : mbtiList) {
            mbtiDtoList.add(UserRes.MbtiDto.builder()
                    .id(mbti.getId())
                    .name(mbti.getName())
                    .build());
        }
        return mbtiDtoList;
    }

    public void updateProfile(Long userId, UserReq.ProfileUpdateDto profileUpdateDto) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) {
            throw new NotFoundException("존재하지 않는 회원입니다.");
        }
        User user = optionalUser.get();

        Optional<Mbti> optionalMbti = mbtiRepository.findById(profileUpdateDto.getMbtiId());
        if(optionalMbti.isEmpty()) {
            throw new BadRequestException("존재하지 않는 MBTI입니다.");
        }
        Mbti mbti = optionalMbti.get();

        user.setFirstBio(profileUpdateDto.getFirstBio());
        user.setSecondBio(profileUpdateDto.getSecondBio());
        user.setThirdBio(profileUpdateDto.getThirdBio());
        user.setInstagram(profileUpdateDto.getInstagram());
        user.setMbti(mbti);
        user.setPhone(profileUpdateDto.getPhone());
        userRepository.save(user);

        List<Long> newHashtagIds = Arrays.asList(profileUpdateDto.getFirstTripStyleId(),
                profileUpdateDto.getSecondTripStyleId(), profileUpdateDto.getThirdTripStyleId());

        List<UserHashtag> userHashtagList = userHashtagRepository.findByUser(user);
        List<Long> existingHashtagIds = new ArrayList<>();
        for(UserHashtag userHashtag : userHashtagList) {
            existingHashtagIds.add(userHashtag.getHashtag().getId());
        }

        for(Long hashtagId : newHashtagIds) {
            if(hashtagId.equals(0L)) {
                continue;
            }
            if(!existingHashtagIds.contains(hashtagId)) {
                Optional<Hashtag> optionalHashtag = hashtagRepository.findById(hashtagId);
                if(optionalHashtag.isEmpty()) {
                    throw new BadRequestException("존재하지 않는 해시태그입니다.");
                }
                Hashtag hashtag = optionalHashtag.get();
                UserHashtag userHashtag = UserHashtag.builder()
                        .user(user)
                        .hashtag(hashtag)
                        .build();
                userHashtagRepository.save(userHashtag);
            }
        }

        for(Long hashtagId : existingHashtagIds) {
            if(!newHashtagIds.contains(hashtagId)) {
                Optional<UserHashtag> optionalUserHashtag = Optional.ofNullable(userHashtagRepository.findByUserAndHashtag_Id(user, hashtagId));
                if(optionalUserHashtag.isEmpty()) {
                    throw new BadRequestException("해시태그를 삭제할 수 없습니다.");
                }
                UserHashtag userHashtag = optionalUserHashtag.get();
                userHashtag.setDeleteYn(true);
                userHashtagRepository.save(userHashtag);
            }
        }
    }

    public Long findUserIdByUsername(String username) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(username));
        if(optionalUser.isEmpty()) {
            return 0L;
        }
        return optionalUser.get().getId();

    }

    public void makeNamePrivate(Long userId) {
        User user = getUserByUserId(userId);
        boolean isNamePrivate = user.isNamePrivate();
        user.setNamePrivate(!isNamePrivate);
    }

    public void makeMbtiPrivate(Long userId) {
        User user = getUserByUserId(userId);
        boolean isMbtiPrivate = user.isMbtiPrivate();
        user.setMbtiPrivate(!isMbtiPrivate);
    }

    public void makeInstagramPrivate(Long userId) {
        User user = getUserByUserId(userId);
        boolean isInstagramPrivate = user.isInstagramPrivate();
        user.setInstagramPrivate(!isInstagramPrivate);
    }

    public void makePhonePrivate(Long userId) {
        User user = getUserByUserId(userId);
        boolean isPhonePrivate = user.isPhonePrivate();
        user.setPhonePrivate(!isPhonePrivate);
    }
}
