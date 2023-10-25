package com.tripyle.service.destination;

import com.tripyle.model.dto.board.TripylerReq;
import com.tripyle.model.dto.board.TripylerRes;
import com.tripyle.model.entity.board.Tripyler;
import com.tripyle.model.entity.board.TripylerHashtag;
import com.tripyle.model.entity.destination.Continent;
import com.tripyle.model.entity.destination.Nation;
import com.tripyle.model.entity.destination.Region;
import com.tripyle.model.entity.hashtag.Hashtag;
import com.tripyle.model.entity.user.User;
import com.tripyle.repository.board.TripylerCommentRepository;
import com.tripyle.repository.board.TripylerHashtagRepository;
import com.tripyle.repository.board.TripylerLikeRepository;
import com.tripyle.repository.board.TripylerRepository;
import com.tripyle.repository.destination.ContinentRepository;
import com.tripyle.repository.destination.NationRepository;
import com.tripyle.repository.destination.RegionRepository;
import com.tripyle.repository.hashtag.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DestinationService {
    private final ContinentRepository continentRepository;
    private final NationRepository nationRepository;
    private final RegionRepository regionRepository;

    public List<Continent> getContinentList(){
        return continentRepository.findAll();
    }

    public List<Nation> getNationList(Long continentId){
        return nationRepository.findByContinentId(continentId);
    }

    public List<Region> getRegionList(Long nationId){
        return regionRepository.findByNationId(nationId);
    }
}
