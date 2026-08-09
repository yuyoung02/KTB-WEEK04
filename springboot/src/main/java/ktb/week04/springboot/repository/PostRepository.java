package ktb.week04.springboot.repository;

import jakarta.persistence.LockModeType;
import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글만 조회..
    List<Post> findByDeletedAtIsNullOrderByCreatedAtDescPostIdDesc();

    // 삭제된 유저의 게시글 지우기 위해
    List<Post> findByUserAndDeletedAtIsNull(User user);

    List<Post> findByStadiumCodeAndDeletedAtIsNullOrderByCreatedAtDescPostIdDesc(
            StadiumCode stadiumCode
    );

    // 구장 필터와 제목·본문 검색을 함께 처리
    @Query("""
            SELECT p
            FROM Post p
            WHERE p.deletedAt IS NULL
              AND (:stadiumId IS NULL OR p.stadiumCode = :stadiumId)
              AND (
                  :keyword IS NULL
                  OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(p.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY p.createdAt DESC, p.postId DESC
            """)
    List<Post> searchPosts(
            @Param("stadiumId") StadiumCode stadiumId,
            @Param("keyword") String keyword
    );

    // 원자적 업데이트
    @Modifying
    @Query("""
    UPDATE Post p
    SET p.viewNum = p.viewNum + 1
    WHERE p.postId = :postId
      AND p.deletedAt IS NULL
    """)
    int increaseViewCount(@Param("postId") Long postId);



}
