package com.juhsouza.automacao.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@Service
public class AutomacaoCorbanService {

    private String caminhoPlanilha;
    private int contadorConsultas = 0;
    private  String beneficio;

    public void setCaminhoPlanilha(String caminhoPlanilha) {
        String diretorioRaiz = System.getProperty("user.dir");
        String caminhoCompleto = Paths.get(diretorioRaiz, caminhoPlanilha).toString();

        System.out.println("Caminho completo da planilha: " + caminhoCompleto);

        this.caminhoPlanilha = caminhoCompleto;
    }

    public String executarAutomacao(String usuario, String senha) {
        String driverPath = System.getProperty("user.dir") + "/resources/drivers/chromedriver";
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get("https://gestao.sistemacorban.com.br/index.php");
            fazerLogin(driver, wait, usuario, senha);

            List<String> beneficios = lerBeneficiosDoExcel(caminhoPlanilha);

            for (int i = 0; i < beneficios.size(); i++) {
                beneficio = beneficios.get(i);
                selecionarBeneficio(driver, beneficio);

                List<String> telefones1 = capturarTelefones(driver, wait, "btnHideOrShowTelefone", "//div[@class='text-secundary__system']");
                salvarTelefonesNaPlanilha(telefones1, i + 1);

                List<String> telefones2 = capturarTelefones(driver, wait, "repCpf2", "//div[@id='content-botoes']//table[2]//tr[position()>1]/td[1]");
                salvarTelefonesNaPlanilha(telefones2, i + 1);

                if(contadorConsultas != 1){
                    fecharJanela(driver);
                }

                if (i == beneficios.size() - 1) {
                    System.out.println("Finalizado o salvamento dos telefones do CORBAM. Último telefones salvo do Benefício: " + beneficio);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return "Telefones capturados e salvos com sucesso! A planilha está pronta para ser baixada.";
    }

    private void fazerLogin(WebDriver driver, WebDriverWait wait, String usuario, String senha) {
        driver.findElement(By.id("exten")).sendKeys(usuario);
        driver.findElement(By.id("password")).sendKeys(senha);
        driver.findElement(By.id("button-sigin")).click();

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.id("dashboard")),
                    ExpectedConditions.alertIsPresent()
            ));

            try {
                Alert alert = driver.switchTo().alert();
                if (alert.getText().contains("Usuário já autenticado em outra estação")) {
                    alert.accept();
                    System.out.println("Alerta 'Usuário já autenticado em outra estação' foi aceito.");
                }
            } catch (NoAlertPresentException ignored) {
                System.out.println("Nenhum alerta foi apresentado.");
            }

            Set<org.openqa.selenium.Cookie> cookies = driver.manage().getCookies();
            System.out.println("Cookies salvos após login.");

            for (org.openqa.selenium.Cookie cookie : cookies) {
                driver.manage().addCookie(cookie);
            }

