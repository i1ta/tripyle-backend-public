package com.tripyle.repository.hashtag;

import com.tripyle.model.entity.hashtag.Hashtag;
import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    boolean existsByName(String name);
    List<Hashtag> findByNameContains(String name);
    Hashtag findByName(String name);
}
