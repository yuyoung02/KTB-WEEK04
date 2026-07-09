package ktb.week04.springboot.service;

import ktb.week04.springboot.entity.PostLike;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import ktb.week04.springboot.dto.post.*;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.PostLikeRepository;
import ktb.week04.springboot.repository.PostRepository;
import ktb.week04.springboot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    //게시글 작성
    public PostCreateResponseDto createPost(PostRequestDto postRequest, Long currentUserId){

        User user = findUserById(currentUserId);

        Post post = new Post(
                user,
                postRequest.getSubject(),
                postRequest.getImage(),
                postRequest.getText()
        );

        Post savedPost = postRepository.save(post);

        return new PostCreateResponseDto(post);

    }

    //게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPost() {
        List<Post> posts = postRepository.findByDeletedAtIsNull();

        return posts.stream().map(post -> {
                    Long likeCount = postLikeRepository.countByPost(post);
                    return new PostListResponseDto(post, likeCount);
                })
                .toList();
    }

    //게시글 상세 조회
    public PostResponseDto getPost(Long postId){
        Post post = findPostById(postId);

        // 상세 조회하면 -> 조회수 ++
        post.increaseView();

        //좋아요수 따로
        Long likeCnt = postLikeRepository.countByPost(post);

        return new PostResponseDto(post, likeCnt);
    }

    //게시글 수정
    public PostResponseDto patchPost(Long postId, PostPatchDto patchRequest, Long currentUserId){

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

        if(patchRequest.getPatchImage()!=null){
            post.changeImage(patchRequest.getPatchImage());
        }

        if(patchRequest.getPatchText() != null){
            post.changeText(patchRequest.getPatchText());
        }

        if(patchRequest.getPatchSubject() == null && patchRequest.getPatchText() == null && patchRequest.getPatchImage() == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing changed"
            );
        }

        Long likeCnt = postLikeRepository.countByPost(post);

        return new PostResponseDto(post, likeCnt);

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

        post.delete();

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
}
