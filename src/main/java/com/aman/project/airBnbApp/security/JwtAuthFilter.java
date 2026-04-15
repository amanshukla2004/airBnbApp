package com.aman.project.airBnbApp.security;

import com.aman.project.airBnbApp.entity.User;
import com.aman.project.airBnbApp.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JWTService jwtService;
	private final UserService userService;

	// Question : for jwt exception we need a HandlerExceptionResolver
	@Autowired
	@Qualifier("handlerExceptionResolver")
	private HandlerExceptionResolver handlerExceptionResolver;

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		try {
			final String requestTokenHeader = request.getHeader("Authorization");

			// 1. Check if the header is missing, wrong format, or too short to be a JWT
			if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ") || requestTokenHeader.length() <= 7) {
				filterChain.doFilter(request, response);
				return;
			}

			// 2. Extract the token safely
			final String token = requestTokenHeader.substring(7);

			// extra check: a JWT MUST have periods. if not, don't even try to parse
			if (!token.contains(".")) {
				filterChain.doFilter(request, response);
				return;
			}

			Long userId = jwtService.getUserIdFromToken(token);

			// 3. If userId exists and they aren't authenticated yet...
			if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				User user = userService.getUserById(userId);
				
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					user,
					null,
					user.getAuthorities()
				);

				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
			filterChain.doFilter(request, response);
		} catch (JwtException ex) {
			// If it's a genuine JWT error (expired, etc.), we resolve the exception
			handlerExceptionResolver.resolveException(request, response, null, ex);
		} catch (Exception ex) {
			// fallback for any other unexpected errors
			filterChain.doFilter(request, response);
		}
	}
}
