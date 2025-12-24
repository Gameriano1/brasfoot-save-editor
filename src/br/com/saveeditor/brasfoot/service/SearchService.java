package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.util.ReflectionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Serviço de busca e mapeamento de dados.
 */
public class SearchService {
    
    // Helper para Java 8 (String.repeat foi adicionado no Java 11)
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public void iniciarBusca(Object obj, String pathLabel, String searchTerm, boolean isGlobal) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Especifique o texto a procurar");
        }
        System.out.println("\n🔍 A iniciar busca por '" + searchTerm + "'...");
        realizarBusca(obj, searchTerm.toLowerCase(), pathLabel, new HashSet<>(), isGlobal);
        System.out.println("\n✔ Busca concluída");
    }

    private void realizarBusca(Object obj, String searchTerm, String path, Set<Object> visited, boolean isGlobal) {
        if (obj == null || visited.contains(obj) || obj.getClass().isPrimitive()) return;

        if (obj.getClass().getName().startsWith("java.lang") && !(obj instanceof Collection)) {
            if (obj.toString().toLowerCase().contains(searchTerm)) {
                System.out.println("  ✓ " + path + " → " + formatValue(obj));
            }
            return;
        }

        visited.add(obj);

        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            int i = 0;
            for (Object item : coll) {
                if (isGlobal && item != null && contem(item, searchTerm)) {
                    // Em busca global, mostrar apenas o índice se o item contém o termo
                    System.out.println("  ✓ " + path + "[" + i + "]");
                }
                realizarBusca(item, searchTerm, path + "[" + i + "]", visited, isGlobal);
                i++;
            }
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object item = Array.get(obj, i);
                if (isGlobal && item != null && contem(item, searchTerm)) {
                    // Em busca global, mostrar apenas o índice se o item contém o termo
                    System.out.println("  ✓ " + path + "[" + i + "]");
                }
                realizarBusca(item, searchTerm, path + "[" + i + "]", visited, isGlobal);
            }
        } else {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);
                    if (fieldValue != null) {
                        if (fieldValue.toString().toLowerCase().contains(searchTerm)) {
                            System.out.println("  ✓ " + path + "." + field.getName() + " → " + formatValue(fieldValue));
                        }
                        if (ReflectionUtils.isComplexObject(fieldValue)) {
                            realizarBusca(fieldValue, searchTerm, path + "." + field.getName(), visited, isGlobal);
                        }
                    }
                } catch (Exception e) { /* Ignora */ }
            }
        }
    }
    
    /**
     * Verifica se um objeto contém o termo de busca (recursivamente, mas superficial).
     */
    private boolean contem(Object obj, String searchTerm) {
        if (obj == null) return false;
        
        // Verificar o toString do objeto
        try {
            if (obj.toString().toLowerCase().contains(searchTerm)) {
                return true;
            }
        } catch (Exception e) {
            // Ignorar
        }
        
        // Se for um objeto complexo, verificar seus campos (apenas nível 1)
        if (ReflectionUtils.isComplexObject(obj)) {
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isSynthetic()) continue;
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value != null && value.toString().toLowerCase().contains(searchTerm)) {
                        return true;
                    }
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }
        
        return false;
    }

    private String formatValue(Object value) {
        if (value == null) return "null";
        String str = value.toString();
        return str.length() > 80 ? str.substring(0, 77) + "..." : str;
    }

    public void mapearComBusca(Object objetoRaiz, String arg) {
        String[] args = arg.split(";", 2);
        if (args.length < 2) {
            throw new IllegalArgumentException("Sintaxe: <arquivo.txt>; <termo>");
        }
        String nomeArquivo = args[0].trim();
        String termoBusca = args[1].trim().toLowerCase();

        if (!nomeArquivo.toLowerCase().endsWith(".txt")) {
            nomeArquivo += ".txt";
        }

        System.out.println("\n🔍 A procurar por '" + termoBusca + "' para mapear...");

        try (FileWriter fileWriter = new FileWriter(nomeArquivo);
             PrintWriter writer = new PrintWriter(fileWriter)) {

            int[] contador = {0};
            mapearRecursivo(writer, objetoRaiz, termoBusca, "raiz", new IdentityHashMap<>(), contador);

            if (contador[0] > 0) {
                System.out.println("✔ " + contador[0] + " ocorrências mapeadas em '" + nomeArquivo + "'");
            } else {
                System.out.println("⚠ Nenhuma ocorrência encontrada");
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar ficheiro: " + e.getMessage(), e);
        }
    }

    private void mapearRecursivo(PrintWriter writer, Object obj, String termo, String path, Map<Object, String> visitados, int[] contador) {
        if (obj == null || visitados.containsKey(obj)) return;

        if (!ReflectionUtils.isComplexObject(obj)) {
            if (obj.toString().toLowerCase().contains(termo)) {
                writer.println(repeat("=", 80));
                writer.println("TERMO ENCONTRADO");
                writer.println("  [CAMINHO]: " + path);
                writer.println("  [VALOR]: " + obj);
                writer.println(repeat("=", 80) + "\n");
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
        } catch (Exception e) { /* Ignora */ }

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
            writer.println(repeat("=", 80));
            writer.println("OBJETO ENCONTRADO");
            writer.println("  [CAMINHO]: " + path);
            writer.println("  [CLASSE]: " + obj.getClass().getName());
            writer.println("--- CAMPOS ---");
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(obj);
                    String valorStr = (fieldValue == null) ? "null" : fieldValue.toString();
                    if (valorStr.length() > 150) valorStr = valorStr.substring(0, 147) + "...";
                    writer.printf("  > %-25s: %s\n", field.getName(), valorStr);
                } catch (Exception e) {
                    writer.printf("  > %-25s: [Erro]\n", field.getName());
                }
            }
            writer.println(repeat("=", 80) + "\n");
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
}