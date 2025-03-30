package com.juhsouza.automacao.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AutomacaoLemittiService {
    private final String caminhoPlanilha = "C:\\Users\\juju_\\planilhas\\Planilha_26_03_25.xlsx";
    private int contadorConsultas = 0;
    private  String beneficio;

    public void executarAutomacao() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get("https://lemitti.com/auth/login");
            fazerLogin(driver, wait);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private void fazerLogin(WebDriver driver, WebDriverWait wait) {

        // Informaçoes sobre cookies:
        driver.findElement(By.cssSelector("div.cc-window.cc-banner a.cc-btn.cc-dismiss")).click();

        // Preenche o formulário de login
        driver.findElement(By.id("email")).sendKeys("inserir email");
        driver.findElement(By.id("password")).sendKeys("inserir senha");
        driver.findElement(By.className("btn-lemit")).click();

        // Espera que o dashboard apareça ou que o alerta seja exibido
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.id("dashboard")),
                    ExpectedConditions.alertIsPresent()
            ));

            // Tenta tratar o alerta, caso apareça
            try {
                Alert alert = driver.switchTo().alert();
                if (alert.getText().contains("Usuário já autenticado em outra estação")) {
                    // Aceita o alerta
                    alert.accept();
                    System.out.println("Alerta 'Usuário já autenticado em outra estação' foi aceito.");
                }
            } catch (NoAlertPresentException ignored) {
                // Se não houver alerta, ignora e segue com o login
                System.out.println("Nenhum alerta foi apresentado.");
            }

        } catch (TimeoutException e) {
            // System.out.println("Falha ao logar, tentando fazer login novamente...");
        }
    }
}
