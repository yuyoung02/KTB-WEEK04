package ktb.week04.springboot.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    // S3Config에서 Bean으로 등록한 S3Client를 주입받는다.
    private final S3Client s3Client;

    // application.yml에 작성한 버킷 이름
    @Value("${aws.s3.bucket}")
    private String bucket;

    // application.yml에 작성한 리전
    @Value("${aws.s3.region}")
    private String region;


    // S3에 이미지를 업로드하고 저장된 이미지 URL을 반환
    // @param file 업로드할 이미지
    // @param directory S3 내부 폴더명 (posts, profiles ...)

    public String upload(MultipartFile file, String directory) {

        // 빈 파일인지, 이미지인지 검사
        validateFile(file);

        // 원본 파일명 (cat.png)
        String originalFilename = file.getOriginalFilename();

        // 확장자 추출 (.png)
        String extension = extractExtension(originalFilename);

        //  UUID를 사용하는 이유 -> 같은 이름의 파일(cat.png)이 여러 번 업로드되어도 서로 덮어쓰지 않도록 하기 위해
        //  결과 예시 -> posts/550e8400-e29b-41d4-a716-446655440000.png
        String key = directory + "/" + UUID.randomUUID() + extension;


        //  S3에 저장할 객체 정보를 생성
        //  bucket -> 어느 버킷에 저장할지
        //  key-> 저장될 경로 + 파일명
        //  contentType -> 이미지 타입(image/png 등)

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {

            // 실제 S3 업로드 수행
            // RequestBody.fromInputStream() -> MultipartFile의 InputStream을 AWS SDK가 읽어서 S3로 전송
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

        } catch (IOException e) {
            throw new IllegalStateException("S3 파일 업로드에 실패했습니다.", e);
        }

         //업로드가 끝나면 이미지 URL 반환
         // 예) https://버킷명.s3.ap-northeast-2.amazonaws.com/posts/uuid.png
        return "https://" + bucket
                + ".s3."
                + region
                + ".amazonaws.com/"
                + key;
    }

    // 저장된 S3 URL에서 객체 key를 추출해 파일을 삭제한다.
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String path = URI.create(fileUrl).getPath();
            String key = path.startsWith("/") ? path.substring(1) : path;

            if (key.isBlank()) {
                return;
            }

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (RuntimeException exception) {
            // S3 삭제 실패로 게시글 수정·삭제 자체가 실패하지 않게 한다.
            log.warn("S3 이미지 삭제에 실패했습니다. url={}", fileUrl, exception);
        }
    }

     //업로드 가능한 파일인지 검사
    private void validateFile(MultipartFile file) {

        // 파일이 없는 경우
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String contentType = file.getContentType();

        // image/png, image/jpeg 등만 허용
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }


     // 파일 확장자를 추출
     // cat.png -> .png
     // dog.jpg -> .jpg
    private String extractExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}
