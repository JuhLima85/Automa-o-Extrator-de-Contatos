# Automação Extrator de Contatos

## Descrição

Este projeto automatiza a extração de números de telefone de um sistema com base em uma lista pré fornecida em uma planilha Excel. Ele utiliza Selenium para navegar no sistema, realizar login, buscar informações de contato, salvar os números de telefone encontrados de volta na planilha Excel original e disponibiliza para download.

## Pré-requisitos

* Java 17
* Maven
* ChromeDriver (compatível com a versão do seu Chrome)
* Planilha Excel com a coluna "BENEFICIO" preenchida.

## Configuração

1.  **ChromeDriver:**
    * Baixe o ChromeDriver compatível com a sua versão do Google Chrome e coloque-o na pasta `resources/drivers/`.
    * Verifique se o caminho do ChromeDriver está correto no código (`String driverPath = System.getProperty("user.dir") + "/resources/drivers/chromedriver";`).
2.  **Planilha Excel:**
    * Crie uma planilha Excel com uma coluna chamada "BENEFICIO" contendo os nomes dos benefícios que você deseja pesquisar no sistema.
    * Salve a planilha em um local acessível e configure o caminho da planilha no código (`setCaminhoPlanilha`).
3.  **Credenciais:**
    * As credenciais de login do sistema (usuário e senha) serão passadas como parâmetros para o método `executarAutomacao`.

## Como Executar

1.  Clone este repositório.
2.  Navegue até o diretório do projeto no seu terminal.
3.  Execute o seguinte comando para compilar o projeto:

    ```bash
    mvn clean install
    ```

## Tecnologias Utilizadas

Este projeto foi desenvolvido utilizando as seguintes tecnologias:

* **Java:** A linguagem de programação principal para a lógica de automação e manipulação de dados.
* **Selenium WebDriver:** Para automação da interação com a interface web do sistema, permitindo a navegação e extração de dados.
* **Apache POI:** Uma biblioteca Java para manipulação de arquivos Excel, usada para leitura da lista de benefícios e escrita dos números de telefone extraídos.
* **ChromeDriver:** O driver específico do navegador Chrome, necessário para o Selenium WebDriver interagir com o Chrome.
* **Maven:** Para gerenciamento de dependências e construção do projeto.
