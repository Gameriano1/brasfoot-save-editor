import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Editor Interativo aprimorado para arquivos de save do Brasfoot (.s22).
 *
 * Funcionalidades:
 * - Interface de linha de comando colorida e organizada.
 * - Seleção de arquivo inteligente com listagem automática de saves.
 * - Criação automática de backup (.bak) do save original.
 * - Navegação, visualização e edição de campos.
 * - Comandos de alto nível para editar jogadores e times.
 * - Mapeamento ultrarrápido de dados para um arquivo de texto (.txt) focado em um termo de busca.
 * - Visualizador com paginação para listas e arrays.
 */
public class EditorInterativo {

    // --- Constantes para Cores do Terminal (ANSI) ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";

    // --- Variáveis Estáticas Globais ---
    static Object objetoRaiz;
    static Object dataAfQ;
    static Stack<Object> trilhaNavegacao = new Stack<>();
    static Kryo kryo;
    static Map<Object, Integer> viewState = new HashMap<>(); // Armazena a página atual para coleções
    private static final int ITENS_POR_PAGINA = 20;

    private static class LoadingAnimation implements Runnable {
        private volatile boolean running = true;
        private final Thread thread;

        public LoadingAnimation() { this.thread = new Thread(this); }
        public void start() { thread.start(); }
        public void stop() {
            running = false;
            try { thread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.print("\r" + " ".repeat(40) + "\r");
        }
        @Override
        public void run() {
            char[] spinner = {'|', '/', '-', '\\'};
            int i = 0;
            while (running) {
                System.out.print(ANSI_YELLOW + "\r" + spinner[i++ % spinner.length] + " Processando, por favor aguarde..." + ANSI_RESET);
                try { Thread.sleep(150); } catch (InterruptedException e) { running = false; }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printTitle("Bem-vindo ao Editor Interativo de Saves .s22");
        String filePath = escolherArquivo(scanner);
        if (filePath == null) {
            printError("Nenhum arquivo selecionado. A sair.");
            return;
        }
        try {
            String backupPath = filePath + ".bak";
            System.out.println("A criar backup do ficheiro original em: " + backupPath);
            Files.copy(Paths.get(filePath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
            printSuccess("Backup criado com sucesso!");
        } catch (IOException e) {
            printError("Falha ao criar o backup. Continue por sua conta e risco.");
            System.err.println("Detalhe do erro: " + e.getMessage());
        }
        kryo = new Kryo();
        configurarKryoParaLeitura(kryo);
        System.out.println("\nA tentar ler o ficheiro: " + filePath);
        try (Input input = new Input(new FileInputStream(filePath))) {
            objetoRaiz = kryo.readClassAndObject(input);
            dataAfQ = kryo.readClassAndObject(input);
            trilhaNavegacao.push(objetoRaiz);
            printSuccess("Ficheiro lido! A iniciar interface interativa.");
        } catch (Exception e) {
            printError("Ocorreu um erro fatal ao ler o ficheiro. Verifique se o ficheiro está corrompido.");
            e.printStackTrace();
            return;
        }
        while (true) {
            Object objetoAtual = trilhaNavegacao.peek();
            String path = construirCaminhoNavegacao();
            System.out.print(ANSI_CYAN + "[" + path + "]" + ANSI_YELLOW + " > " + ANSI_RESET);
            String linha = scanner.nextLine().trim();
            if (linha.equalsIgnoreCase("sair")) break;
            if (linha.isEmpty()) continue;
            processarComando(linha);
        }
        scanner.close();
        System.out.println("\nA sair do editor. Adeus!");
    }

    private static String escolherArquivo(Scanner scanner) {
        File diretorioAtual = new File(".");
        File[] savesDisponiveis = diretorioAtual.listFiles((dir, name) -> name.toLowerCase().endsWith(".s22"));
        if (savesDisponiveis == null || savesDisponiveis.length == 0) {
            System.out.println(ANSI_YELLOW + "Nenhum ficheiro .s22 encontrado no diretório atual." + ANSI_RESET);
            System.out.println(ANSI_WHITE + "Por favor, digite o caminho completo para o seu save:" + ANSI_RESET);
        } else {
            System.out.println("Saves encontrados no diretório atual:");
            for (int i = 0; i < savesDisponiveis.length; i++) {
                System.out.printf("  %s[%d]%s %s\n", ANSI_GREEN, i + 1, ANSI_RESET, savesDisponiveis[i].getName());
            }
            System.out.println("\nEscolha um número ou digite o caminho completo para o seu save:");
        }
        while(true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            try {
                int choice = Integer.parseInt(input);
                if (savesDisponiveis != null && choice > 0 && choice <= savesDisponiveis.length) {
                    return savesDisponiveis[choice - 1].getPath();
                } else {
                    printError("Número inválido. Tente novamente.");
                }
            } catch (NumberFormatException e) {
                File chosenFile = new File(input);
                if (!chosenFile.exists()) {
                    printError("Erro: Ficheiro não encontrado em '" + input + "'. Tente novamente.");
                } else if (!input.toLowerCase().endsWith(".s22")) {
                    printError("Erro: O nome do ficheiro deve terminar com a extensão '.s22'. Tente novamente.");
                } else {
                    return input;
                }
            }
        }
    }

    public static void processarComando(String linha) {
        String[] partes = linha.split(" ", 2);
        String comando = partes[0].toLowerCase();
        String arg = (partes.length > 1) ? partes[1] : null;
        Object objetoAtual = trilhaNavegacao.peek();
        try {
            switch (comando) {
                case "ajuda": mostrarAjuda(); break;
                case "ver": listarCampos(objetoAtual); break;
                case "entrar": entrarEmCampo(objetoAtual, arg); break;
                case "item": entrarEmItemDeLista(objetoAtual, arg); break;
                case "voltar":
                    if (trilhaNavegacao.size() > 1) trilhaNavegacao.pop();
                    else printInfo("Já está no topo da hierarquia.");
                    break;
                case "topo":
                    while (trilhaNavegacao.size() > 1) trilhaNavegacao.pop();
                    printInfo("Voltou ao objeto raiz.");
                    break;
                case "buscar":
                    iniciarBusca(objetoAtual, construirCaminhoNavegacaoSimples(), arg);
                    break;
                case "busca-global":
                    iniciarBusca(objetoRaiz, "raiz", arg);
                    break;
                case "set": modificarValor(objetoAtual, arg); break;
                case "proxima": case "p":
                    avancarPagina(objetoAtual);
                    break;
                case "anterior": case "a":
                    retrocederPagina(objetoAtual);
                    break;
                case "salvar":
                    if (arg == null || arg.trim().isEmpty() || !arg.trim().toLowerCase().endsWith(".s22")) {
                        printError("Especifique um nome de ficheiro válido com extensão '.s22'. Ex: salvar meu_save_editado.s22");
                    } else {
                        salvarArquivo(arg.trim());
                    }
                    break;
                case "mapear":
                    if (arg == null || !arg.contains(";")) {
                        printError("Sintaxe incorreta. Use: mapear <arquivo.txt>; <termo_de_busca>");
                    } else {
                        mapearComBusca(arg);
                    }
                    break;
                case "editarjogador": editarJogador(arg); break;
                case "editartime": editarTime(arg); break;
                default:
                    printError("Comando desconhecido. Digite 'ajuda' para ver a lista de comandos.");
            }
        } catch (Exception e) {
            printError("Ocorreu um erro inesperado ao processar o comando: " + e.getMessage());
        }
    }

    private static void mapearComBusca(String arg) {
        String[] args = arg.split(";", 2);
        if (args.length < 2) {
            printError("Sintaxe incorreta. Faltam argumentos. Use: mapear <arquivo.txt>; <termo_de_busca>");
            return;
        }
        String nomeArquivo = args[0].trim();
        String termoBusca = args[1].trim().toLowerCase();

        if (!nomeArquivo.toLowerCase().endsWith(".txt")) {
            nomeArquivo += ".txt";
        }

        System.out.println("\nA procurar por '" + termoBusca + "' para mapear os resultados para " + nomeArquivo + "...");
        LoadingAnimation loader = new LoadingAnimation();
        loader.start();
        
        try (FileWriter fileWriter = new FileWriter(nomeArquivo);
             PrintWriter writer = new PrintWriter(fileWriter)) {
            
            int[] contador = {0}; // Usado como uma referência mutável para contar os resultados
            mapearRecursivo(writer, objetoRaiz, termoBusca, "raiz", new IdentityHashMap<>(), contador);
            
            loader.stop();
            if (contador[0] > 0) {
                printSuccess(contador[0] + " ocorrências encontradas e mapeadas em '" + nomeArquivo + "'.");
            } else {
                printInfo("Nenhuma ocorrência encontrada para o termo '" + termoBusca + "'.");
            }

        } catch (IOException e) {
            loader.stop();
            printError("Erro de I/O ao criar ou escrever no ficheiro de mapa: " + e.getMessage());
        } catch (Exception e) {
            loader.stop();
            printError("Ocorreu um erro inesperado durante o mapeamento.");
            e.printStackTrace();
        }
    }

    private static void mapearRecursivo(PrintWriter writer, Object obj, String termo, String path, Map<Object, String> visitados, int[] contador) {
        if (obj == null || visitados.containsKey(obj)) return;

        if (!isComplexObject(obj)) {
            if (obj.toString().toLowerCase().contains(termo)) {
                writer.println("=".repeat(80));
                writer.println("TERMO ENCONTRADO EM VALOR PRIMITIVO");
                writer.println("  [CAMINHO]: " + path);
                writer.println("  [VALOR]: " + obj);
                writer.println("=".repeat(80) + "\n");
                contador[0]++;
            }
            return;
        }
        
        visitados.put(obj, path);
        
        boolean objetoContemTermo = false;
        try {
            if (obj.toString().toLowerCase().contains(termo)) {
                objetoContemTermo = true;
            }
        } catch (Exception e) {
            // Ignora se o toString() falhar
        }

        List<Field> camposComTermo = new ArrayList<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isSynthetic()) continue;
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                if (fieldValue != null && fieldValue.toString().toLowerCase().contains(termo)) {
                    camposComTermo.add(field);
                }
            } catch (Exception e) { /* Ignora */}
        }
        
        if (objetoContemTermo || !camposComTermo.isEmpty()) {
            writer.println("=".repeat(80));
            writer.println("OBJETO ENCONTRADO CONTENDO O TERMO");
            writer.println("  [CAMINHO DO OBJETO]: " + path);
            writer.println("  [CLASSE]: " + obj.getClass().getName());
            writer.println("--- CAMPOS DO OBJETO ---");
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);
                    String valorStr = (fieldValue == null) ? "null" : fieldValue.toString();
                    if (valorStr.length() > 150) valorStr = valorStr.substring(0, 147) + "...";
                    writer.printf("  > %-25s: %s\n", field.getName(), valorStr);
                } catch (Exception e) {
                    writer.printf("  > %-25s: [Erro ao ler: %s]\n", field.getName(), e.getClass().getSimpleName());
                }
            }
            writer.println("=".repeat(80) + "\n");
            contador[0]++;
        }
        
