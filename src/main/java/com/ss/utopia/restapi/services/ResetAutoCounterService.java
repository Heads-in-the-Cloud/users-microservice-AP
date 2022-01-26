package com.ss.utopia.restapi.services;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class ResetAutoCounterService {
    @PersistenceContext
    EntityManager em;

    @Transactional
    public void resetAutoCounter(String tableName) {
        em.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
    }
}
