package ktb.week04.springboot.security;

// jwt는 사용자 정보를 꺼낸 결과를 Claims라는 객체로 받는다.
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ktb.week04.springboot.entity.Enum.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
// JWT에 서명할 비밀키 타입
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    //비밀키, 유효시간
    private final SecretKey signingKey;
    private final long expiration;

    public JwtTokenProvider(
            // 설정 파일에서 가져오기
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        // jwt 서명에서는 그냥 String 사용 불가. byte[] 형태 필요.
        //byte[]를 jwt 서명에 사용할 수 있는 비밀키로 바꾸기 Keys.hma~
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // jwt 생성
    public String createToken(Long userId, String email, Role role) {
        // 생성 시간, 만료 시간
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiration);

        // Jwt 생성
        return Jwts.builder()
                .subject(email) // sub: subject는 보통 식별 가능한 값 넣음
                .claim("userId", userId) // userId와 권한 저장
                .claim("role", role.name())
                .issuedAt(now) // iat : 발급 시간
                .expiration(expiredAt) // exp : 만료 시간
                .signWith(signingKey) // 사인 하고
                .compact(); // 완성된 jwt를 문자열로
        // sub → Subject → 이 토큰의 주체가 누구인가
        // iat → Issued At → 언제 발급했는가
        // exp → Expiration → 언제 만료되는가
        // 위의것들은 이미 등록된 클레임들..
    }

    // 토큰 검사
    public boolean validateToken(String token) {
        try {
            // 토큰 파싱 시도
            getClaims(token);
            return true;
        } catch (Exception e) {
            // 문제시 예외 발생 (예: 서명 잘못됨,기만료, jwt 형식 이상)
            return false;
        }
    }

    //jwt에서 내용들 꺼내기
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
        // 등록되지 않은 클레임들을 이렇게 꺼내야한다..
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
        // 등록된 클레임은 이렇게..
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    // jwt 검증하고 내부 데이터 꺼내기
    private Claims getClaims(String token) {
        // jwt 분석 (생성은 .builder())
        return Jwts.parser()
                // 서명 검증할 비밀키 설정 (생성할땐 .signWith())
                .verifyWith(signingKey)
                .build() // paserer 객체 생성 완료
                .parseSignedClaims(token) // 전달 받은 jwt 문자열 분석 (전달받은 JWT 문자열을 파싱하고 서명 등을 검증해서 Claims를 읽는 메서드)
                .getPayload(); // payload 꺼내기 -> 결과타입은 클레임
    }
}