# Spring Security Configuration Guide

Детальное описание ключевых методов настройки безопасности в Spring Security 6+.

---

## 1. csrf()

**Пример:**
```
    /// for default stateful applications.
    http.csrf(csrf -> csrf 
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .ignoringRequestMatchers("/api/public/**")
    );
    
    http.csrf(csrf -> csrf.disable()) // for RestApi applications.
```

**Цель**
```
    Защита от подделки межсайтовых запросов (CSRF).

    Генерирует и валидирует CSRF-токены для stateful-приложений

    Обязателен для форм с аутентификацией через сессии

    Для REST API (stateless) отключается через .disable()
```

## Внутренние настройки CSRF

1. **csrf.disable()**:

Назначение: Отключает защиту от CSRF.

Когда используется: Обычно применяется в приложениях с stateless архитектурой (например, REST API), где нет сессий, и защита от CSRF не нужна.

Пример использования:
```
    http.csrf().disable(); // Отключение CSRF защиты
```
2. **csrf.csrfTokenRepository(CsrfTokenRepository csrfTokenRepository)**:

Назначение: Устанавливает репозиторий для хранения и извлечения CSRF токенов.

Когда используется: Это используется для указания, как и где будут храниться CSRF токены (например, в cookies, сессиях и т.д.).

Пример использования:
```
    http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
```
Варианты:

CookieCsrfTokenRepository: Сохраняет токен в cookies.

HttpSessionCsrfTokenRepository: Сохраняет токен в HTTP-сессии.

RequestAttributeCsrfTokenRepository: Сохраняет токен в атрибутах запроса.

3. **csrf.ignoringRequestMatchers(RequestMatcher... requestMatchers)**:

Назначение: Исключает указанные URL-адреса из проверки CSRF токенов.

Когда используется: Это нужно, когда вы хотите разрешить доступ к определенным маршрутам без проверки CSRF. Это часто используется для публичных API или внешних ресурсов, которые не изменяют состояние (например, при аутентификации через токены).

Пример использования:
```
    http.csrf().ignoringRequestMatchers("/api/public/**");
```
4. **csrf().requireCsrfProtectionMatcher(RequestMatcher requestMatcher)**:

Назначение: Устанавливает собственное условие для того, чтобы CSRF защита применялась только к определенным запросам.

Когда используется: Это позволяет настроить защиту от CSRF только для определенных HTTP-методов или маршрутов.

Пример использования:
```
    http.csrf().requireCsrfProtectionMatcher(new AntPathRequestMatcher("/api/protected/**"));
```

## 2. cors()
**Пример:**
```
    http.csrf(csrf -> csrf.disable())  // Отключение CSRF
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Настройка CORS
        .build();
```

**Цель**
```
   CORS — это механизм, который позволяет веб-страницам делать запросы к ресурсам с другого домена (кросс-доменные запросы). 
   Веб-браузеры блокируют кросс-доменные запросы, если сервер не разрешает их явно. 
   CORS используется для определения политики, разрешающих или запрещающих такие запросы для вашего приложения.
```

## Внутренние настройки CORS
1. **cors().configurationSource(CorsConfigurationSource configurationSource)**:

Назначение: Этот метод позволяет указать источник конфигурации CORS, который будет отвечать за определение того, какие CORS-запросы разрешены.

Когда используется:Применяется, когда необходимо настроить политику CORS с использованием собственного источника конфигурации.

Пример использования:
```
    http.cors().configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList("https://trusted-site.com"));
    config.setAllowedMethods(Arrays.asList("GET", "POST"));
    return config;
});

 или же 

 @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://example.com")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); 
        configuration.setExposedHeaders(Arrays.asList("Authorization")); 
        configuration.setAllowCredentials(true); 
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    и добавлением этого бина в http.cors.configurationSource(corsConfigurationSource())
```

Возможные значения:

configuration.setAllowedOrigins(Arrays.asList("https://example.com")); - Разрешает, с каких доменов можно отправлять запросы к вашему серверу.

configuration.setAllowedMethods(Arrays.asList("GET", "POST")); - Определяет, какие HTTP-методы разрешены при кросс-доменных запросах.

configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); - Какие заголовки могут быть отправлены с клиентского запроса.

configuration.setExposedHeaders(Arrays.asList("Authorization")); - Позволяет клиенту (браузеру) видеть указанные заголовки в ответе сервера. По умолчанию браузеры скрывают почти все заголовки из кросс-доменных ответов.

configuration.setAllowCredentials(true); - Разрешает/запрещает отправку cookies и заголовков авторизации (например, Authorization) в запросах.

configuration.setMaxAge(3600L); - Устанавливает, сколько секунд результат preflight-запроса (OPTIONS) может кэшироваться браузером.

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); - Привязывает CORS-настройку к определённым путям (в данном случае /**, то есть ко всем).

2. **cors().disable()**:

Назначение: Отключает настройку CORS для приложения.

Когда используется: Применяется, если в проекте нет необходимости в разрешении CORS-запросов или если CORS будет настроен вручную через другие компоненты.

Пример использования:
```
    http.cors().disable(); // Отключение поддержки CORS
```

## 3. **http.formLogin()**:

Назначение: Конфигурирует аутентификацию с использованием стандартной формы входа.

Когда используется: Когда необходимо реализовать аутентификацию через форму (например, для веб-приложений).

Пример использования:
```
    http.formLogin()
    .loginPage("/login")  // Страница для входа
    .loginProcessingUrl("/login")  // URL для обработки логина
    .defaultSuccessUrl("/home", true)  // Страница, куда пользователь перенаправляется после успешного входа
    .failureUrl("/login?error=true");  // Страница для неудачной попытки входа
```

## 4. **http.httpBasic()**:

Назначение: Настройка аутентификации через базовую HTTP-аутентификацию (передача логина и пароля в заголовках).

Когда используется: Обычно используется для API, где клиент передает логин и пароль в заголовке HTTP-запроса.

Пример использования:
```
    http.httpBasic();  // Включает Basic Authentication
```

## 5. **http.logout()**:

Назначение: Настройка процесса выхода пользователя из приложения.

Когда используется: Когда необходимо настроить логику выхода из системы (например, очистка сессии, редирект и т.д.)

Пример использования:
```
    http.logout()
    .logoutUrl("/logout")  // URL для выхода
    .logoutSuccessUrl("/login?logout=true")  // Страница для редиректа после успешного выхода
    .invalidateHttpSession(true)  // Инвалидирует сессию пользователя
    .clearAuthentication(true);  // Очищает аутентификацию
```
## 6. **http.authorizeRequests()**:

Назначение: Настройка, какие роли или пользователи имеют доступ к каким URL.

Когда используется: Когда необходимо настроить доступ к различным частям приложения на основе ролей или прав.

Пример использования:
```
    http.authorizeRequests()
        .antMatchers("/admin/**").hasRole("ADMIN")  // Доступ только для пользователей с ролью ADMIN
        .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")  // Доступ для пользователей с ролью USER или ADMIN
        .anyRequest().authenticated();  // Все остальные запросы требуют аутентификации
```

## 7. **http.sessionManagement()**:

Назначение: Управление сессиями, включая создание, уничтожение и лимитирование сессий.

Когда используется: Когда необходимо настроить, как управлять сессиями (например, ограничения на количество сессий).

Пример использования:
```
    http.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Сессия создается только если это необходимо
        .maximumSessions(1)  // Максимум 1 активная сессия на пользователя
        .expiredUrl("/session-expired");  // URL при истечении срока действия сессии
```

Возможные значения:

SessionCreationPolicy.ALWAYS — сессия создается всегда.

SessionCreationPolicy.IF_REQUIRED — сессия создается только в случае необходимости.

SessionCreationPolicy.NEVER — сессия не создается вообще.

SessionCreationPolicy.STATELESS — приложение работает без сессий.


## 8. http.rememberMe():

Назначение: Настройка аутентификации с использованием механизма "Запомнить меня", чтобы пользователь оставался авторизованным после выхода.

Когда используется: Когда необходимо обеспечить, чтобы пользователь оставался авторизованным даже после закрытия браузера.

Пример использования:
```
    http.rememberMe()
        .key("mySecretKey")  // Уникальный ключ для генерации токенов "Запомнить меня"
        .tokenValiditySeconds(1209600);  // Время действия токена (2 недели)
```
## 9. http.addFilterBefore()

Назначение: Позволяет вставить пользовательский фильтр в цепочку фильтров безопасности перед другим фильтром.

Когда используется: Когда нужно добавить дополнительный фильтр, который должен быть выполнен до определенного фильтра, например, до фильтра аутентификации.

Пример:
```
    http.addFilterBefore(new MyCustomFilter(), UsernamePasswordAuthenticationFilter.class);
```

## 10. http.addFilterAfter()

Назначение: Добавляет пользовательский фильтр после другого фильтра в цепочке фильтров.

Когда используется: Когда нужно добавить фильтр, который должен быть выполнен после стандартного фильтра безопасности, например, после фильтра аутентификации.

Пример:
```
    ttp.addFilterAfter(new MyCustomFilter(), UsernamePasswordAuthenticationFilter.class);
```

## 11.http.exceptionHandling()

Назначение: Настройка обработки исключений для различных типов ошибок аутентификации и авторизации.

Когда используется: Для задания специфической обработки ошибок, таких как неавторизованные запросы или ошибки аутентификации.

Пример:
```
    http.exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));  // Устанавливает точку входа при ошибке аутентификации
```

## 12. http.headers()

Назначение: Управление HTTP-заголовками безопасности.

Когда используется: Когда нужно усилить безопасность приложения через настройки HTTP-заголовков (например, защита от XSS, Clickjacking и т. д.).

Пример:
```
    http.headers()
        .frameOptions().deny()  // Защита от Clickjacking
        .httpStrictTransportSecurity().maxAgeInSeconds(31536000)  // Защита через HSTS
        .xssProtection().block(true);  // Защита от XSS атак
```

Возможные значения:

frameOptions().deny() — предотвращает использование приложения внутри фреймов (обезопасит от атак типа Clickjacking).

httpStrictTransportSecurity() — включает защиту через HSTS (HTTP Strict Transport Security), предотвращая атакующие попытки работать с HTTP, вместо HTTPS.

xssProtection().block(true) — блокирует выполнение скриптов на странице для защиты от атак XSS.

## 13. http.securityContext()

Назначение: Настройка контекста безопасности.

Когда используется: Для управления тем, как будет храниться контекст безопасности (например, аутентификационные данные) между запросами.

Пример:
```
    http.securityContext().securityContextRepository(new HttpSessionSecurityContextRepository());
```

## 14. http.requestCache()

Назначение: Управление кешированием запросов.

Когда используется: Когда необходимо сохранить запросы пользователя для последующего возвращения к ним после успешной аутентификации.

Пример:
```
    http.requestCache().requestCache(new NullRequestCache());  // Отключение кеширования запросов
```

## 15. http.authenticationManager()

Назначение: Настройка менеджера аутентификации, который будет обрабатывать аутентификацию.

Когда используется: Когда нужно использовать кастомный менеджер аутентификации для настройки процесса аутентификации, например, с кастомной логикой.

Пример:
```
    http.authenticationManager(authenticationManager());
```

## 16.http.userDetailsService()

Назначение: Установка кастомного сервиса для загрузки пользовательских данных.

Когда используется: Когда необходимо использовать свой собственный сервис для извлечения данных пользователя (например, из базы данных).

Пример:
```
    http.userDetailsService(userDetailsService);
```

## 17.http.addFilter()

Назначение: Добавление фильтра в цепочку фильтров Spring Security.

Когда используется: Когда необходимо использовать кастомные фильтры для обработки запросов.

Пример:
```
    http.addFilter(new MyCustomFilter());
```

## 18. http.authenticationProvider()

Назначение: Установка кастомного провайдера аутентификации.

Когда используется: Когда нужно использовать кастомный провайдер аутентификации, например, для интеграции с внешними системами или кастомной логикой аутентификации.

Пример:
```
    http.authenticationProvider(new MyCustomAuthenticationProvider());
```

## 19. http.portMapper()

Назначение: Настраивает отображение портов HTTP ↔ HTTPS для автоматического перенаправления между ними.

Когда используется: Когда приложение поддерживает как HTTP, так и HTTPS, и требуется безопасное перенаправление (например, с порта 8080 на 8443).

Пример:
```
    http.portMapper(mapper -> mapper
        .http(8080).mapsTo(8443)
        .http(80).mapsTo(443)
    );
```

## 20.http.requiresChannel()

Назначение: Указывает, какие каналы (http/https) требуются для конкретных запросов.

Когда используется: Для защиты чувствительных маршрутов, где необходимо использовать только HTTPS.

Пример:
```
   http.requiresChannel(channel -> channel
        .requestMatchers("/secure/**").requiresSecure() // только HTTPS
        .anyRequest().requiresInsecure()                // только HTTP
    );
```

## 21.http.anonymous()

Назначение:
Позволяет обрабатывать неаутентифицированных пользователей как "анонимных" с определённой ролью.

Когда используется:
Когда нужно предоставить ограниченный доступ (например, чтение) неаутентифицированным пользователям.

Пример:
```
   http.anonymous(anon -> anon
        .principal("guestUser") // Устанавливает имя для анонимного пользователя
        .authorities("ROLE_GUEST") // Устанавливает роль для анонимного пользователя
    );
```

## 22.http.oauth2Login()

Назначение:
Настройка входа с использованием OAuth2 (Google, GitHub и т.д.).

Когда используется:
Для интеграции с внешними поставщиками аутентификации.

Пример:
```
   http.oauth2Login(oauth2 -> oauth2
        .loginPage("/oauth2/authorization/mysite")
        .defaultSuccessUrl("/home", true)
    );
```

## 23.http.oauth2ResourceServer()

Назначение:
Настройка Spring Security как ресурсо-сервера (Resource Server) в архитектуре OAuth2.

Когда используется:
Когда нужно защищать REST API и проверять JWT или opaque-токены от внешнего авторизационного сервера.

Пример (JWT):
```
   http.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt
            .jwkSetUri("https://auth-server.com/.well-known/jwks.json")
        )
    );
```

## 24.http.passwordManagement()
Назначение:
Управление функциональностью восстановления пароля и управления им (в рамках нового PasswordManagementConfigurer в Spring Security 6+).

Когда используется:
Если вы хотите предоставить возможность пользователю сбросить или обновить пароль в рамках security-фреймворка.

Пример:
```
   http.passwordManagement(passwordManagement -> passwordManagement
            .changePasswordPage("/change-password")  // Своя страница смены пароля
            .changePasswordProcessingUrl("/perform-password-change")  // URL, куда отправляется POST запрос
            .passwordChangeHandler((request, response, authentication) -> {
                // можно сделать логирование, редирект или другую кастомную логику
                response.sendRedirect("/password-changed-success");
            })
        );
```

## 25.servletApi()

Метод servletApi() в Spring Security используется для включения поддержки Servlet API, чтобы настройки безопасности интегрировались с приложением на основе сервлетов. Этот метод включается в конфигурацию безопасности для обеспечения того, чтобы Spring Security мог работать в приложениях, использующих сервлеты (например, веб-приложения на основе JSP или сервлетов).

В Spring Security servletApi() используется для интеграции с Servlet API, обеспечивая дополнительные функции безопасности для приложений, которые не используют WebFlux или другие нетрадиционные технологии для обработки запросов. Это полезно для классических веб-приложений, использующих сервлеты для обработки HTTP-запросов.

Что делает servletApi():

Открывает доступ к HTTP-сервлету: Этот метод интегрирует Spring Security с стандартным Servlet API, что позволяет использовать различные стандартные API-сервлеты (например, HttpServletRequest, HttpServletResponse и другие), которые нужны для обработки HTTP-запросов.

Поддержка Session Tracking: Включает возможность отслеживания сессий в приложении, используя стандартные возможности сервлетов. Это особенно важно для аутентификации и управления сессиями пользователей.

Взаимодействие с FilterChain: Spring Security интегрируется с фильтрами сервлетов через механизм фильтров, что позволяет добавить дополнительные фильтры безопасности в цепочку обработки запросов.

Пример:
```
   http.servletApi()
    .authorizeRequests()
        .antMatchers("/secure/**").authenticated()
        .anyRequest().permitAll();

```