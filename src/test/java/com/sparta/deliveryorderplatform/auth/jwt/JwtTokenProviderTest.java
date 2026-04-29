package com.sparta.deliveryorderplatform.auth.jwt;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

class JwtTokenProviderTest {

	// HS256 최소 256비트(32바이트) 이상 필요 — 64자 = 512비트
	private static final String TEST_SECRET =
		"testSecretKeyForJunitTestingPurposesOnlyMustBe256BitsOrLonger12345678";

	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, 30L, 7L);
	}

	@Test
	@DisplayName("액세스 토큰 생성 - username과 role 클레임이 포함된다")
	void createAccessToken_containsUsernameAndRole() {
		String token = jwtTokenProvider.createAccessToken("testuser", UserRole.CUSTOMER);

		assertThat(token).isNotBlank();
		assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("testuser");
		assertThat(jwtTokenProvider.getRole(token)).isEqualTo(UserRole.CUSTOMER.getAuthority());
	}

	@Test
	@DisplayName("액세스 토큰 생성 - OWNER 권한이 올바르게 담긴다")
	void createAccessToken_ownerRole_returnsOwnerAuthority() {
		String token = jwtTokenProvider.createAccessToken("owner1", UserRole.OWNER);

		assertThat(jwtTokenProvider.getRole(token)).isEqualTo(UserRole.OWNER.getAuthority());
	}

	@Test
	@DisplayName("리프레시 토큰 생성 - 유효한 토큰이 생성되고 username이 추출된다")
	void createRefreshToken_validToken() {
		String token = jwtTokenProvider.createRefreshToken("testuser");

		assertThat(token).isNotBlank();
		assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("testuser");
	}

	@Test
	@DisplayName("유효한 토큰 검증 - true를 반환한다")
	void validateToken_validToken_returnsTrue() {
		String token = jwtTokenProvider.createAccessToken("testuser", UserRole.CUSTOMER);

		assertThat(jwtTokenProvider.validateToken(token)).isTrue();
	}

	@Test
	@DisplayName("조작된 토큰 검증 - 예외가 발생한다")
	void validateToken_tamperedToken_throwsException() {
		String token = jwtTokenProvider.createAccessToken("testuser", UserRole.CUSTOMER);
		String tampered = token + "tampered";

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(tampered))
			.isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("잘못된 형식의 토큰 검증 - 예외가 발생한다")
	void validateToken_malformedToken_throwsException() {
		assertThatThrownBy(() -> jwtTokenProvider.validateToken("this.is.not.a.valid.jwt"))
			.isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("만료되지 않은 토큰 - isTokenExpired가 false를 반환한다")
	void isTokenExpired_freshToken_returnsFalse() {
		String token = jwtTokenProvider.createAccessToken("testuser", UserRole.CUSTOMER);

		assertThat(jwtTokenProvider.isTokenExpired(token)).isFalse();
	}

	@Test
	@DisplayName("만료된 토큰 - isTokenExpired가 true를 반환한다")
	void isTokenExpired_expiredToken_returnsTrue() throws InterruptedException {
		String token = jwtTokenProvider.createToken("testuser", 1L); // 1ms 후 만료
		Thread.sleep(10);

		assertThat(jwtTokenProvider.isTokenExpired(token)).isTrue();
	}

	@Test
	@DisplayName("서로 다른 사용자 토큰에서 username이 독립적으로 추출된다")
	void getUsername_differentUsers_returnsCorrectSubject() {
		String token1 = jwtTokenProvider.createAccessToken("user1a", UserRole.CUSTOMER);
		String token2 = jwtTokenProvider.createAccessToken("user2b", UserRole.OWNER);

		assertThat(jwtTokenProvider.getUsername(token1)).isEqualTo("user1a");
		assertThat(jwtTokenProvider.getUsername(token2)).isEqualTo("user2b");
	}

	@Test
	@DisplayName("남은 유효 시간 계산 - 만료 전이면 양수 밀리초를 반환한다")
	void getRemainingValidityMillis_future_returnsPositive() {
		// 1분(60,000ms) 유효한 토큰 생성
		String token = jwtTokenProvider.createToken("testuser", 60000L);

		long remaining = jwtTokenProvider.getRemainingValidityMillis(token);

		assertThat(remaining).isGreaterThan(0);
		assertThat(remaining).isLessThanOrEqualTo(60000L);
	}

	@Test
	@DisplayName("남은 유효 시간 계산 - 만료된 경우 0을 반환한다")
	void getRemainingValidityMillis_expired_returnsZero() throws InterruptedException {
		String token = jwtTokenProvider.createToken("testuser", 1L);
		Thread.sleep(10); // 만료 대기

		long remaining = jwtTokenProvider.getRemainingValidityMillis(token);

		assertThat(remaining).isEqualTo(0);
	}

	@Test
	@DisplayName("isTokenExpired - 잘못된 서명이나 형식이면 false를 반환한다 (catch 블록 커버)")
	void isTokenExpired_invalidFormat_returnsFalse() {
		// 이 테스트는 catch (Exception e) 구문을 타게 함으로써 커버리지를 확보합니다.
		assertThat(jwtTokenProvider.isTokenExpired("invalid-token-string")).isFalse();
	}

	@Test
	@DisplayName("getUsername/getRole - 만료된 토큰인 경우 ExpiredJwtException이 발생한다")
	void getUsername_expiredToken_throwsException() throws InterruptedException {
		String token = jwtTokenProvider.createToken("testuser", 1L);
		Thread.sleep(10);

		assertThatThrownBy(() -> jwtTokenProvider.getUsername(token))
			.isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
	}

	@Test
	@DisplayName("validateToken - 잘못된 서명(Key mismatch) 시 SignatureException이 발생한다")
	void validateToken_wrongKey_throwsException() {
		// 다른 키로 생성된 토큰
		JwtTokenProvider otherProvider = new JwtTokenProvider(
			"anotherSecretKeyForTestingAnotherSecretKeyForTesting123!", 30L, 7L
		);
		String tokenFromOther = otherProvider.createAccessToken("user", UserRole.CUSTOMER);

		assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenFromOther))
			.isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
	}

	@Test
	@DisplayName("createAccessToken - role 클레임이 없는 토큰에서 getRole 호출 시 null을 반환한다")
	void getRole_noClaim_returnsNull() {
		// createToken은 subject만 넣고 role(auth 클레임)은 넣지 않음
		String tokenNoRole = jwtTokenProvider.createToken("testuser", 60000L);

		String role = jwtTokenProvider.getRole(tokenNoRole);

		assertThat(role).isNull();
	}

	@Test
	@DisplayName("getRemainingValidityMillis - 만료된 토큰일 때 catch(ExpiredJwtException) 블록을 실행한다")
	void getRemainingValidityMillis_catchExpiredException() throws InterruptedException {
		// 1ms짜리 토큰 생성 후 확실히 만료시킴
		String token = jwtTokenProvider.createToken("testuser", 1L);
		Thread.sleep(5);

		// 실행 결과가 0이어야 하며, 내부적으로 catch 블록이 실행됨
		long remaining = jwtTokenProvider.getRemainingValidityMillis(token);

		assertThat(remaining).isEqualTo(0);
	}

	@Test
	@DisplayName("isTokenExpired - 만료된 토큰일 때 catch(ExpiredJwtException) 블록을 실행한다")
	void isTokenExpired_catchExpiredException() throws InterruptedException {
		String token = jwtTokenProvider.createToken("testuser", 1L);
		Thread.sleep(5);

		// catch(ExpiredJwtException) 로직이 실행되어 true 반환
		boolean isExpired = jwtTokenProvider.isTokenExpired(token);

		assertThat(isExpired).isTrue();
	}

	@Test
	@DisplayName("isTokenExpired - 형식이 잘못된 토큰일 때 catch(Exception) 블록을 실행한다")
	void isTokenExpired_catchGeneralException() {
		// "abc" 같이 점(.)조차 없는 문자열은 MalformedJwtException 등을 발생시킴
		// 이는 catch(Exception e) 블록으로 넘어가서 false를 반환하게 됨
		boolean result = jwtTokenProvider.isTokenExpired("completely-wrong-token-format");

		assertThat(result).isFalse();
	}
}
