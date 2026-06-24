package app.web.rtgtechnologies.rent2go.iam.infrastructure.authorization;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ForbiddenAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForbiddenAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        String path = request.getRequestURI();
        String reason = ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "insufficient role";
        LOGGER.warn("Access denied to {} - reason: {}", path, reason);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String body = String.format(
            "{\"error\":\"Forbidden\",\"message\":\"You do not have permission to perform this action. This endpoint requires the ADMIN role.\",\"reason\":\"%s\",\"path\":\"%s\"}",
            reason, path
        );
        response.getWriter().write(body);
    }
}
