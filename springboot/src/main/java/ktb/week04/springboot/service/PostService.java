package ktb.week04.springboot.service;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.entity.PostLike;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.validation.Valid;
import org.springframework.resilience.annotation.Retryable;
import ktb.week04.springboot.dto.post.*;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.PostLikeRepository;
import ktb.week04.springboot.repository.PostRepository;
import ktb.week04.springboot.repository.CommentRepository;
import ktb.week04.springboot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       PostLikeRepository postLikeRepository,
                       CommentRepository commentRepository,
                       S3Service s3Service) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.s3Service = s3Service;
    }

    //게시글 작성
    public PostCreateResponseDto createPost(PostRequestDto postRequest, MultipartFile image, Long currentUserId){

        User user = findUserById(currentUserId);

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = s3Service.upload(image, "posts");
        }

        Post post = new Post(
                user,
                postRequest.getStadiumId(),
                postRequest.getSubject(),
                imageUrl,
                postRequest.getText()
        );

        Post savedPost = postRepository.save(post);

        return new PostCreateResponseDto(post);

    }

    //게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPost() {
        List<Post> posts =
                postRepository
                        .findByDeletedAtIsNullOrderByCreatedAtDescPostIdDesc();

        return posts.stream().map(post -> {
                    Long likeCount = postLikeRepository.countByPost(post);
                    Long commentCount = commentRepository.countByPostAndDeletedAtIsNull(post);
                    return toPostListResponse(post, likeCount, commentCount);
                })
                .toList();
    }

    //구장별 조회 메소드
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPostsByStadium(
            StadiumCode stadiumCode
    ) {
        List<Post> posts =
                postRepository
                        .findByStadiumCodeAndDeletedAtIsNullOrderByCreatedAtDescPostIdDesc(
                                stadiumCode
                        );

        return posts.stream()
                .map(post -> {
                    Long likeCount = postLikeRepository.countByPost(post);
                    Long commentCount = commentRepository.countByPostAndDeletedAtIsNull(post);
                    return toPostListResponse(post, likeCount, commentCount);
                })
                .toList();
    }

    // 구장과 검색어 조건으로 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostListResponseDto> searchPosts(
            StadiumCode stadiumCode,
            String keyword
    ) {
        String normalizedKeyword = keyword == null || keyword.isBlank()
                ? null
                : keyword.trim();

        return postRepository.searchPosts(stadiumCode, normalizedKeyword)
                .stream()
                .map(post -> {
                    Long likeCount = postLikeRepository.countByPost(post);
                    Long commentCount = commentRepository.countByPostAndDeletedAtIsNull(post);
                    return toPostListResponse(post, likeCount, commentCount);
                })
                .toList();
    }

    //게시글 상세 조회
    public PostResponseDto getPost(Long postId){

        //원자적 update -> 조회수 +1
        int updatedCount = postRepository.increaseViewCount(postId);

        if (updatedCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        Post post = findPostById(postId);

        // 상세 조회하면 -> 조회수 ++
        //post.increaseView();

        //좋아요수 따로
        Long likeCnt = postLikeRepository.countByPost(post);

        return toPostResponse(post, likeCnt);
    }

    //게시글 수정
    public PostResponseDto patchPost(Long postId, PostPatchDto patchRequest, Long currentUserId, MultipartFile image){

        Post post = findPostById(postId);

        //내가 쓴것만 수정 가능
        if (!post.getUser().getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only author can modify post"
            );
        }

        if (patchRequest.getPatchSubject() != null) {
            post.changeSubject(patchRequest.getPatchSubject());
        }

        boolean hasImage = image != null && !image.isEmpty();
        boolean removeImage = Boolean.TRUE.equals(patchRequest.getRemoveImage());

        if (hasImage) {
            String previousImageUrl = post.getImage();
            String imageUrl = s3Service.upload(image, "posts");
            post.changeImage(imageUrl);
            deleteImageAfterCommit(previousImageUrl);
        } else if (removeImage && post.getImage() != null) {
            String previousImageUrl = post.getImage();
            post.changeImage(null);
            deleteImageAfterCommit(previousImageUrl);
        }

        if(patchRequest.getPatchText() != null){
            post.changeText(patchRequest.getPatchText());
        }

        if (patchRequest.getPatchStadiumId() != null) {
            post.changeStadiumCode(
                    patchRequest.getPatchStadiumId()
            );
        }

        if (patchRequest.getPatchSubject() == null
                && patchRequest.getPatchText() == null
                && patchRequest.getPatchStadiumId() == null
                && !removeImage
                && !hasImage) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing changed"
            );
        }

        Long likeCnt = postLikeRepository.countByPost(post);

        return toPostResponse(post, likeCnt);

    }
    //게시글 삭제
    public String deletePost(Long postId, Long userId) {
        Post post = findPostById(postId);

        //내가 쓴것만 삭제 가눙
        if (!post.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only author can delete post"
            );
        }

        String imageUrl = post.getImage();
        post.delete();
        deleteImageAfterCommit(imageUrl);

        return "Delete Success";
    }


    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, Long userId) {
        Post post = findPostById(postId);
        User user = findUserById(userId);

        return postLikeRepository.existsByUserAndPost(user, post);
    }

    // 좋아요 +1 -> 디비 구성 후 한 사람이 하나 누르게 수정
    public void increaseLike(Long postId, Long userId){
        Post post = findPostById(postId);

        User user =findUserById(userId);

        if (postLikeRepository.existsByUserAndPost(user, post)){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already liked"
            );
        }

        // 종ㅎ아요 저장
        PostLike postLike = new PostLike(user, post);
        postLikeRepository.save(postLike);
    }

    //좋아요 취소
    public void decreaseLike(Long postId, Long userId){
        Post post = findPostById(postId);

        User user = findUserById(userId);

        if (!postLikeRepository.existsByUserAndPost(user, post)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Like not found"
            );
        }

        postLikeRepository.deleteByUserAndPost(user, post);

    }



    //Not found 메소드
    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .filter(post -> post.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // DB 변경이 확정된 뒤 기존 S3 이미지를 삭제한다.
    private void deleteImageAfterCommit(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        s3Service.delete(imageUrl);
                    }
                }
        );
    }

    private PostListResponseDto toPostListResponse(
            Post post,
            Long likeCount,
            Long commentCount
    ) {
        return new PostListResponseDto(
                post,
                likeCount,
                commentCount,
                s3Service.createPresignedGetUrl(post.getUser().getImage())
        );
    }

    private PostResponseDto toPostResponse(Post post, Long likeCount) {
        return new PostResponseDto(
                post,
                likeCount,
                s3Service.createPresignedGetUrl(post.getImage()),
                s3Service.createPresignedGetUrl(post.getUser().getImage())
        );
    }
}
