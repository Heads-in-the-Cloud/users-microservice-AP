package com.ss.utopia.restapi.dao;

import java.util.Optional;

import com.ss.utopia.restapi.models.User;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    public Optional<User> findByUsername(String username);
}