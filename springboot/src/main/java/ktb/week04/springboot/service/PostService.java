package ktb.week04.springboot.service;

import jakarta.validation.Valid;
import ktb.week04.springboot.dto.post.*;
import ktb.week04.springboot.entity.Comment;
import ktb.week04.springboot.entity.Post;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    // 게시글 데이터 담을곳.. (디비 나오면 다 없애야하는 인스턴스 변수들)
    private final Map<Long, Post> posts = new HashMap<>();
    private Long postId = 1L;

    //게시글 작성
    public PostCreateResponseDto createPost(@Valid @RequestBody PostRequestDto postRequest){

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초"));

        Post post = new Post(
                postRequest.getUserId(),
                postId,
                postRequest.getSubject(),
                postRequest.getImage(),
                postRequest.getText(),
                0L,
                0L,
                dateTime
        );

        posts.put(postId,post);

        postId ++;

        return new PostCreateResponseDto(post);

    }

    //게시글 목록 조회
    public List<PostListResponseDto> getPost(){
        List<PostListResponseDto> postList = new ArrayList<>();

        if(posts.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        for (Post post : posts.values()) {
            postList.add(new PostListResponseDto(post));
        }

        return postList;
    }

    //게시글 상세 조회
    public PostResponseDto getPost(@PathVariable Long postId){
        Post post = posts.get(postId);


        if (post == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        //조회수 ++
        post.increaseView();

        return new PostResponseDto(post);
    }

    //게시글 수정
    public PostResponseDto patchPost(@PathVariable Long postId, @RequestBody PostPatchDto patchRequest){

        Post post = posts.get(postId);
        if(patchRequest.getPatchImage()!=null){
            post.changeImage(patchRequest.getPatchImage());
        }

        if(patchRequest.getPatchText() != null){
            post.changeText(patchRequest.getPatchText());
        }

        if(patchRequest.getPatchText() == null && patchRequest.getPatchImage() == null){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing changed"
            );
        }

        return new PostResponseDto(post);

    }
    //게시글 삭제
    public String deletePost(@PathVariable Long postId) {
        Post post = posts.get(postId);

        if (post == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        posts.remove(postId);

        return "Delete Success";
    }

    // 좋아요 +1 -> 디비 구성 후 한 사람이 하나 누르게 수정
    public Long increaseLike(@PathVariable Long postId){
        Post post = posts.get(postId);

        if (post == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        return post.increaseLike();
    }

    //좋아요 취소
    public Long decreaseLike(@PathVariable Long postId){
        Post post = posts.get(postId);

        if (post == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post not found"
            );
        }

        return post.decreaseLike();

    }
}
