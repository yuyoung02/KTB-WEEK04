package ktb.week04.springboot.repository;

import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.CommentId;
import ktb.week04.springboot.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, CommentId> {

    List<Comment> findByPostAndDeletedAtIsNull(Post post);

    Optional<Comment> findByPostAndCommentIdAndDeletedAtIsNull(Post post, Long commentId);

    Long countByPost(Post post);
}