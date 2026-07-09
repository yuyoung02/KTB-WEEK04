package ktb.week04.springboot.service;

import ktb.week04.springboot.dto.comment.CommentPatchDto;
import ktb.week04.springboot.dto.comment.CommentRequestDto;
import ktb.week04.springboot.dto.comment.CommentResponseDto;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;
import ktb.week04.springboot.entity.User;
import ktb.week04.springboot.repository.CommentRepository;
import ktb.week04.springboot.repository.PostRepository;
import ktb.week04.springboot.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    //댓글 달기
    public CommentResponseDto postComment(Long postId, CommentRequestDto commentRequest, Long currentUserId
    ) {
        Post post = findPostById(postId);

        User user = findUserById(currentUserId);

        Long commentId = commentRepository.countByPost(post) +1;

        Comment comment = new Comment(
                post,
                commentId,
                commentRequest.getCommentText(),
                user
        );

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDto(savedComment);
    }

    //댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId){
        Post post =findPostById(postId);

        return commentRepository.findByPostAndDeletedAtIsNull(post)
                .stream()
                .map(CommentResponseDto::new)
                .toList();
    }

    //댓글 수정 -> 내가 쓴것만 수정할 수있게..
    public CommentResponseDto updateComment(
            Long postId,
            Long commentId,
            Long currentUserId,
            CommentPatchDto patchRequest
    ) {

        Post post =findPostById(postId);
        Comment comment = findCommentByPostAndCommentId(post,commentId);


        if (!comment.getUser().getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only author can modify comment"
            );
        }


        if (patchRequest.getCommentText() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing changed"
            );
        }

        comment.updateCommentText(patchRequest.getCommentText());

        return new CommentResponseDto(comment);
    }

    //댓긋 삭제
    public String deleteComment(Long postId, Long commentId, Long currentUserId
    ) {

        Post post = findPostById(postId);
        Comment comment = findCommentByPostAndCommentId(post, commentId);

        if (!comment.getUser().getUserId()
                .equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only author can delete comment"
            );
        }

        comment.delete();

        return "Delete Success";
    }


    //not found 메소
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

    private Comment findCommentByPostAndCommentId(Post post, Long commentId) {
        return commentRepository.findByPostAndCommentIdAndDeletedAtIsNull(post, commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found"
                ));
    }
}
