package com.example.bodybalance.Repository;

import com.example.bodybalance.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {

    User findByChatId(Long chatId);
}
