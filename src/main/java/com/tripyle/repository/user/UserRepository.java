package com.tripyle.repository.user;

import com.tripyle.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username=:username")

    User findByUsername(String username);

    User findByLoginTypeAndSnsId(String loginType, String snsId);

    boolean existsByLoginTypeAndSnsId(String loginType, String snsId);

    boolean existsByUsername(String username);

    User findByNameAndPhone(String name, String phone);

    User findByUsernameAndPhone(String username, String phone);

}
