package ktb.week04.springboot.security;

import jakarta.servlet.FilterChain; //다음 필터로 넘기는 통로
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest; //들어온 요청
import jakarta.servlet.http.HttpServletResponse; //나갈 응답

import lombok.RequiredArgsConstructor;
//Spring Security의 Authentication 객체를 만들 때 쓰는 클래스
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// 현재 요청에서 인증 정보 저장 -> contextHolder
import org.springframework.security.core.context.SecurityContextHolder;
// 인증 객체에 요청 관련 부가정보 넣을때 사용
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
// 요청당 필터 1번만 실행되게
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 요청이 들어오면 실행되는 메소드
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        // 2. 헤더에 토큰이 없거나, bearer이 아니면 탈락
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Bearer < 공백까지 7글자 잘라서 토큰만 빼내기
        String token = authorizationHeader.substring(7);

        // 4. 토큰 검증 로직
        if (jwtTokenProvider.validateToken(token)) {
            //jwt 토큰에서 사용자 정보 꺼내기
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // 5. userDetails 객체 만들기
            CustomUserDetails userDetails = new CustomUserDetails(userId, email, role);

            //6. 인증 완료 상태를 표현하는 Authentication 객체 만들기
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, // 현재 로그인한 사용자 정보
                            null, // 비밀번호 자리 -> 토큰 검증 끝나서 null
                            userDetails.getAuthorities() // 인가 검사
                    );

            // 인증 객체에 요청 관련 부가 정보 넣기 (핵심 인증엔 없어도 됨. 관례같은거) -> 예시: 접속 ip, 세션 id 등
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 만들어진 인증 객체를 securityContextHolder에 넣기 -> 이후 get으로 꺼낼 수 ㅇ
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터 실행~
        filterChain.doFilter(request, response);
    }
}