        if (obj instanceof Collection) {
            int i = 0;
            for (Object item : (Collection<?>) obj) {
                mapearRecursivo(writer, item, termo, path + "[" + i++ + "]", visitados, contador);
            }
        } else if (obj.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(obj); i++) {
                mapearRecursivo(writer, Array.get(obj, i), termo, path + "[" + i + "]", visitados, contador);
            }
        } else {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);
                    mapearRecursivo(writer, fieldValue, termo, path + "." + field.getName(), visitados, contador);
                } catch (Exception e) { /* Ignora */ }
            }
        }
        visitados.remove(obj);
    }
    
    // --- MÉTODOS DO CÓDIGO ORIGINAL (ADAPTADOS PARA A NOVA UI) ---

    private static void editarJogador(String arg) {
         if (arg == null || !arg.contains(";")) {
            printError("Erro de sintaxe. Use: editarjogador <nome do jogador>; <nova idade>; <novo over>");
            return;
        }
        try {
            String[] params = arg.split(";", 3);
            if (params.length != 3) {
                 printError("Erro de sintaxe. Faltam parâmetros. Use: editarjogador <nome>; <idade>; <over>");
                return;
            }
            String nomeJogador = params[0].trim();
            int novaIdade = Integer.parseInt(params[1].trim());
            int novoOver = Integer.parseInt(params[2].trim());
            if (novaIdade <= 0 || novoOver <= 0) {
                 printError("Idade e Over devem ser números positivos.");
                 return;
            }
            System.out.println("A procurar por '" + nomeJogador + "' para editar...");
            boolean encontrado = encontrarEModificarJogadorRecursivo(objetoRaiz, nomeJogador, novaIdade, novoOver, new HashSet<>());
            if (!encontrado) {
                printError("Jogador '" + nomeJogador + "' não encontrado.");
            }
        } catch (NumberFormatException e) {
            printError("A idade e o over devem ser números inteiros válidos. Detalhe: " + e.getMessage());
        } catch (Exception e) {
            printError("Ocorreu um erro ao editar o jogador: " + e.getMessage());
        }
    }

    private static void editarTime(String arg) {
        if (arg == null || !arg.contains(";")) {
            printError("Erro de sintaxe. Use: editartime <nome do time>; <atributo>; <valor>");
            return;
        }
        try {
            String[] params = arg.split(";", 3);
            if (params.length != 3) {
                printError("Erro de sintaxe. Faltam parâmetros. Use: editartime <time>; <atributo>; <valor>");
                return;
            }
            String nomeTime = params[0].trim();
            String atributo = params[1].trim();
            String valorStr = params[2].trim();
            System.out.println("A procurar pelo time: '" + nomeTime + "'...");
            Object timeEncontrado = encontrarObjetoTimeRecursivo(objetoRaiz, nomeTime, new HashSet<>());
            if (timeEncontrado == null) {
                printError("Time '" + nomeTime + "' não encontrado.");
                return;
            }
            printSuccess("Time encontrado! A modificar jogadores...");
            List<?> listaJogadores = null;
            try {
                listaJogadores = (List<?>) getFieldValue(timeEncontrado, "nd");
            } catch (NoSuchFieldException e) {
                printError("O objeto de time (best.ah) não tem o campo 'nd' (lista de jogadores).");
                return;
            }
            if (listaJogadores == null || listaJogadores.isEmpty()) {
                printInfo("O time '" + nomeTime + "' não possui jogadores na lista 'nd'.");
                return;
            }
            int contador = 0;
            Field campo;
            try {
                 campo = listaJogadores.get(0).getClass().getDeclaredField(atributo);
            } catch (NoSuchFieldException e) {
                printError("O atributo '" + atributo + "' não existe nos objetos de jogador.");
                return;
            }
            Object valorConvertido;
            try {
                valorConvertido = converterStringParaTipoDoCampo(valorStr, campo.getType());
            } catch (NumberFormatException e) {
                 printError("Erro de conversão: O valor '" + valorStr + "' não é um formato válido para o tipo do campo (" + campo.getType().getSimpleName() + ").");
                 return;
            }
            for (Object jogador : listaJogadores) {
                try {
                    setFieldValue(jogador, atributo, valorConvertido);
                    contador++;
                } catch (Exception e) { /* Ignora erros individuais */ }
            }
            printSuccess(contador + " jogadores do time '" + nomeTime + "' tiveram o atributo '" + atributo + "' alterado para '" + valorStr + "'.");
            printInfo("Lembre-se de usar o comando 'salvar <nome_arquivo>.s22' para guardar as alterações.");

        } catch (Exception e) {
            printError("Ocorreu um erro inesperado ao editar o time: " + e.getMessage());
        }
    }

    private static boolean encontrarEModificarJogadorRecursivo(Object currentObject, String nomeJogadorAlvo, int novaIdade, int novoOver, Set<Object> visited) {
        if (currentObject == null || !isComplexObject(currentObject) || visited.contains(currentObject)) {
            return false;
        }
        visited.add(currentObject);

        if ("best.F".equals(currentObject.getClass().getName())) {
            try {
                String nomeAtual = (String) getFieldValue(currentObject, "dm");
                if (nomeJogadorAlvo.equals(nomeAtual)) {
                    int idadeAntiga = (int) getFieldValue(currentObject, "em");
                    int overAntigo = (int) getFieldValue(currentObject, "eq");
                    setFieldValue(currentObject, "em", novaIdade);
                    setFieldValue(currentObject, "eq", novoOver);
                    printSuccess("Jogador encontrado e modificado!");
                    System.out.println("  - Idade ('em'): " + idadeAntiga + " -> " + ANSI_GREEN + novaIdade + ANSI_RESET);
                    System.out.println("  - Força ('eq'): " + overAntigo + " -> " + ANSI_GREEN + novoOver + ANSI_RESET);
                    return true;
                }
            } catch (Exception e) { /* Ignora */ }
        }

        if (currentObject instanceof Collection) {
            for (Object item : (Collection<?>) currentObject) {
                if (encontrarEModificarJogadorRecursivo(item, nomeJogadorAlvo, novaIdade, novoOver, visited)) return true;
            }
        } else if (currentObject.getClass().isArray()) {
             for(int i=0; i < Array.getLength(currentObject); i++){
                if (encontrarEModificarJogadorRecursivo(Array.get(currentObject, i), nomeJogadorAlvo, novaIdade, novoOver, visited)) return true;
             }
        } else {
            for (Field field : currentObject.getClass().getDeclaredFields()) {
                try {
                    Object fieldValue = getFieldValue(currentObject, field.getName());
                    if (encontrarEModificarJogadorRecursivo(fieldValue, nomeJogadorAlvo, novaIdade, novoOver, visited)) return true;
                } catch (Exception e) { /* Ignora */ }
            }
        }
        return false;
    }

    private static Object encontrarObjetoTimeRecursivo(Object currentObject, String nomeTimeAlvo, Set<Object> visited) {
        if (currentObject == null || !isComplexObject(currentObject) || visited.contains(currentObject)) {
            return null;
        }
        visited.add(currentObject);

        if ("best.ah".equals(currentObject.getClass().getName())) {
            try {
                String nomeAtual = (String) getFieldValue(currentObject, "dm");
                if (nomeTimeAlvo.equals(nomeAtual)) {
                    return currentObject;
                }
            } catch (Exception e) { /* Ignora */ }
        }

        if (currentObject instanceof Collection) {
            for (Object item : (Collection<?>) currentObject) {
                Object encontrado = encontrarObjetoTimeRecursivo(item, nomeTimeAlvo, visited);
                if (encontrado != null) return encontrado;
            }
        } else if (currentObject.getClass().isArray()) {
            for(int i=0; i< Array.getLength(currentObject); i++) {
                Object encontrado = encontrarObjetoTimeRecursivo(Array.get(currentObject, i), nomeTimeAlvo, visited);
                if (encontrado != null) return encontrado;
            }
        } else {
            for (Field field : currentObject.getClass().getDeclaredFields()) {
                try {
                    Object fieldValue = getFieldValue(currentObject, field.getName());
                    Object encontrado = encontrarObjetoTimeRecursivo(fieldValue, nomeTimeAlvo, visited);
                    if (encontrado != null) return encontrado;
                } catch (Exception e) { /* Ignora */ }
            }
        }
        return null;
    }

    private static void salvarArquivo(String nomeArquivo) {
        Kryo kryoWriter = new Kryo();
        kryoWriter.setClassLoader(EditorInterativo.class.getClassLoader());
        System.out.println("\nA salvar o estado atual para o ficheiro: " + nomeArquivo);
        try (Output output = new Output(new FileOutputStream(nomeArquivo))) {
            kryoWriter.writeClassAndObject(output, objetoRaiz);
            kryoWriter.writeClassAndObject(output, dataAfQ);
            printSuccess("Ficheiro salvo com sucesso!");
        } catch (Exception e) {
            printError("Ocorreu um erro inesperado ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void configurarKryoParaLeitura(Kryo kryo) {
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy()); 
        kryo.setRegistrationRequired(false);
        kryo.setClassLoader(EditorInterativo.class.getClassLoader());
        CollectionSerializer arrayListSerializer = new CollectionSerializer() {
            @Override
            protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                return new ArrayList();
            }
        };
        kryo.register(ArrayList.class, arrayListSerializer);
    }
    
    private static Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    private static void setFieldValue(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private static Object converterStringParaTipoDoCampo(String valorStr, Class<?> tipoCampo) throws NumberFormatException {
        if (tipoCampo == int.class || tipoCampo == Integer.class) return Integer.parseInt(valorStr);
        if (tipoCampo == long.class || tipoCampo == Long.class) return Long.parseLong(valorStr);
        if (tipoCampo == double.class || tipoCampo == Double.class) return Double.parseDouble(valorStr);
        if (tipoCampo == float.class || tipoCampo == Float.class) return Float.parseFloat(valorStr);
        if (tipoCampo == boolean.class || tipoCampo == Boolean.class) {
             String lowerStr = valorStr.toLowerCase();
             if ("true".equals(lowerStr) || "false".equals(lowerStr)) return Boolean.parseBoolean(valorStr);
             throw new NumberFormatException("Valor inválido para booleano. Use 'true' ou 'false'.");
        }
        if (tipoCampo == String.class) return valorStr;
        throw new NumberFormatException("Tipo de campo '" + tipoCampo.getSimpleName() + "' não suportado para modificação.");
    }

    private static void entrarEmCampo(Object obj, String nomeCampo) throws ReflectiveOperationException {
        if (nomeCampo == null) {
            printError("Especifique um campo para entrar. Ex: entrar cE");
            return;
        }
        Field campo;
        try {
            campo = obj.getClass().getDeclaredField(nomeCampo);
        } catch (NoSuchFieldException e) {
            printError("O campo '" + nomeCampo + "' não existe no objeto atual.");
            return;
        }
        campo.setAccessible(true);
        Object valorCampo = campo.get(obj);
        if (valorCampo != null) {
            trilhaNavegacao.push(valorCampo);
            if (valorCampo instanceof Collection || valorCampo.getClass().isArray()) {
                viewState.put(valorCampo, 1);
                listarCampos(valorCampo);
            }
        } else {
            printInfo("O campo '" + nomeCampo + "' é nulo (null).");
        }
    }

    private static void entrarEmItemDeLista(Object obj, String indiceStr) {
        if (indiceStr == null) {
            printError("Especifique um índice. Ex: item 0");
            return;
        }
        try {
            int index = Integer.parseInt(indiceStr);
            Object item;
            if (obj instanceof List) {
                item = ((List<?>) obj).get(index);
            } else if (obj.getClass().isArray()) {
                item = Array.get(obj, index);
            } else {
                printError("O objeto atual não é uma lista ou array.");
                return;
            }
            if (item != null) {
                trilhaNavegacao.push(item);
                 if (item instanceof Collection || item.getClass().isArray()) {
                    viewState.put(item, 1);
                    listarCampos(item);
                }
            } else {
                printInfo("O item no índice " + index + " é nulo (null).");
            }
        } catch (NumberFormatException e) {
            printError("O índice deve ser um número inteiro.");
        } catch (IndexOutOfBoundsException e) {
            printError("Índice fora dos limites da lista/array.");
        }
    }

    private static void iniciarBusca(Object obj, String pathLabel, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            printError("Especifique o texto a procurar. Ex: buscar Mirassol");
            return;
        }
        System.out.println("\nA iniciar busca por '" + searchTerm + "' em '" + pathLabel + "'... isto pode demorar.");
        realizarBusca(obj, searchTerm.toLowerCase(), pathLabel, new HashSet<>());
        System.out.println("\nBusca concluída.");
    }

    private static void modificarValor(Object obj, String arg) throws Exception {
        if (arg == null || !arg.contains("=")) {
            printError("Use o formato 'set <campo> = <valor>'. Ex: set rM = 2077");
            return;
        }
        String[] partes = arg.split("=", 2);
        String nomeCampo = partes[0].trim();
        String valorStr = partes[1].trim();
        Field campo;
        try {
            campo = obj.getClass().getDeclaredField(nomeCampo);
        } catch (NoSuchFieldException e) {
            printError("O campo '" + nomeCampo + "' não existe no objeto atual.");
            return;
        }
        try {
            Object valorConvertido = converterStringParaTipoDoCampo(valorStr, campo.getType());
            setFieldValue(obj, nomeCampo, valorConvertido);
            printSuccess("Campo '" + nomeCampo + "' atualizado para '" + valorStr + "'.");
            printInfo("Use 'salvar <nome_arquivo>.s22' para guardar as alterações.");
        } catch (NumberFormatException e) {
            printError("Erro de conversão: O valor '" + valorStr + "' não é válido para o tipo do campo (" + campo.getType().getSimpleName() + ").");
        }
    }
    
    public static void realizarBusca(Object obj, String searchTerm, String path, Set<Object> visited) {
        if (obj == null || visited.contains(obj) || obj.getClass().isPrimitive()) return;

        if (obj.getClass().getName().startsWith("java.lang") && !(obj instanceof Collection)) {
            if (obj.toString().toLowerCase().contains(searchTerm)) {
                System.out.println(ANSI_GREEN + "  Encontrado em: " + ANSI_YELLOW + path + ANSI_RESET + " -> " + formatarValorParaBusca(obj));
            }
            return;
        }
        
        visited.add(obj);

        if (obj instanceof Collection) {
            int i = 0;
            for (Object item : (Collection<?>) obj) {
                realizarBusca(item, searchTerm, path + "[" + i++ + "]", visited);
            }
        } else if (obj.getClass().isArray()) {
             for(int i=0; i< Array.getLength(obj); i++){
                realizarBusca(Array.get(obj, i), searchTerm, path + "[" + i + "]", visited);
             }
        } else {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);
                    if (fieldValue != null) {
                        if (fieldValue.toString().toLowerCase().contains(searchTerm)) {
                             System.out.println(ANSI_GREEN + "  Encontrado em: " + ANSI_YELLOW + path + "." + field.getName() + ANSI_RESET + " -> " + formatarValorParaBusca(fieldValue));
                        }
                        if (isComplexObject(fieldValue)) {
                             realizarBusca(fieldValue, searchTerm, path + "." + field.getName(), visited);
                        }
                    }
                } catch (Exception e) { /* Ignora */ }
            }
        }
    }
    
    private static String formatarValorParaBusca(Object valor) {
        if (valor == null) {
            return "null";
        }
        String valueStr;
        if (valor instanceof Collection) {
            valueStr = "Lista de " + ((Collection<?>) valor).size() + " itens (Tipo: " + getCollectionType(valor) + ")";
        } else if (valor.getClass().isArray()) {
            valueStr = "Array de " + Array.getLength(valor) + " itens (Tipo: " + valor.getClass().getComponentType().getSimpleName() + ")";
        } else {
            valueStr = valor.toString();
        }
        if (valueStr.length() > 100) {
            valueStr = valueStr.substring(0, 97) + "...";
        }
        return valueStr;
    }
    
    private static boolean isComplexObject(Object obj) {
        return obj != null && !obj.getClass().isPrimitive() && !obj.getClass().getPackageName().startsWith("java");
    }

    public static void printTitle(String title) {
        System.out.println(ANSI_BOLD + ANSI_PURPLE + "\n" + "=".repeat(title.length() + 4));
        System.out.println("= " + title + " =");
        System.out.println("=".repeat(title.length() + 4) + ANSI_RESET + "\n");
    }
    
    public static void printSuccess(String message) {
        System.out.println(ANSI_GREEN + "✔ " + message + ANSI_RESET);
    }
    
    public static void printError(String message) {
        System.err.println(ANSI_RED + "✖ " + message + ANSI_RESET);
    }
    
    public static void printInfo(String message) {
        System.out.println(ANSI_BLUE + "ℹ " + message + ANSI_RESET);
    }

    public static void mostrarAjuda() {
        printTitle("Comandos Disponíveis");
        System.out.println(ANSI_CYAN + "  Navegação:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "    ver                     " + ANSI_RESET + "- Lista os campos do objeto atual ou o conteúdo de uma lista/array.");
        System.out.println(ANSI_YELLOW + "    entrar <nome_campo>     " + ANSI_RESET + "- Navega para dentro do objeto de um campo.");
        System.out.println(ANSI_YELLOW + "    item <índice>           " + ANSI_RESET + "- Navega para um item em uma lista/array.");
        System.out.println(ANSI_YELLOW + "    proxima, p              " + ANSI_RESET + "- Vai para a próxima página de uma lista/array.");
        System.out.println(ANSI_YELLOW + "    anterior, a             " + ANSI_RESET + "- Volta para a página anterior de uma lista/array.");
        System.out.println(ANSI_YELLOW + "    voltar                  " + ANSI_RESET + "- Retorna ao objeto anterior.");
        System.out.println(ANSI_YELLOW + "    topo                    " + ANSI_RESET + "- Retorna ao objeto raiz do save.");
        System.out.println(ANSI_CYAN + "\n  Edição e Análise:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "    set <c> = <v>           " + ANSI_RESET + "- Modifica o valor de um campo. Ex: set rM = 2030");
        System.out.println(ANSI_YELLOW + "    buscar <texto>          " + ANSI_RESET + "- Procura por um texto a partir do objeto atual.");
        System.out.println(ANSI_YELLOW + "    busca-global <texto>    " + ANSI_RESET + "- Procura por um texto em todo o save (a partir da raiz).");
        System.out.println(ANSI_YELLOW + "    mapear <fich.txt>; <termo>" + ANSI_RESET + "- Mapeia e salva os objetos que contêm o termo em um ficheiro de texto.");
        System.out.println(ANSI_YELLOW + "    editarjogador <n>;<i>;<o>" + ANSI_RESET + "- Edita idade <i> e over <o> do jogador <n>.");
        System.out.println(ANSI_YELLOW + "    editartime <t>;<a>;<v>   " + ANSI_RESET + "- Altera o atributo <a> para <v> em todos os jogadores do time <t>.");
        System.out.println(ANSI_CYAN + "\n  Ficheiros:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "    salvar <fich>.s22       " + ANSI_RESET + "- Salva o estado atual para um novo ficheiro.");
        System.out.println(ANSI_CYAN + "\n  Geral:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "    ajuda                   " + ANSI_RESET + "- Mostra esta lista de comandos.");
        System.out.println(ANSI_YELLOW + "    sair                    " + ANSI_RESET + "- Fecha o editor.");
        System.out.println();
    }
    
    private static void avancarPagina(Object obj) {
        if (! (obj instanceof Collection || obj.getClass().isArray())) {
            printError("O comando 'proxima' só pode ser usado dentro de uma lista ou array.");
            return;
        }
        int totalSize = (obj instanceof Collection) ? ((Collection<?>) obj).size() : Array.getLength(obj);
        int totalPages = (int) Math.ceil((double) totalSize / ITENS_POR_PAGINA);
        if (totalPages == 0) totalPages = 1;
        
        int currentPage = viewState.getOrDefault(obj, 1);
        if (currentPage < totalPages) {
            viewState.put(obj, currentPage + 1);
        } else {
            printInfo("Já está na última página.");
        }
        listarConteudoPaginado(obj);
    }
    
    private static void retrocederPagina(Object obj) {
        if (! (obj instanceof Collection || obj.getClass().isArray())) {
            printError("O comando 'anterior' só pode ser usado dentro de uma lista ou array.");
            return;
        }
        int currentPage = viewState.getOrDefault(obj, 1);
        if (currentPage > 1) {
            viewState.put(obj, currentPage - 1);
        } else {
            printInfo("Já está na primeira página.");
        }
        listarConteudoPaginado(obj);
    }

    private static void listarConteudoPaginado(Object obj) {
        String tipo;
        int totalSize;

        if (obj instanceof Collection) {
            tipo = "Lista";
            totalSize = ((Collection<?>) obj).size();
        } else {
            tipo = "Array";
            totalSize = Array.getLength(obj);
        }
        
        String className = obj.getClass().getSimpleName();
        printTitle("Conteúdo de " + tipo + " (" + className + ")");

        if (totalSize == 0) {
            printInfo("A " + tipo.toLowerCase() + " está vazia.");
            System.out.println();
            return;
        }

        int totalPages = (int) Math.ceil((double) totalSize / ITENS_POR_PAGINA);
        if (totalPages == 0) totalPages = 1;
        int currentPage = viewState.getOrDefault(obj, 1);
        if (currentPage > totalPages) {
            currentPage = totalPages;
            viewState.put(obj, currentPage);
        }

        int startIndex = (currentPage - 1) * ITENS_POR_PAGINA;
        int endIndex = Math.min(startIndex + ITENS_POR_PAGINA, totalSize);
        
        System.out.println("A exibir itens " + (startIndex + 1) + " a " + endIndex + " de " + totalSize + ".\n");

        for (int i = startIndex; i < endIndex; i++) {
            Object item = (obj instanceof Collection) ? ((List<?>) obj).get(i) : Array.get(obj, i);
            String itemStr = (item == null) ? "null" : item.toString();
            if (itemStr.length() > 100) itemStr = itemStr.substring(0, 97) + "...";
            System.out.printf(ANSI_GREEN + "  [%d]: " + ANSI_RESET + "%s\n", i, itemStr);
        }

        System.out.println("\n" + ANSI_BLUE + "Página " + currentPage + " de " + totalPages + ANSI_RESET);
        printInfo("Use 'proxima'/'p' ou 'anterior'/'a' para navegar. Use 'item <índice>' para entrar.");
        System.out.println();
    }

    public static void listarCampos(Object obj) {
        if (obj instanceof Collection || obj.getClass().isArray()) {
            listarConteudoPaginado(obj);
            return;
        }

        String header = "--- Campos de " + obj.getClass().getName() + " ---";
        printTitle(header.substring(4, header.length() - 4));

        Field[] fields = obj.getClass().getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        
        System.out.printf(ANSI_BOLD + "%-25s | %-20s | %s\n" + ANSI_RESET, "Nome do Campo", "Tipo", "Valor");
        System.out.println("-".repeat(80));

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                String fieldName = field.getName();
                String fieldType = field.getType().getSimpleName();
                String valueStr = "null";
                if (fieldValue != null) {
                    if (fieldValue instanceof Collection)
                        valueStr = "Lista de " + ((Collection<?>) fieldValue).size() + " itens (Tipo: " + getCollectionType(fieldValue) + ")";
                    else if (fieldValue.getClass().isArray())
                        valueStr = "Array de " + Array.getLength(fieldValue) + " itens (Tipo: " + field.getType().getComponentType().getSimpleName() + ")";
                    else valueStr = fieldValue.toString();
                }
                if (valueStr.length() > 80) valueStr = valueStr.substring(0, 77) + "...";
                System.out.printf(ANSI_CYAN + "%-25s " + ANSI_RESET + "| " + ANSI_YELLOW + "%-20s " + ANSI_RESET + "| %s\n", fieldName, fieldType, valueStr);
            } catch (Exception e) {
                 System.out.printf(ANSI_CYAN + "%-25s " + ANSI_RESET + "| " + ANSI_YELLOW + "%-20s " + ANSI_RESET + "| " + ANSI_RED + "[Inacessível ou erro ao ler]\n" + ANSI_RESET, field.getName(), field.getType().getSimpleName());
            }
        }
        System.out.println();
    }
    
    private static String getCollectionType(Object collection) {
        if (collection instanceof Collection) {
            Collection<?> coll = (Collection<?>) collection;
            if (coll.isEmpty()) return "?";
            Object first = coll.iterator().next();
            return first != null ? first.getClass().getSimpleName() : "?";
        }
        return "?";
    }

    private static String construirCaminhoNavegacao() {
        StringBuilder sb = new StringBuilder("raiz");
        Object topo = trilhaNavegacao.peek();
        
        for (Object obj : trilhaNavegacao) {
            if(obj == objetoRaiz) continue;
            sb.append(ANSI_YELLOW).append(" -> ").append(ANSI_CYAN).append(obj.getClass().getSimpleName());
        }

        if (topo instanceof Collection || topo.getClass().isArray()) {
            int totalSize = (topo instanceof Collection) ? ((Collection<?>) topo).size() : Array.getLength(topo);
            if (totalSize > 0) {
                int totalPages = (int) Math.ceil((double) totalSize / ITENS_POR_PAGINA);
                if (totalPages == 0) totalPages = 1;
                int currentPage = viewState.getOrDefault(topo, 1);
                sb.append(String.format(ANSI_PURPLE + " (Pág. %d/%d)" + ANSI_CYAN, currentPage, totalPages));
            }
        }
        return sb.toString();
    }

    private static String construirCaminhoNavegacaoSimples() {
        if (trilhaNavegacao.isEmpty() || trilhaNavegacao.peek() == objetoRaiz) {
            return "raiz";
        }
        StringBuilder sb = new StringBuilder("raiz");
        for (Object obj : trilhaNavegacao) {
            if (obj == objetoRaiz) continue;
            sb.append(" -> ").append(obj.getClass().getSimpleName());
        }
        return sb.toString();
    }
}

