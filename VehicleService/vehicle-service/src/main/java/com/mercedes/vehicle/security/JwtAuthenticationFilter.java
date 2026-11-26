
package com.mercedes.vehicle.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.vehicle.repository.RevokedTokenRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final RevokedTokenRepository revoked;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtAuthenticationFilter(
            JwtService jwt,
            RevokedTokenRepository revoked
    ) {
        this.jwt = jwt;
        this.revoked = revoked;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return request.getMethod().equalsIgnoreCase("OPTIONS")
                || path.equals("/actuator/health")
                || path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            writeError(
                    res,
                    401,
                    "A valid Bearer token is required"
            );
            return;
        }

        String token = header.substring(7);

        try {

            if (!jwt.isValid(token)) {
                writeError(res, 401, "Invalid or expired token");
                return;
            }

            var claims = jwt.parse(token);

            String jti = claims.getId();

            if (jti != null && revoked.existsById(jti)) {
                writeError(res, 401, "Token has been revoked");
                return;
            }

            String userId = claims.get("userId", String.class);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if (userId == null || userId.isBlank()) {
                writeError(res, 401, "JWT is missing userId claim");
                return;
            }

            req.setAttribute(
                    "currentUser",
                    new CurrentUser(userId, email, role, jti)
            );

            chain.doFilter(req, res);

        } catch (Exception ex) {

            writeError(
                    res,
                    401,
                    "Unable to authenticate token"
            );
        }
    }

    private void writeError(
            HttpServletResponse res,
            int status,
            String message
    ) throws IOException {

        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);

        mapper.writeValue(
                res.getOutputStream(),
                Map.of(
                        "status", status,
                        "error", "Unauthorized",
                        "message", message
                )
        );
    }
}
