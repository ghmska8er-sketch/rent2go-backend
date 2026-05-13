package app.web.rtgtechnologies.rent2go.iam.infrastructure.authorization;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedRequestHandlerEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String original = (String) request.getAttribute("javax.servlet.error.request_uri");
        String path = original != null ? original : request.getRequestURI();
        LOGGER.warn("Unauthorized access to {} - reason: {}", path, authException == null ? "unknown" : authException.getMessage());
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String body = String.format("{\"error\":\"Unauthorized\",\"path\":\"%s\"}", path);
        response.getWriter().write(body);
    }
}
