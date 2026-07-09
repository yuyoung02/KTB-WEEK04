package ktb.week04.springboot.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String role;

    public CustomUserDetails(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    // 현재 사용자가 가진 권한을 sprng security에게 알려주기 위한 메소드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security에서 hasRole("USER")는 내부적으로 "ROLE_USER"를 찾음
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // 비밀번호 반환
    @Override
    public String getPassword() {
        return null;
    }

    // 로그인 식별자 이메일로
    @Override
    public String getUsername() {
        return email;
    }
}