package com.ss.utopia.restapi.configs;

//TODO(Angel): Implement JWT Auth

// import com.ss.utopia.restapi.dao.UserRepository;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
//     UserRepository userDB;

//     public SecurityConfiguration(UserRepository userDB) {
//         this.userDB = userDB;
//     }

//     @Override
//     protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//         // TODO Auto-generated method stub
//         super.configure(auth);
//     }

//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         http.csrf()
//             .disable()
//             .sessionManagement()
//             .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//             .and()
//             .authorizeRequests()
//             .antMatchers(HttpMethod.POST, "/login").permitAll()
//             .antMatchers("");
//     }

//     @Bean
//     PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }
// }
