package ktb.week04.springboot.repository;

import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.PostLike;
import ktb.week04.springboot.entity.PostLikeId;
import ktb.week04.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    boolean existsByUserAndPost(User user, Post post);

    Long countByPost(Post post);

    void deleteByUserAndPost(User user, Post post);
}
