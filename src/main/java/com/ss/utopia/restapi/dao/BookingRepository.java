package com.ss.utopia.restapi.dao;

import com.ss.utopia.restapi.models.*;

import org.springframework.data.repository.CrudRepository;

public interface BookingRepository extends CrudRepository<Booking, Integer> {}