package edu.handong.web11.controller;

import edu.handong.web11.domain.Attachment;
import edu.handong.web11.repository.AttachmentRepository;
import edu.handong.web11.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentRepository attachments;
    private final FileStorageService storage;
    public AttachmentController(AttachmentRepository attachments, FileStorageService storage) {
        this.attachments = attachments; this.storage = storage;
    }
    // 파일 조회 및 HTTP 다운로드 응답 처리
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment file = attachments.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));
        // 리소스 = 서버 시스템에 저장된 실제 파일을 읽기 위한 객체
        Resource resource = storage.load(file);
        String disposition = ContentDisposition.attachment() //브라우저가 파일 다운로드가 동작하도록 설정 및 파일명 지정
            .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
            .build().toString();
        return ResponseEntity.ok() // http 상태코드 구성,헤더, 바디 구성
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
            .body(resource);
    }

    // 이미지 미리보기를 위한 GET 요청 처리
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id) {
        // ID를 이용해 DB에서 첨부파일 정보를 조회
        // 존재하지 않는다면 오류를 반환
        Attachment file = attachments.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "첨부파일이 없습니다."));

        // 이미지 파일인지 MIME 타입을 검사
        String contentType = file.getContentType();

        if (!"image/jpeg".equals(contentType) &&
                !"image/png".equals(contentType) &&
                !"image/gif".equals(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "이미지 파일만 미리보기가 가능합니다.");
        }
        // 파일이 존재하지 않는 경우 storage.load()에서 404 오류를 처리.
        Resource resource = storage.load(file);

        // 브라우저에서 다운로드 대신 이미지를 표시하도록 설정.
        String disposition = ContentDisposition.inline()
                .filename(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .build().toString();

        // 이미지 타입과 파일 데이터를 HTTP 응답으로 전달.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
