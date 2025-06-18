package com.spring.security.configuration;

import com.spring.security.jwt.JWTFilter;
import com.spring.security.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration

//Включает методовую безопасность, т.е. аннотации вроде:
//@PreAuthorize("hasRole('ADMIN')")
//
//@PostAuthorize(...)
//
//@Secured("ROLE_ADMIN")
//
//@RolesAllowed(...)
@EnableMethodSecurity

//Включает веб-безопасность — т.е. безопасность HTTP-запросов.
//Подключает фильтры Spring Security (например, для авторизации, аутентификации, CSRF, CORS).
//Позволяет настраивать правила доступа по URL:
@EnableWebSecurity

@RequiredArgsConstructor
public class SecurityConfiguration {
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationConfiguration authenticationConfiguration,
                                                   JWTFilter jwtFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(configuration -> configuration.configurationSource(
                        request -> new CorsConfiguration().applyPermitDefaultValues())
                )
                .anonymous(configuration -> configuration
                        .principal("guest")
                        .authorities("ROLE_GUEST")) // выдаю имя, и его права не зарегестрированному пользователю для полного контроля действий
                .formLogin(AbstractHttpConfigurer::disable) // делаю кастомный JWT. formLogin подходит толлько для дефолтного редиректа и сессий.
                .logout(configuration -> {
                            configuration.logoutUrl("/logout");
                            configuration.logoutSuccessUrl("/first-page");
                            configuration.clearAuthentication(true); // очищаем spring-context
                            configuration.permitAll();
                        }
                ) //тут мы зачастую будем использовать логаут, по причине того что удаление из локального хранилища лежит на фронтенде.
                //В JWT-архитектуре logout чаще бывает stateless — т.е. вызов /logout не обязателен, но я его добавил для того чтобы показать как его конфигурировать.
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests.requestMatchers("/user").hasRole("USER");
                    authorizeRequests.requestMatchers("/admin").hasRole("ADMIN");
                    authorizeRequests.requestMatchers("/auth/registration", "/auth/authenticate", "/auth/refresh-token").permitAll();
                })
                .exceptionHandling(exceptions -> {
                    exceptions.authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    });

                    exceptions.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Forbidden\"}");
                    });
                    //прописываем кастомные ошибки на аутентификацию + форбиден, желательно вынести в отдельный класс. Тут просто показано что так можно делать
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Отключаем хранение сессий в Spring Security: каждый запрос аутентифицируется независимо (stateless).Это необходимо для REST API с JWT, чтобы не использовать HTTP-сессии и обеспечить масштабируемость.
                .passwordManagement(AbstractHttpConfigurer::disable) // к сожалению мы опять используем JWT тут  - passwordManagement работает только с формой логина и сессиями.
                .authenticationManager(authenticationManager(authenticationConfiguration)) // можно инициализировать его тут, можно забить хуй ведь спринг и так найдет бин прописанный ниже
                .authenticationProvider(authenticationProvider()) // тоже самое можно тут дописать, можно хуй забить.
                // забыл упомянуть в новых можно опустить, так как Spring Boot 3+ автоматически конфигурирует бин,
                // в старых требуется конкретная инициализации в фильтер чейне
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // прописываем тут фильтр, который будет срабатывать на каждый запрос приходящий на наше приложение
                .build();
    }

    @Bean
    // используется Spring Security для загрузки данных пользователя. В данном случае,
    // UserDetailsService возвращает информацию о пользователе по его имени (пользовательский запрос для аутентификации).
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    @Bean
    //Здесь создается бин для энкодера паролей. В данном случае используется BCryptPasswordEncoder
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //Этот метод создает бин DaoAuthenticationProvider, который является стандартным провайдером аутентификации для Spring Security.
    //Он выполняет аутентификацию пользователей, используя UserDetailsService и PasswordEncoder.
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    //AuthenticationManager управляет процессом аутентификации. Он обрабатывает входящие запросы аутентификации,
    //используя настроенные AuthenticationProvider (например, DaoAuthenticationProvider),
    //чтобы проверить правильность учетных данных пользователя (например, имя пользователя и пароль).
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //Итог
    //UserDetailsService — это компонент, который отвечает за загрузку данных пользователя (например, из базы данных).
    //
    //PasswordEncoder — это компонент, который используется для безопасного хеширования паролей.
    //
    //DaoAuthenticationProvider — это компонент, который управляет аутентификацией пользователя, используя UserDetailsService и PasswordEncoder.
    //
    //AuthenticationManager — это компонент, который координирует процесс аутентификации и использует все настроенные провайдеры для проверки данных.
}

//Все что касается методов внутри секьюрити фильтер чейна. Можно найти в ReadMe файле или почитать в репозитории гитхаба.
//Все что касается методов внутри секьюрити фильтер чейна. Можно найти в ReadMe файле или почитать в репозитории гитхаба.
//Все что касается методов внутри секьюрити фильтер чейна. Можно найти в ReadMe файле или почитать в репозитории гитхаба.
