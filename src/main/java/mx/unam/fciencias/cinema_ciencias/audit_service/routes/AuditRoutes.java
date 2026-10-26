package mx.unam.fciencias.cinema_ciencias.audit_service.routes;

import mx.unam.fciencias.cinema_ciencias.audit_service.model.AuditLog;
import mx.unam.fciencias.cinema_ciencias.audit_service.repository.AuditRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Configuration
public class AuditRoutes {

    @Bean
    public RouterFunction<ServerResponse> auditLogRoutes(AuditRepository repository) {
        return route()
                .POST("/api/logs", request -> {
                    var logRequest = request.body(AuditLog.class);
                    var savedLog = repository.save(logRequest);
                    return ok().body(savedLog);
                })
                .build();
    }
}