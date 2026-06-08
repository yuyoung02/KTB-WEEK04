package ktb.week04.springboot.service;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.comment.CommentRequestDto;
import ktb.week04.springboot.dto.comment.CommentResponseDto;
import ktb.week04.springboot.entity.Comment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    //댓글 데이터 담을곳..
    private Long commentId = 1L;
    private final Map<Long, List<Comment>> comments = new HashMap<>();


    //댓글 달기
    @PostMapping("/{postId}/comments")
    public CommentResponseDto postComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequest
    ) {
        Comment comment = new Comment(
                commentId++,
                commentRequest.getCommentText(),
                commentRequest.getNickname()
        );

        List<Comment> commentList =
                comments.getOrDefault(postId, new ArrayList<>());

        commentList.add(comment);

        comments.put(postId, commentList);

        return new CommentResponseDto(comment);
    }

    //댓글 조회
    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId){
        if(comments.get(postId) == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        return comments.get(postId);
    }

    //댓글 수정 -> 나중에 내가 쓴것만 수정할 수있게..
    @PatchMapping("/{postId}/comments/{commentId}")
    public CommentResponseDto updateComment(@PathVariable Long postId, @PathVariable Long commentId, @Valid @RequestBody CommentRequestDto commentRequest
    ) {
        List<Comment> commentList = comments.get(postId);

        if (commentList == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found"
            );
        }

        Comment targetComment = null;

        for (Comment comment : commentList) {
            if (comment.getCommentId().equals(commentId)) {
                targetComment = comment;
                break;
            }
        }

        if (targetComment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found"
            );
        }

        targetComment.updateCommentText(commentRequest.getCommentText());

        return new CommentResponseDto(targetComment);
    }

    //댓긋 삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    public String deleteComment(@PathVariable Long postId, @PathVariable Long commentId
    ) {

        List<Comment> commentList = comments.get(postId);

        if (commentList == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found"
            );
        }

        Comment targetComment = null;

        for (Comment comment : commentList) {
            if (comment.getCommentId().equals(commentId)) {
                targetComment = comment;
                break;
            }
        }

        if (targetComment == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comment not found"
            );
        }

        commentList.remove(targetComment);

        return "Delete Success";
    }
}
