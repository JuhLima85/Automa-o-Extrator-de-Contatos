package com.juhsouza.automacao.controller;

import com.juhsouza.automacao.service.AutomacaoLemittiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/automacaoLemitti")
public class AutomacaoLemittiController {
    private final AutomacaoLemittiService automacaoLemittiService;

    public AutomacaoLemittiController(AutomacaoLemittiService automacaoLemittiService) {
        this.automacaoLemittiService = automacaoLemittiService;
    }

    @GetMapping("/executar")
    public void executar() {
        automacaoLemittiService.executarAutomacao();
    }
}