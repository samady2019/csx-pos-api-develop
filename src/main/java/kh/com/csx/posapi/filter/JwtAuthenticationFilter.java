package kh.com.csx.posapi.filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.com.csx.posapi.constant.ResponseCode;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.model.ErrorResponse;
import kh.com.csx.posapi.repository.UserRepository;
import kh.com.csx.posapi.service.JwtService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;
            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request,response);
                return;
            }
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserEntity userEntity = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                if (jwtService.isTokenValid(jwt,userEntity)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEntity,
                            null,
                            userEntity.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request,response);
        } catch (RuntimeException e){
            ErrorResponse errorResponse = new ErrorResponse("Error occurred");
            ObjectMapper mapper = new ObjectMapper();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            if (e instanceof UsernameNotFoundException) {
                errorResponse.setMessage("User not found");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            } else if (e instanceof ExpiredJwtException) {
                errorResponse.setMessage("Your token has expired. Please login again.");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            } else if (e instanceof SignatureException){
                errorResponse.setMessage("Invalid token");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            } else {
                errorResponse.setMessage("Internal Server Error");
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            String jsonResponse = mapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
        }
    }
}
