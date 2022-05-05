package com.ss.restapis.utopia.controllers;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.javafaker.Internet;
import com.github.javafaker.Name;
import com.jayway.jsonpath.JsonPath;
import com.ss.utopia.restapi.configs.SecurityConfiguration;
import com.ss.utopia.restapi.controllers.UserController;
import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.jwt.JwtProperties;
import com.ss.utopia.restapi.models.LoginInfo;
import com.ss.utopia.restapi.models.User;
import com.ss.utopia.restapi.models.UserPrincipal;
import com.ss.utopia.restapi.models.UserRole;
import com.ss.utopia.restapi.services.ResetAutoCounterService;
import com.ss.utopia.restapi.services.UserPrincipalDetailsService;

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
import org.springframework.security.crypto.password.PasswordEncoder;

@Import(SecurityConfiguration.class)
@SpringBootTest(classes = UserController.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
class TestUserController {
    @MockBean UserRepository userDB;
    @MockBean UserPrincipalDetailsService userDetailsService;
    @MockBean ResetAutoCounterService resetAutoCounterService;

    @Autowired PasswordEncoder passwordEncoder;
    @Autowired TestRestTemplate restTemplate;

    UserRole agentRole = new UserRole(1, "AGENT");
    UserRole userRole = new UserRole(2, "USER");
    UserRole guestRole = new UserRole(3, "GUEST");
    UserRole adminRole = new UserRole(4, "ADMIN");

    User adminUser = new User(0, adminRole, "Admin", "Soto Pellot", "admin", "admin@smoothstack.com", "admin",
            "540-850-6969");
    User angelAdmin = new User(1, adminRole, "Angel", "Soto Pellot", "asoto22", "asoto22@smoothstack.com", "somepass",
            "1-800-235-4851");

    User user1, user2, user3, user4;

    ArrayList<User> users;
    String adminToken;
    String userToken;

    HttpHeaders adminHeader;
    HttpHeaders userHeader;

    Faker faker = new Faker();

    private <T> T asParsedJson(Object obj) throws JsonProcessingException {
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        return JsonPath.read(json, "$");
    }

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        Name name = faker.name();
        Internet internet = faker.internet();
        user1 = new User(2, userRole, name.firstName(), name.lastName(), name.username(), internet.emailAddress(),
                internet.password(), faker.phoneNumber().cellPhone());
        user2 = new User(3, userRole, name.firstName(), name.lastName(), name.username(), internet.emailAddress(),
                internet.password(), faker.phoneNumber().cellPhone());
        user3 = new User(4, agentRole, name.firstName(), name.lastName(), name.username(), internet.emailAddress(),
                internet.password(), faker.phoneNumber().cellPhone());
        user4 = new User(5, agentRole, name.firstName(), name.lastName(), name.username(), internet.emailAddress(),
                internet.password(), faker.phoneNumber().cellPhone());

        users = new ArrayList<>(Arrays.asList(adminUser, angelAdmin, user1, user2, user3, user4));
        for (int i = 0; i < users.size(); i++) {
            when(userDB.findById(i))
                .thenReturn(Optional.of(users.get(i)));
        }

        when(userDB.findByUsername("admin"))
            .thenReturn(Optional.of(adminUser));

        when(userDetailsService.loadUserByUsername("admin"))
            .thenReturn(new UserPrincipal(adminUser));

        when(userDB.findByUsername(user1.getUsername()))
            .thenReturn(Optional.of(user1));

        when(userDetailsService.loadUserByUsername(user1.getUsername()))
            .thenReturn(new UserPrincipal(user1));

        when(userDB.findAll())
            .thenReturn(users);

        adminToken = JWT.create()
            .withSubject("admin")
            .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
            .sign(HMAC512(JwtProperties.SECRET.getBytes()));

        userToken = JWT.create()
            .withSubject(user1.getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
            .sign(HMAC512(JwtProperties.SECRET.getBytes()));

        adminHeader = new HttpHeaders();
        adminHeader.setBearerAuth(adminToken);

        userHeader = new HttpHeaders();
        userHeader.setBearerAuth(userToken);
    }

    @Test
    void loginTest() {
        adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUsername("admin");
        loginInfo.setPassword("admin");

        HttpEntity<LoginInfo> entity = new HttpEntity<>(loginInfo, new HttpHeaders());
        ResponseEntity<String> response = restTemplate
            .postForEntity("/login", entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Authorization"));
    }

    @Test
    void getUserByIDTest() throws Exception {
        HttpEntity<String> unauthEntity = new HttpEntity<>("", new HttpHeaders());
        HttpEntity<String> authEntity = new HttpEntity<>("", adminHeader);
        HttpEntity<String> userEntity = new HttpEntity<>("", userHeader);

        String url = "/users/";

        for (int i = 0; i < 6; i++) {
            ResponseEntity<String> noAuthResponse = restTemplate.exchange(url + i, HttpMethod.GET, unauthEntity, String.class);
            ResponseEntity<String> adminResponse = restTemplate.exchange(url + i, HttpMethod.GET, authEntity, String.class);
            ResponseEntity<String> userResponse = restTemplate.exchange(url + i, HttpMethod.GET, userEntity, String.class);

            assertEquals(HttpStatus.UNAUTHORIZED, noAuthResponse.getStatusCode());
            assertEquals(HttpStatus.OK, adminResponse.getStatusCode());

            if (users.get(i).getUsername() == user1.getUsername()) {
                User _user = new ObjectMapper().readValue(userResponse.getBody(), User.class);

                assertEquals(HttpStatus.OK, userResponse.getStatusCode());
                assertEquals(asParsedJson(users.get(i)).toString(), asParsedJson(_user).toString());
            } else {
                assertEquals(HttpStatus.UNAUTHORIZED, userResponse.getStatusCode());
            }
        }
    }

    @Test
    void getAllUsersTest() throws JsonProcessingException {
        HttpEntity<String> unauthEntity = new HttpEntity<>("", new HttpHeaders());
        HttpEntity<String> authEntity = new HttpEntity<>("", adminHeader);
        HttpEntity<String> userEntity = new HttpEntity<>("", userHeader);

        String url = "/users/all";

        ResponseEntity<String> noAuthResponse = restTemplate.exchange(url, HttpMethod.GET, unauthEntity, String.class);
        ResponseEntity<String> adminResponse = restTemplate.exchange(url, HttpMethod.GET, authEntity, String.class);
        ResponseEntity<String> userResponse = restTemplate.exchange(url, HttpMethod.GET, userEntity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, noAuthResponse.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, userResponse.getStatusCode());
        assertEquals(HttpStatus.OK, adminResponse.getStatusCode());

        ArrayList<User> _users = JsonPath.read(adminResponse.getBody(), "$");
        for (int i = 0; i < _users.size(); i++) {
            assertEquals(asParsedJson(users.get(i)), _users.get(i));
        }
    }

    @Test
    void createUserTests() throws Exception {
        ArrayList<User> userList = new ArrayList<User>();

        when(userDB.save(any(User.class)))
            .thenAnswer((u) -> {
                User i = u.getArgument(0);
                if (userList.stream().anyMatch(j -> i.getId() == j.getId())) {
                    throw new DataIntegrityViolationException("Error: User exists!");
                }
                userList.add(i);
                return u.getArgument(0);
            });

        HttpEntity<User> unauthEntity = new HttpEntity<>(user1, new HttpHeaders());
        HttpEntity<User> authEntity = new HttpEntity<>(user1, adminHeader);
        HttpEntity<User> userEntity = new HttpEntity<>(user2, userHeader);
        HttpEntity<User> nullEntity = new HttpEntity<>(new User(), adminHeader);

        String url = "/users";

        ResponseEntity<String> noAuthResponse = restTemplate.exchange(url, HttpMethod.POST, unauthEntity, String.class);
        ResponseEntity<String> adminResponse = restTemplate.exchange(url, HttpMethod.POST, authEntity, String.class);
        ResponseEntity<String> userResponse = restTemplate.exchange(url, HttpMethod.POST, userEntity, String.class);
        ResponseEntity<String> repeatedResponse = restTemplate.exchange(url, HttpMethod.POST, userEntity, String.class);
        ResponseEntity<String> nullResponse = restTemplate.exchange(url, HttpMethod.POST, nullEntity, String.class);

        assertEquals(HttpStatus.FORBIDDEN, noAuthResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED, adminResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED, userResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, repeatedResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, nullResponse.getStatusCode());
    }

    @FunctionalInterface
    interface UserLambda {
        public void invoke(User user);
    }

    void updateUserMethod(String url, UserLambda lambda) {
        User updatedUser = new User();
        lambda.invoke(updatedUser);

        HttpEntity<User> authEntity = new HttpEntity<>(updatedUser, adminHeader);
        HttpEntity<User> userEntity = new HttpEntity<>(updatedUser, userHeader);

        ResponseEntity<String> adminResponse = restTemplate.exchange(url, HttpMethod.PUT, authEntity, String.class);
        ResponseEntity<String> userResponse = restTemplate.exchange(url, HttpMethod.PUT, userEntity, String.class);

        assertEquals(HttpStatus.FORBIDDEN, userResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, adminResponse.getStatusCode());
    }

    @Test
    void updateUsers() {
        when(userDB.save(any(User.class)))
            .thenAnswer((u) -> {
                User i = u.getArgument(0);
                User k = users.stream().filter(j -> i.getId() == j.getId()).findFirst().get();
                users.remove(k);
                users.add(i);
                return u.getArgument(0);
            });

        Name name = faker.name();
        Internet internet = faker.internet();
        User updatedUser = new User(2, userRole, name.firstName(), name.lastName(), name.username(), internet.emailAddress(),
                internet.password(), faker.phoneNumber().cellPhone());

        String url = "/users/3";

        updateUserMethod(url, (u) -> u.setRole(agentRole));
        updateUserMethod(url, (u) -> u.setGivenName(updatedUser.getGivenName()));
        updateUserMethod(url, (u) -> u.setFamilyName(updatedUser.getFamilyName()));
        updateUserMethod(url, (u) -> u.setUsername(updatedUser.getUsername()));
        updateUserMethod(url, (u) -> u.setEmail(updatedUser.getEmail()));
        updateUserMethod(url, (u) -> u.setPassword(updatedUser.getPassword()));
        updateUserMethod(url, (u) -> u.setPhone(updatedUser.getPhone()));
    }

    @Test
    void deleteUsers() {
        doAnswer(u -> {
            try {
                User i = u.getArgument(0);
                User removedUser = users.stream()
                    .filter(k -> k.getId() == i.getId())
                    .findFirst().get();

                users.remove(removedUser);
            } catch (Exception e) {
                throw new DataIntegrityViolationException(e.getMessage());
            }

            return null;
        }).when(userDB).delete(any(User.class));

        HttpEntity<String> unauthEntity = new HttpEntity<>("", new HttpHeaders());
        HttpEntity<String> authEntity = new HttpEntity<>("", adminHeader);
        HttpEntity<String> userEntity = new HttpEntity<>("", userHeader);

        for (int i = 0; i < users.size(); i++) {
            int id = users.get(0).getId();
            String url = "/users/" + id;

            ResponseEntity<String> noAuthResponse = restTemplate.exchange(url, HttpMethod.DELETE, unauthEntity, String.class);
            ResponseEntity<String> adminResponse = restTemplate.exchange(url, HttpMethod.DELETE, authEntity, String.class);
            ResponseEntity<String> userResponse = restTemplate.exchange(url, HttpMethod.DELETE, userEntity, String.class);

            assertEquals(HttpStatus.NO_CONTENT, adminResponse.getStatusCode());
            assertEquals(HttpStatus.UNAUTHORIZED, noAuthResponse.getStatusCode());

            // Bad Request since user should be deleted, unauthorized otherwise
            assertEquals(id == user1.getId() ? HttpStatus.BAD_REQUEST : HttpStatus.UNAUTHORIZED, userResponse.getStatusCode());
        }
    }
}
