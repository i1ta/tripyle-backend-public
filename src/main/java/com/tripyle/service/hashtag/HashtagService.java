package com.tripyle.service.hashtag;

import com.tripyle.common.exception.BadRequestException;
import com.tripyle.common.exception.ServerErrorException;
import com.tripyle.model.dto.hashtag.HashtagRes;
import com.tripyle.model.entity.hashtag.Hashtag;
import com.tripyle.model.entity.user.User;
import com.tripyle.model.entity.user.UserHashtag;
import com.tripyle.repository.hashtag.HashtagRepository;
import com.tripyle.repository.user.UserHashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final UserHashtagRepository userHashtagRepository;

    public List<HashtagRes.HashtagDto> readHashtagSearchList(String name) {
        List<Hashtag> hashtags = hashtagRepository.findByNameContains(name);
        List<HashtagRes.HashtagDto> hashtagDtoList = new ArrayList<>();
        for(Hashtag hashtag : hashtags) {
//            int count = userHashtagRepository.countByHashtag(hashtag);
            hashtagDtoList.add(
                    HashtagRes.HashtagDto.builder()
                            .id(hashtag.getId())
                            .name(hashtag.getName())
//                            .count(count)
                            .build()
            );
        }
        return hashtagDtoList;
    }

    public List<HashtagRes.HashtagDto> readHashtagList() {
        List<Hashtag> hashtags = hashtagRepository.findAll();
        List<HashtagRes.HashtagDto> hashtagDtoList = new ArrayList<>();

        for(Hashtag hashtag: hashtags){
            HashtagRes.HashtagDto e = HashtagRes.HashtagDto.builder()
                    .id(hashtag.getId())
                    .name(hashtag.getName())
                    .build();

            hashtagDtoList.add(e);
        }


        return hashtagDtoList;
    }

//    public HashtagRes.HashtagDto createHashtag(String name) {
//        if(hashtagRepository.existsByName(name)) {
//            throw new BadRequestException("이미 존재하는 해시태그입니다.");
//        }
//        Hashtag hashtag = Hashtag.builder()
//                .name(name)
//                .build();
//        hashtagRepository.save(hashtag);
//        Optional<Hashtag> optionalHashtag = Optional.ofNullable(hashtagRepository.findByName(name));
//        if(optionalHashtag.isEmpty()) {
//            throw new ServerErrorException();
//        }
//        Hashtag _hashtag = optionalHashtag.get();
//        return HashtagRes.HashtagDto.builder()
//                .id(_hashtag.getId())
//                .name(_hashtag.getName())
//                .build();
//    }
}
