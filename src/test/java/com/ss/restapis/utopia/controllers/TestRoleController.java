package com.ss.restapis.utopia.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.ss.utopia.restapi.configs.SecurityConfiguration;
import com.ss.utopia.restapi.controllers.UserRoleController;
import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.dao.UserRoleRepository;
import com.ss.utopia.restapi.models.UserRole;
import com.ss.utopia.restapi.services.ResetAutoCounterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@Import(SecurityConfiguration.class)
@SpringBootTest(classes = UserRoleController.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
class TestRoleController {
    @MockBean UserRoleRepository roleDB;
    @MockBean UserRepository userDB;
    @MockBean ResetAutoCounterService resetService;
    @Autowired TestRestTemplate restTemplate;

    ArrayList<UserRole> roles;

    UserRole agentRole = new UserRole(1, "AGENT");
    UserRole userRole = new UserRole(2, "USER");
    UserRole guestRole = new UserRole(3, "GUEST");
    UserRole adminRole = new UserRole(4, "ADMIN");

    private <T> T asParsedJson(Object obj) throws JsonProcessingException {
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        return JsonPath.read(json, "$");
    }

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        roles = new ArrayList<>(Arrays.asList(agentRole, userRole, guestRole, adminRole));
    }

    @Test
    @PreAuthorize("ADMIN")
    void getRoleByIDTest() {
        when(roleDB.findById(anyInt()))
            .thenAnswer(u -> roles.stream().filter(r -> r.getId() == (int) u.getArgument(0)).findFirst());

        String url = "/user-roles/";
        HttpEntity<String> entity = new HttpEntity<String>("", new HttpHeaders());

        for (UserRole role : roles) {
            ResponseEntity<String> response = restTemplate.exchange(url + role.getId(), HttpMethod.GET, entity, String.class);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    @PreAuthorize("ADMIN")
    void getAllRolesTest() throws JsonProcessingException {
        when(roleDB.findAll())
            .thenReturn(roles);

        String url = "/user-roles/";
        HttpEntity<String> entity = new HttpEntity<String>("", new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        ArrayList<UserRole> _roles = JsonPath.read(response.getBody(), "$");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(asParsedJson(roles), _roles);
    }

    @Test
    @PreAuthorize("ADMIN")
    void createRoleTest() {
        when(roleDB.findById(anyInt()))
            .thenAnswer(u ->  roles.stream().filter(r -> r.getId() == (int)u.getArgument(0)).findFirst());

        when(roleDB.save(any(UserRole.class)))
            .thenAnswer(u -> {
                UserRole i = u.getArgument(0);
                if (i.getId() == null || i.getName() == null) { throw new IllegalArgumentException(); }
                roles.add(i);
                return i;
            });

        UserRole role = new UserRole(5, "BUSSINESS");

        String url = "/user-roles/";
        HttpEntity<UserRole> entity = new HttpEntity<>(role, new HttpHeaders());
        HttpEntity<UserRole> nullEntity = new HttpEntity<>(new UserRole(), new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        ResponseEntity<String> nullResponse = restTemplate.exchange(url, HttpMethod.POST, nullEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, nullResponse.getStatusCode());
    }

    @Test
    @PreAuthorize("ADMIN")
    void deleteRoleTest() {
        when(roleDB.findById(anyInt()))
            .thenAnswer(u -> roles.stream().filter(r -> r.getId() == (int)u.getArgument(0)).findFirst());

        doAnswer(r -> {
            try {
                UserRole i = r.getArgument(0);
                UserRole removedRole = roles.stream()
                    .filter(k -> k.getId() == i.getId())
                    .findFirst().get();

                roles.remove(removedRole);
            } catch (Exception e) {
                throw new DataIntegrityViolationException(e.getMessage());
            }
            return null;
        }).when(roleDB).delete(any(UserRole.class));

        String url = "/user-roles/1";
        HttpEntity<String> entity = new HttpEntity<>("", new HttpHeaders());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        ResponseEntity<String> repeatResponse = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, repeatResponse.getStatusCode());
    }
}
