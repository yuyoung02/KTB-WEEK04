package ktb.week04.springboot.repository;

import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글만 조회..
    List<Post> findByDeletedAtIsNull();

    // 삭제된 유저의 게시글 지우기 위해
    List<Post> findByUserAndDeletedAtIsNull(User user);
}