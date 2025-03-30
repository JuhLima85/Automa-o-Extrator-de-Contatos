package com.juhsouza.automacao.controller;

import com.juhsouza.automacao.service.AutomacaoCorbanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://juhsouza.netlify.app"})
@RequestMapping("/api/planilha")
public class PlanilhaController {

    @Autowired
    private AutomacaoCorbanService automacaoCorbanService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPlanilha(@RequestParam("file") MultipartFile file) throws IOException {
        Path diretorioPath = Paths.get("uploads");
        Files.createDirectories(diretorioPath);
        String nomeArquivo = file.getOriginalFilename();
        Path caminhoArquivo = diretorioPath.resolve(nomeArquivo);
        Files.write(caminhoArquivo, file.getBytes());

        automacaoCorbanService.setCaminhoPlanilha(caminhoArquivo.toString());

        return ResponseEntity.ok("Arquivo salvo com sucesso!.");
    }
    @GetMapping("/download/{nomeArquivo}")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable String nomeArquivo) throws IOException {
        Path caminhoArquivo = Paths.get("uploads").resolve(nomeArquivo);
        Resource resource = new UrlResource(caminhoArquivo.toUri());

        if (resource.exists() || resource.isReadable()) {
            String contentType = Files.probeContentType(caminhoArquivo);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("Erro ao acessar o arquivo.");
        }
    }
}