            driver.navigate().refresh();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dashboard")));
            System.out.println("Login realizado com sucesso!");

        } catch (TimeoutException e) {
            System.out.println("Falha ao logar, tentando fazer login novamente...");
        }
    }

    private void selecionarBeneficio(WebDriver driver, String beneficio) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement seletorBeneficio = wait.until(ExpectedConditions.elementToBeClickable(By.id("typeOfDataSent")));
        new Select(seletorBeneficio).selectByValue("beneficio");

        Wait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(15))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

        WebElement campoBeneficio = fluentWait.until(ExpectedConditions.elementToBeClickable(By.id("dataToReceptivo")));
        campoBeneficio.sendKeys(beneficio, Keys.ENTER);
    }


    private void fecharJanela(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#modal-botoes")
            ));

            if (modal.isDisplayed()) {
                WebElement botaoFechar = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("#modal-botoes > div > div > div.modal-footer > div > button:nth-child(2)")
                ));
                botaoFechar.click();
            }
        } catch (TimeoutException | NoSuchElementException ignored) {
            System.out.println("Modal não encontrado, seguindo adiante.");
        }
    }

    private List<String> capturarTelefones(WebDriver driver, WebDriverWait wait, String botaoId, String xpath) {
        try {
            WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(By.id(botaoId)));
            botao.click();

            FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(7))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

            List<WebElement> elementos = fluentWait.until(new Function<WebDriver, List<WebElement>>() {
                public List<WebElement> apply(WebDriver driver) {
                    List<WebElement> encontrados = driver.findElements(By.xpath(xpath));
                    return encontrados.isEmpty() ? null : encontrados;
                }
            });

            List<String> telefones = new ArrayList<>();
            for (WebElement elemento : elementos) {
                String telefone = elemento.getText().trim().replaceAll("[^0-9]", "");
                if (!telefone.isEmpty()) {
                    telefones.add(telefone);
                }  else if ("repCpf2".equals(botaoId)) {
                    try {
                        System.out.println("Nenhum telefone encontrado para o botão: " + botaoId);
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            System.out.println("Benefício: " + beneficio);
            System.out.println("Consulta " + contadorConsultas + ": " + String.join(", ", telefones));
            contadorConsultas++;

            return telefones;
        } catch (TimeoutException e) {
            System.out.println("Nenhum telefone encontrado para o botão: " + botaoId);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao capturar telefones para o botão: " + botaoId, e);
        }
    }

    /*
    * SALVA DINAMICAMENTE - MAS PULA OS CAMPOS MESMO QUANDO APAGO AS INFORMAÇOES, NESSE CASO TENHO QUE APAGAR AS COLUNAS NÃO SOMENTE OS CAMPOS
    * */
    private void salvarTelefonesNaPlanilha(List<String> telefones, int linhaIndex) {
        if (telefones.isEmpty()) return;

        // Remove duplicatas da lista capturada
        Set<String> telefonesUnicos = new LinkedHashSet<>(telefones);

        List<String> colunasTelefone = List.of(
                "TELEFONES CORBAN 1", "TELEFONES CORBAN 2", "TELEFONES CORBAN 3",
                "TELEFONES CORBAN 4", "TELEFONES CORBAN 5", "TELEFONES CORBAN 6",
                "TELEFONES CORBAN 7", "TELEFONES CORBAN 8", "TELEFONES CORBAN 9",
                "TELEFONES CORBAN 10", "TELEFONES CORBAN 11", "TELEFONES CORBAN 12",
                "TELEFONES CORBAN 13", "TELEFONES CORBAN 14", "TELEFONES CORBAN 15",
                "TELEFONES CORBAN 16", "TELEFONES CORBAN 17", "TELEFONES CORBAN 18",
                "TELEFONES CORBAN 19", "TELEFONES CORBAN 20"
        );

        try (FileInputStream file = new FileInputStream(caminhoPlanilha);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) headerRow = sheet.createRow(0);

            Row dataRow = sheet.getRow(linhaIndex);
            if (dataRow == null) dataRow = sheet.createRow(linhaIndex);

            // Mapear os índices das colunas na ordem correta
            Map<String, Integer> colunasIndices = new LinkedHashMap<>();
            for (int i = 0; i < colunasTelefone.size(); i++) {
                String nomeColuna = colunasTelefone.get(i);
                Integer colIndex = null;

                for (Cell cell : headerRow) {
                    if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().equals(nomeColuna)) {
                        colIndex = cell.getColumnIndex();
                        break;
                    }
                }

                if (colIndex == null) {
                    colIndex = headerRow.getLastCellNum() == -1 ? 0 : (int) headerRow.getLastCellNum();
                    Cell headerCell = headerRow.createCell(colIndex, CellType.STRING);
                    headerCell.setCellValue(nomeColuna);
                }
                colunasIndices.put(nomeColuna, colIndex);
            }

            // Coletar telefones já preenchidos na planilha para evitar duplicação
            Set<String> telefonesExistentes = new HashSet<>();
            for (Integer colIndex : colunasIndices.values()) {
                Cell cell = dataRow.getCell(colIndex);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    telefonesExistentes.add(cell.getStringCellValue().trim());
                }
            }

            // Filtra apenas os telefones que ainda não estão na planilha
            telefonesUnicos.removeAll(telefonesExistentes);

            // Preencher as colunas disponíveis
            int telefoneIndex = 0;
            for (String nomeColuna : colunasTelefone) {
                if (telefoneIndex >= telefonesUnicos.size()) break;

                Integer colIndex = colunasIndices.get(nomeColuna);
                Cell cell = dataRow.getCell(colIndex);

                // Se a célula está vazia, preenche com um telefone único que ainda não está na planilha
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    dataRow.createCell(colIndex, CellType.STRING)
                            .setCellValue((String) telefonesUnicos.toArray()[telefoneIndex]);
                    telefoneIndex++;
                }
            }

            // Salvar a planilha
            try (FileOutputStream outputStream = new FileOutputStream(caminhoPlanilha)) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> lerBeneficiosDoExcel(String resourcePath) throws IOException {
        List<String> beneficios = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(resourcePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            int beneficioColumnIndex = -1;
            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING && "BENEFICIO".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                    beneficioColumnIndex = cell.getColumnIndex();
                    break;
                }
            }
            if (beneficioColumnIndex == -1) throw new IOException("Coluna 'BENEFICIO' não encontrada.");

            // Percorre todas as linhas a partir da segunda (índice 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null) continue;

                Cell cell = dataRow.getCell(beneficioColumnIndex);
                if (cell == null || cell.getCellType() == CellType.BLANK) continue;

                String beneficio;
                switch (cell.getCellType()) {
                    case STRING -> beneficio = cell.getStringCellValue().trim();
                    case NUMERIC -> beneficio = String.valueOf((long) cell.getNumericCellValue()); // Converte número para string
                    default -> throw new IOException("Tipo de dado inesperado na célula da coluna 'BENEFICIO'.");
                }

                beneficios.add(beneficio);
            }
        }
        return beneficios;
    }
}