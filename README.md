

# Editor Interativo de Saves Brasfoot (.s22)

Este é um editor de linha de comando (CLI) avançado e interativo para visualizar e modificar ficheiros de save (`.s22`) do jogo Brasfoot. A ferramenta foi construída em Java e utiliza a biblioteca Kryo para desserializar e serializar os dados, permitindo uma manipulação profunda da estrutura do save.

Foi projetado para ser robusto, rápido e amigável, mesmo para utilizadores com menos experiência técnica, graças a uma interface colorida, comandos intuitivos e funcionalidades de segurança como backups automáticos.


## Funcionalidades Principais

  * **Interface Amigável:** Um CLI colorido que organiza a informação e melhora a legibilidade.
  * **Backup Automático:** Cria automaticamente um backup (`.bak`) do seu ficheiro de save original ao abri-lo, garantindo que nunca perca os seus dados.
  * **Navegação Hierárquica:** Explore a complexa estrutura de dados do save de forma intuitiva com os comandos `entrar`, `item`, `voltar` e `topo`.
  * **Visualizador com Paginação:** O comando `ver` exibe o conteúdo de listas e arrays gigantes em páginas, permitindo navegar por todos os itens com os comandos `proxima` e `anterior`.
  * **Busca Poderosa:**
      * `buscar`: Realiza uma busca a partir da sua localização atual na estrutura de dados.
      * `busca-global`: Varre o ficheiro de save inteiro à procura de um termo.
  * **Mapeamento Rápido:** O comando `mapear` gera um ficheiro de texto (`.txt`) ultrarrápido, mostrando o caminho e um resumo de todos os objetos que contêm um termo de busca específico.
  * **Edição de Alto Nível:** Modifique entidades do jogo facilmente com comandos como `editarjogador` e `editartime`.
  * **Edição de Baixo Nível:** Altere qualquer campo individualmente com o comando `set`.
  * **Multiplataforma:** Scripts de compilação (`build.sh` e `build.bat`) fornecidos para ambientes Linux/macOS e Windows.

-----

## Requisitos

  * **Java Development Kit (JDK)**: Versão 8 ou superior.

-----

## Como Compilar

O projeto inclui todos os JARs de dependência na pasta `lib/`. Para compilar, utilize o script correspondente ao seu sistema operativo.

**No Windows:**

Abra um terminal (`cmd` ou `PowerShell`) na pasta do projeto e execute:

```bash
.\build.bat
```

**No Linux ou macOS:**

Abra um terminal na pasta do projeto e execute:

```bash
sh ./build.sh
```

Ambos os scripts irão limpar builds antigas, compilar o código-fonte e empacotar tudo num único ficheiro executável chamado `editor-final.jar`.

-----

## Como Executar

Após a compilação, execute o programa com o seguinte comando no seu terminal:

```bash
java -jar editor-final.jar
```

O editor irá iniciar, procurar por ficheiros `.s22` no diretório e pedir-lhe para escolher um para começar a editar.

-----

## Referência de Comandos

Aqui está a lista completa de comandos disponíveis no editor:

| Comando                 | Atalhos | Descrição                                                                                             |
| ----------------------- | ------- | ----------------------------------------------------------------------------------------------------- |
| `ajuda`                 |         | Mostra la lista completa de comandos disponibles.                                                     |
| `ver`                   |         | Lista los campos del objeto actual o el contenido paginado de una lista/array.                       |
| `entrar <campo>`        |         | Navega hacia adentro del objeto contenido en un campo. Ej: `entrar ag`.                                   |
| `item <índice>`         |         | Navega a un ítem específico de una lista o array. Ej: `item 10`.                                     |
| `proxima`               | `p`     | Avanza a la siguiente página al visualizar una lista/array.                                          |
| `anterior`              | `a`     | Retrocede a la página anterior al visualizar una lista/array.                                        |
| `voltar`                |         | Regresa al objeto anterior en la jerarquía de navegación.                                             |
| `topo`                  |         | Regresa directamente al objeto raíz del save.                                                          |
| `buscar <termo>`        |         | Busca un término a partir de tu ubicación actual.                                                    |
| `busca-global <termo>`  |         | Busca un término en todo el archivo de save, desde la raíz.                                          |
| `set <campo> = <valor>` |         | Modifica el valor de un campo en el objeto actual. Ej: `set eq = 99`.                                   |
| `mapear <arq>; <termo>` |         | Mapea y guarda en un `.txt` todos los objetos que contienen el término. Ej: `mapear jogadores.txt; Lionel Messi`. |
| `editarjogador <n>;<i>;<o>` |     | Edita la edad `<i>` y el over `<o>` del jugador `<n>`. Ej: `editarjogador Zico; 35; 99`.                 |
| `editartime <t>;<a>;<v>` |         | Altera o atributo `<a>` para o valor `<v>` em todos os jogadores do time `<t>`. Ex: `editartime Flamengo; eq; 99`. |
| `salvar <arquivo.s22>`  |         | Salva todas as modificações num novo ficheiro de save. Ex: `salvar meu_save_editado.s22`.             |
| `sair`                  |         | Fecha o editor.                                                                                       |

-----

## Exemplo de Utilização

Vamos imaginar que quer encontrar o jogador "Simon Pinelli", aumentar o seu "over" (força) para 99 e salvar.

1.  **Iniciar o editor:**

    ```
    java -jar editor-final.jar
    ```

2.  **Escolher o save:** Selecione o número do seu save na lista.

3.  **Encontrar o jogador:** Para descobrir onde o jogador está, use o comando `mapear`.

    ```
    [raiz] > mapear pinelli.txt; Simon Pinelli
    ```

    Abra o ficheiro `pinelli.txt`. Ele irá mostrar o caminho, por exemplo: `raiz.ag[22]`. Isto significa que ele é o item de índice 22 na lista `ag`.

4.  **Navegar até ao jogador:**

    ```
    [raiz] > entrar ag
    [raiz -> ArrayList (Pág. 1/3682)] > item 22
    [raiz -> ArrayList -> F] >
    ```

5.  **Verificar os dados do jogador:** Agora que está no objeto do jogador, pode ver os seus atributos.

    ```
    [raiz -> ArrayList -> F] > ver
    ```

    A lista de campos será exibida, e você poderá ver o campo da força (geralmente `eq`).

6.  **Modificar o jogador:** Pode usar o comando `set` ou, mais facilmente, o comando `editarjogador`. Vamos voltar à raiz para usar o comando de alto nível.

    ```
    [raiz -> ArrayList -> F] > topo
    [raiz] > editarjogador Simon Pinelli; 25; 99
    ```

7.  **Salvar as alterações:**

    ```
    [raiz] > salvar meu_brasfoot_modificado.s22
    ```

Pronto\! Um novo ficheiro de save foi criado com as suas modificações.

-----

## Estrutura do Projeto

```
brasfoot_editor/
├─ lib/               # Dependências .jar necessárias para compilação
├─ build.bat          # Script de compilação para Windows
├─ build.sh           # Script de compilação para Linux/macOS
├─ EditorInterativo.java # O código-fonte principal da aplicação
└─ shell.nix          # Ficheiro de configuração para ambientes Nix
```