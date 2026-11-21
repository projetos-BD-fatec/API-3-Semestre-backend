package br.com.bughunters.fusexflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita os jobs agendados (ex.: marcar guias vencidas). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
