
package br.com.bughunters.fusexflow.config;

import br.com.bughunters.fusexflow.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Permite o tratamento interno de erros
                        .dispatcherTypeMatchers(DispatcherType.ERROR)
                        .permitAll()

                        // Cadastro de usuário
                        .requestMatchers(HttpMethod.POST, "/usuarios")
                        .permitAll()

                        // Login de usuário
                        .requestMatchers(HttpMethod.POST, "/auth/login")
                        .permitAll()

                        // Cadastro de OCS (prestador + responsável)
                        .requestMatchers(HttpMethod.POST, "/ocs")
                        .permitAll()

                        // Login de Prestador
                        .requestMatchers(HttpMethod.POST, "/prestador/login")
                        .permitAll()

                        // Consultas gerais e encaminhamentos do colaborador
                        .requestMatchers(
                                HttpMethod.GET,
                                "/pre-guias",
                                "/pre-guias/fusex",
                                "/pre-guias/fusex/**"
                        )
                        .hasAnyRole("FUSEX", "ADMIN")

                        // Somente colaboradores autorizados podem decidir
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/pre-guias/fusex/*/decisao"
                        )
                        .hasAnyRole("FUSEX", "ADMIN")

                        // Emissão de guias oficiais
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/guias/upload"
                        )
                        .hasAnyRole("FUSEX", "ADMIN")

                        // Demais rotas exigem autenticação
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
