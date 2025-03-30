package com.juhsouza.automacao.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AutomacaoCorbanServiceTest {

    @Mock
    private AutomacaoCorbanService automacaoCorbanService;

    @Value("${app.execution.mode}")
    private String executionMode;

    @Test
    public void testExecutarAutomacao() {
        // Simula a execução apenas no modo "test"
        if ("test".equals(executionMode)) {
            // Executa o método
            automacaoCorbanService.executarAutomacao("Rayane@25197", "102030");

            // Verifica se o método foi chamado exatamente uma vez
            verify(automacaoCorbanService, times(1)).executarAutomacao("Rayane@25197", "102030");
        }
    }
}
