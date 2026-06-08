package ktb.week04.springboot.controller;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.comment.CommentRequestDto;
import ktb.week04.springboot.dto.comment.CommentResponseDto;
import ktb.week04.springboot.dto.post.*;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;

import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.service.CommentService;
import ktb.week04.springboot.service.PostService;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public PostCreateResponseDto createPost(@Valid @RequestBody PostRequestDto postRequest){
        return postService.createPost(postRequest);
    }

    //게시글 목록 조회
    @GetMapping()
    public List<PostListResponseDto> getPost(){
        return postService.getPost();
    }

    //게시글 상세 조회
    @GetMapping("/{postId}")
    public PostResponseDto getPost(@PathVariable Long postId){
        return postService.getPost(postId);
    }

    //게시글 수정
    @PatchMapping("{postId}")
    public PostResponseDto patchPost(@PathVariable Long postId, @RequestBody PostPatchDto patchRequest){
        return postService.patchPost(postId, patchRequest);
    }
    //게시글 삭제
    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Long postId) {
        return postService.deletePost(postId);
    }

    // 좋아요 +1 -> 디비 구성 후 한 사람이 하나 누르게 수정
    @PostMapping("/{postId}/likes")
    public Long increaseLike(@PathVariable Long postId){
        return postService.increaseLike(postId);
    }

    //좋아요 취소
    @DeleteMapping("/{postId}/likes")
    public Long decreaseLike(@PathVariable Long postId){
        return postService.decreaseLike(postId);
    }

    //댓글 달기
    @PostMapping("/{postId}/comments")
    public CommentResponseDto postComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequest
    ) {
        return commentService.postComment(postId,commentRequest);
    }

    //댓글 조회
    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId){
        return commentService.getComments(postId);
    }

    //댓글 수정 -> 내가 단 것만 할 수 잇게
    @PatchMapping("/{postId}/comments/{commentId}")
    public CommentResponseDto updateComment(@PathVariable Long postId, @PathVariable Long commentId, @Valid @RequestBody CommentRequestDto commentRequest
    ) {
        return commentService.updateComment(postId, commentId, commentRequest);
    }

    //댓긋 삭제 -> 내가 단 것만 할 수 있게..
    @DeleteMapping("/{postId}/comments/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        return commentService.deleteComment(postId, commentId);
    }

}


