package ktb.week04.springboot.repository;

import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.CommentId;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, CommentId> {

    List<Comment> findByPostAndDeletedAtIsNull(Post post);

    Optional<Comment> findByPostAndCommentIdAndDeletedAtIsNull(Post post, Long commentId);

    List<Comment> findByUserAndDeletedAtIsNull(User user);

    Long countByPost(Post post);

    Long countByPostAndDeletedAtIsNull(Post post);
}
