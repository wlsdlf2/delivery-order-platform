package com.sparta.deliveryorderplatform.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sparta.deliveryorderplatform.user.entity.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
