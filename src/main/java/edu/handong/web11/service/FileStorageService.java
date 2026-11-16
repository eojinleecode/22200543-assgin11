package edu.handong.web11.service;

import edu.handong.web11.domain.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "pdf", "txt");
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir}") String path) throws IOException {
        uploadDir = Path.of(path).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    public Attachment store(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank() || original.contains("/") || original.contains("\\") || original.contains(".."))
            throw new IllegalArgumentException("잘못된 파일 이름입니다.");
        if (original.length() > 240) throw new IllegalArgumentException("파일 이름이 너무 깁니다.");
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED.contains(ext)) throw new IllegalArgumentException("허용 형식: jpg, jpeg, png, gif, pdf, txt");
        if (file.getSize() > 10L * 1024 * 1024) throw new IllegalArgumentException("파일은 10MB 이하만 가능합니다.");
        // 중복 이름의 파일이 업로드될 때 덮어쓰이는 것을 방지하기 위함.
        // UUID를 활용하여 서버에 저장될 고유한 파일명을 생성
        String stored = UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(stored).normalize();
        // normalize()로 경로 경로를 정규화시킴.
        // startsWith()를 통해  디렉토리 내부에 있는지 확인하여 Path Traversal 예방.
        if (!target.startsWith(uploadDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");

        //클라이언트에서 전달받은 실제 파일 데이터를 서버의 지정된 폴더에 저장
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            // Teaching sample: MIME type is inferred by extension, not trusted from request headers.
            String contentType = switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "pdf" -> "application/pdf";
                default -> "text/plain";
            };
            return new Attachment(original, stored, contentType, file.getSize());
        } catch (IOException e) {
            try { Files.deleteIfExists(target); } catch (IOException ignored) {}
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
        }
    }

    public Resource load(Attachment attachment) {
        Path target = uploadDir.resolve(attachment.getStoredFilename()).normalize();
        if (!target.startsWith(uploadDir) || !Files.isRegularFile(target))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 없습니다.");
        return new PathResource(target);
    }

    public void deleteIfExists(Attachment attachment) {
        try {
            Files.deleteIfExists(uploadDir.resolve(attachment.getStoredFilename()).normalize());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패", e);
        }
    }
}
