package ktb.week04.springboot.dto.post;

import lombok.Getter;

@Getter
public class PostPatchDto {
    private String patchSubject;
    private String patchText;
    private String patchImage;

}
