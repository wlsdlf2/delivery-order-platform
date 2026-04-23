package com.sparta.deliveryorderplatform.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sparta.deliveryorderplatform.global.exception.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	/*
	 * 필터의 핵심 로직
	 * 모든 요청은 컨트롤러에 도달하기 전 이 메서드를 거친다.
	 */
	@Override
	protected void doFilterInternal (
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		// 1. 요청 헤더에서 JWT 토큰을 꺼내온다.
		String token = extractToken(request);

		// 2. 토큰이 존재하고 유효하다면 인증을 진행한다.
		if (token != null) {
			try {
				if (jwtTokenProvider.validateToken(token)) {
					// 토큰에서 사용자 아이디와 권한을 꺼낸다.
					String username = jwtTokenProvider.getUsername(token);
					String role = jwtTokenProvider.getRole(token);

					// 3. Spring Security가 인식할 수 있는 Authority 리스트를 만든다.
					List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

					// 4. 인증된 사용자 정보를 담은 인증 객체(Authentication)를 생성한다.
					// (비밀번호는 이미 토큰으로 검증되었으므로 null로 둔다.)
					UsernamePasswordAuthenticationToken auth =
						new UsernamePasswordAuthenticationToken(username, null, authorities);

					// 5. SecurityContext에 이 인증 정보를 담아둔다.
					// 이제 이후 로직에서 이 사용자가 누구인지 알 수 있게 된다.
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException e) {
				request.setAttribute("exception", ErrorCode.WRONG_TOKEN);
			} catch (io.jsonwebtoken.ExpiredJwtException e) {
				request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN);
			} catch (io.jsonwebtoken.UnsupportedJwtException e) {
				request.setAttribute("exception", ErrorCode.UNSUPPORTED_TOKEN);
			} catch (Exception e) {
				// 인증 정보 삭제
				SecurityContextHolder.clearContext();
				request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
				log.error("JWT 인증 과정에서 예외 발생: {}", e.getMessage());
			}
		}

		// 6. 다음 필터로 요청을 넘긴다.
		// 인증 정보가 없다면 이후 Security 설정에 의해 EntryPoint가 작동함
		filterChain.doFilter(request, response);
	}

	/*
	 * 헤더에서 토큰 추출
	 * 클라이언트가 보낸 Authorization 헤더에서 Bearer 부분을 떼고 실제 토큰값만 가져온다.
	 */
	private String extractToken (HttpServletRequest request) {
		// Authorization 헤더 (로컬 로그인 클라이언트)
		String bearer = request.getHeader("Authorization");

		// 헤더가 있고 "Bearer "로 시작한다면, 앞의 7글자(Bearer )를 제외한 나머지 토큰 문자열을 반환한다.
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}

}