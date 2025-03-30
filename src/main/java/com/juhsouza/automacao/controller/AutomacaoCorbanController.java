package com.juhsouza.automacao.controller;

import com.juhsouza.automacao.service.AutomacaoCorbanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://juhsouza.netlify.app"})
@RequestMapping("api/automacaoCorban")
public class AutomacaoCorbanController {

    private final AutomacaoCorbanService automacaoCorbanService;

    public AutomacaoCorbanController(AutomacaoCorbanService automacaoCorbanService) {
        this.automacaoCorbanService = automacaoCorbanService;
    }

    @PostMapping("/executar")
    public ResponseEntity<String> executarAutomacao(
            @RequestParam("caminhoPlanilha") String caminhoPlanilha,
            @RequestParam("usuario") String usuario,
            @RequestParam("senha") String senha) {

        automacaoCorbanService.setCaminhoPlanilha(caminhoPlanilha);
        automacaoCorbanService.executarAutomacao(usuario, senha);

        return ResponseEntity.ok("Automação iniciada com a planilha: " + caminhoPlanilha);
    }
}
