package com.talkssogi.TalkSsogi_server.repository;

import com.talkssogi.TalkSsogi_server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByUserId(String userId);
    boolean existsByUserId(String userId);
}