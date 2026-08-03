package ktb.week04.springboot.dto.post;

import ktb.week04.springboot.entity.Enum.StadiumCode;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class PostPatchDto {
    private StadiumCode patchStadiumId;
    private String patchSubject;
    private String patchText;
    private Boolean removeImage;

}
