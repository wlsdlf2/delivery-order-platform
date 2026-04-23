package com.sparta.deliveryorderplatform.auth.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Getter
public class JwtTokenProvider {

	private final Key key; // 토큰을 암호화/복호화할 때 사용하느느 key
	private final long accessTokenExpiration;  // 액세스 토큰 유효 기간
	private final long refreshTokenExpiration; // 리프레시 토큰 유효 기간

	public JwtTokenProvider (
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.access-expiration-min}") long accessTokenMin,
		@Value("${jwt.refresh-expiration-days}") long refreshTokenDays
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.accessTokenExpiration = accessTokenMin * 60 * 1000L;
		this.refreshTokenExpiration = refreshTokenDays * 24 * 60 * 60 * 1000L;
	}

	/*
	 * 액세스 토큰 생성
	 * 사용자가 로그인에 성공하면 해당 사용자의 username과 권한을 Token에 담는다.
	 */
	public String createAccessToken (String username, UserRole role) {
		Date now = new Date();
		return Jwts.builder()
			.setSubject(username)						// Token의 주인 설정
			.claim("auth", role.getAuthority())	// 사용자의 권한을 'auth'라는 이름으로 저장
			.setIssuedAt(now)							// Token 발행 시간 기록
			.setExpiration(new Date(now.getTime() + accessTokenExpiration))	// Token 만료 시간 설정
			.signWith(key, SignatureAlgorithm.HS256)	// 서버의 key로 서명(암호화)
			.compact();									// 최종적인 문자열 형태로 변환
	}

	/*
	 * 리프레시 토큰 생성
	 * 액세스 토큰이 만료되었을 때, 다시 발급받기 위한 용도
	 */
	public String createRefreshToken (String username) {
		return createToken(username, refreshTokenExpiration);
	}

	// 공통 토큰 로직 생성
	public String createToken (String subject, long expire) {
		Date now = new Date();
		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + expire))
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();
	}

	/*
	 * 토큰 검증
	 * 들어온 토큰이 서버가 발행한 토큰이 맞는지, 위조되지는 않았는지 확인한다.
	 */
	public boolean validateToken (String token) {
		// 서버의 key로 토큰을 복호화가 가능하면 true, 불가능하면 에러 발생
		Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
		return true;
	}

	/*
	 * 사용자 아이디 추출
	 * 토큰에서 사용자 아이디를 추출한다.
	 */
	public String getUsername (String token) {
		return Jwts.parserBuilder().setSigningKey(key).build()
			.parseClaimsJws(token).getBody().getSubject();
	}

	/*
	 * 사용자 권한 추출
	 * 토큰에서 사용자 권한을 추출한다.
	 */
	public String getRole (String token) {
		return Jwts.parserBuilder().setSigningKey(key).build()
			.parseClaimsJws(token).getBody().get("auth", String.class);
	}

	/*
	 * 만료 여부 확인
	 * 토큰의 유효 시간이 지났는지 따로 체크한다.
	 */
	public boolean isTokenExpired (String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return false;
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
