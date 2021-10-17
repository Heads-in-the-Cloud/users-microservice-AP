package com.ss.utopia.restapi.dao;

import com.ss.utopia.restapi.models.User;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {}