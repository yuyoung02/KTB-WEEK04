package ktb.week04.springboot.controller;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.comment.CommentPatchDto;
import ktb.week04.springboot.dto.comment.CommentRequestDto;
import ktb.week04.springboot.dto.comment.CommentResponseDto;
import ktb.week04.springboot.dto.post.*;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import ktb.week04.springboot.security.CustomUserDetails;
import ktb.week04.springboot.service.CommentService;
import ktb.week04.springboot.service.PostService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService){
        this.postService = postService;
        this.commentService = commentService;
    }

    //게시글 작성
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public PostCreateResponseDto createPost(@Valid @RequestBody PostRequestDto postRequest, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return postService.createPost(postRequest, userDetails.getUserId());
    }

    //게시글 목록 조회 -> 구단 필터링 추가
    @GetMapping()
    public List<PostListResponseDto> getPost(@RequestParam(required = false) StadiumCode stadiumId){
        if (stadiumId == null) {
            return postService.getPost();
        }

        return postService.getPostsByStadium(stadiumId);
    }

    //게시글 상세 조회
    @GetMapping("/{postId}")
    public PostResponseDto getPost(@PathVariable Long postId){
        return postService.getPost(postId);
    }

    //게시글 수정
    @PatchMapping("/{postId}")
    public PostResponseDto patchPost(@PathVariable Long postId, @Valid @RequestBody PostPatchDto patchRequest, Authentication authentication){

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return postService.patchPost(postId, patchRequest, userDetails.getUserId());
    }
    //게시글 삭제
    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Long postId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return postService.deletePost(postId, userDetails.getUserId());
    }

    @GetMapping("/{postId}/likes")
    public boolean checkLike(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return postService.isLiked(postId, userDetails.getUserId());
    }

    // 좋아요 +1 -> 디비 구성 후 한 사람이 하나 누르게 수정
    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void increaseLike(@PathVariable Long postId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        postService.increaseLike(postId, userDetails.getUserId());
    }

    //좋아요 취소
    @DeleteMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseLike(@PathVariable Long postId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        postService.decreaseLike(postId, userDetails.getUserId());
    }

    //댓글 달기
    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto postComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequest, Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return commentService.postComment(postId,commentRequest, userDetails.getUserId());
    }

    //댓글 조회
    @GetMapping("/{postId}/comments")
    public List<CommentResponseDto> getComments(@PathVariable Long postId){
        return commentService.getComments(postId);
    }

    //댓글 수정 -> 내가 단 것만 할 수 잇게
    @PatchMapping("/{postId}/comments/{commentId}")
    public CommentResponseDto updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentPatchDto patchRequest,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return commentService.updateComment(postId, commentId, userDetails.getUserId(), patchRequest);
    }

    //댓긋 삭제 -> 내가 단 것만 할 수 있게..
    @DeleteMapping("/{postId}/comments/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return commentService.deleteComment(postId, commentId, userDetails.getUserId());
    }
}


