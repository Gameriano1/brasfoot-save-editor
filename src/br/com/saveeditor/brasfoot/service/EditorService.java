package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Serviço de edição e navegação na estrutura de dados.
 */
public class EditorService {

    public void entrarEmCampo(NavegacaoState estado, String nomeCampo) throws ReflectiveOperationException {
        if (nomeCampo == null) {
            throw new IllegalArgumentException("Especifique um campo para entrar");
        }
        Object obj = estado.getObjetoAtual();
        Field campo;
        try {
            campo = obj.getClass().getDeclaredField(nomeCampo);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException("O campo '" + nomeCampo + "' não existe");
        }
        campo.setAccessible(true);
        Object valorCampo = campo.get(obj);
        if (valorCampo != null) {
            estado.entrar(valorCampo);
        } else {
            throw new IllegalStateException("O campo '" + nomeCampo + "' é nulo");
        }
    }

    public void entrarEmItemDeLista(NavegacaoState estado, String indiceStr) {
        if (indiceStr == null) {
            throw new IllegalArgumentException("Especifique um índice");
        }
        Object obj = estado.getObjetoAtual();
        try {
            int index = Integer.parseInt(indiceStr);
            Object item;
            if (obj instanceof List) {
                item = ((List<?>) obj).get(index);
            } else if (obj.getClass().isArray()) {
                item = Array.get(obj, index);
            } else {
                throw new IllegalStateException("O objeto atual não é uma lista ou array");
            }
            if (item != null) {
                estado.entrar(item);
            } else {
                throw new IllegalStateException("O item no índice " + index + " é nulo");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O índice deve ser um número inteiro");
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Índice fora dos limites");
        }
    }

    public void modificarValor(Object obj, String arg) throws Exception {
        if (obj instanceof Collection || obj.getClass().isArray()){
            throw new IllegalStateException("Não pode usar modificação direta numa lista");
        }
        if (arg == null || !arg.contains("=")) {
            throw new IllegalArgumentException("Use o formato '<campo> = <valor>'");
        }
        String[] partes = arg.split("=", 2);
        String nomeCampo = partes[0].trim();
        String valorStr = partes[1].trim();
        Field campo;
        try {
            campo = obj.getClass().getDeclaredField(nomeCampo);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException("O campo '" + nomeCampo + "' não existe");
        }
        try {
            Object valorConvertido = ReflectionUtils.converterStringParaTipoDoCampo(valorStr, campo.getType());
            ReflectionUtils.setFieldValue(obj, nomeCampo, valorConvertido);
            System.out.println("✔ Campo '" + nomeCampo + "' atualizado para '" + valorStr + "'");
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Erro de conversão: O valor '" + valorStr + "' não é válido para o tipo " + campo.getType().getSimpleName());
        }
    }
    
    public void editarTodosOsItens(Object colecaoOuArray, String arg) {
        if (!(colecaoOuArray instanceof Collection || colecaoOuArray.getClass().isArray())) {
            throw new IllegalStateException("Só pode ser usado em lista ou array");
        }
        if (arg == null || !arg.contains("=")) {
            throw new IllegalArgumentException("Use o formato '<campo> = <valor>'");
        }

        String[] partes = arg.split("=", 2);
        String nomeCampo = partes[0].trim();
        String valorStr = partes[1].trim();

        int totalItens = (colecaoOuArray instanceof Collection) ? ((Collection<?>) colecaoOuArray).size() : Array.getLength(colecaoOuArray);
        if (totalItens == 0) {
            return;
        }

        int sucessos = 0;
        Iterator<?> iterator = (colecaoOuArray instanceof Collection)
                ? ((Collection<?>) colecaoOuArray).iterator()
                : null;

        for (int i = 0; i < totalItens; i++) {
            Object item = (iterator != null) ? iterator.next() : Array.get(colecaoOuArray, i);
            if (item == null) continue;

            try {
                Field campo = item.getClass().getDeclaredField(nomeCampo);
                Object valorConvertido = ReflectionUtils.converterStringParaTipoDoCampo(valorStr, campo.getType());
                ReflectionUtils.setFieldValue(item, nomeCampo, valorConvertido);
                sucessos++;
            } catch (NoSuchFieldException e) {
                // Ignora
            } catch (Exception e) {
                System.err.println("✖ Falha ao editar item " + i + ": " + e.getMessage());
            }
        }

        System.out.println("✔ Campo '" + nomeCampo + "' modificado em " + sucessos + " de " + totalItens + " itens");
    }

    public void editarJogador(Object objetoRaiz, String arg) {
        if (arg == null || !arg.contains(";")) {
            throw new IllegalArgumentException("Use: <nome>; <idade>; <over>");
        }
        try {
            String[] params = arg.split(";", 3);
            if (params.length != 3) {
                throw new IllegalArgumentException("Faltam parâmetros");
            }
            String nomeJogador = params[0].trim();
            int novaIdade = Integer.parseInt(params[1].trim());
            int novoOver = Integer.parseInt(params[2].trim());
            
            if (novaIdade <= 0 || novoOver <= 0) {
                throw new IllegalArgumentException("Idade e Over devem ser positivos");
            }
            
            System.out.println("A procurar por '" + nomeJogador + "'...");
            boolean encontrado = encontrarEModificarJogadorRecursivo(objetoRaiz, nomeJogador, novaIdade, novoOver, new HashSet<>());
            
            if (!encontrado) {
                throw new IllegalStateException("Jogador '" + nomeJogador + "' não encontrado");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Idade e over devem ser números válidos");
        }
    }

    private boolean encontrarEModificarJogadorRecursivo(Object currentObject, String nomeJogadorAlvo, int novaIdade, int novoOver, Set<Object> visited) {
        if (currentObject == null || !ReflectionUtils.isComplexObject(currentObject) || visited.contains(currentObject)) {
            return false;
        }
        visited.add(currentObject);

        if ("best.F".equals(currentObject.getClass().getName())) {
            try {
                String nomeAtual = (String) ReflectionUtils.getFieldValue(currentObject, "dm");
                if (nomeJogadorAlvo.equals(nomeAtual)) {
                    int idadeAntiga = (int) ReflectionUtils.getFieldValue(currentObject, "em");
                    int overAntigo = (int) ReflectionUtils.getFieldValue(currentObject, "eq");
                    ReflectionUtils.setFieldValue(currentObject, "em", novaIdade);
                    ReflectionUtils.setFieldValue(currentObject, "eq", novoOver);
                    System.out.println("✔ Jogador encontrado e modificado!");
                    System.out.println("  - Idade: " + idadeAntiga + " → " + novaIdade);
                    System.out.println("  - Força: " + overAntigo + " → " + novoOver);
                    return true;
                }
            } catch (Exception e) { /* Ignora */ }
        }

        if (currentObject instanceof Collection) {
            for (Object item : (Collection<?>) currentObject) {
                if (encontrarEModificarJogadorRecursivo(item, nomeJogadorAlvo, novaIdade, novoOver, visited)) return true;
            }
        } else if (currentObject.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(currentObject); i++) {
                if (encontrarEModificarJogadorRecursivo(Array.get(currentObject, i), nomeJogadorAlvo, novaIdade, novoOver, visited)) return true;
            }
        } else {
            for (Field field : currentObject.getClass().getDeclaredFields()) {
                try {
                    Object fieldValue = ReflectionUtils.getFieldValue(currentObject, field.getName());
                    if (encontrarEModificarJogadorRecursivo(fieldValue, nomeJogadorAlvo, novaIdade, novoOver, visited)) return true;
                } catch (Exception e) { /* Ignora */ }
            }
        }
        return false;
    }

    public void editarTime(Object objetoRaiz, String arg) {
        if (arg == null || !arg.contains(";")) {
            throw new IllegalArgumentException("Use: <time>; <atributo>; <valor>");
        }
        try {
            String[] params = arg.split(";", 3);
            if (params.length != 3) {
                throw new IllegalArgumentException("Faltam parâmetros");
            }
            String nomeTime = params[0].trim();
            String atributo = params[1].trim();
            String valorStr = params[2].trim();

            System.out.println("A procurar pelo time: '" + nomeTime + "'...");
            List<?> listaDeTimes;
            try {
                listaDeTimes = (List<?>) ReflectionUtils.getFieldValue(objetoRaiz, "aj");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Campo 'aj' não encontrado");
            } catch (ClassCastException e) {
                throw new IllegalStateException("Campo 'aj' não é uma lista");
            }

            Object timeEncontrado = null;
            for (Object time : listaDeTimes) {
                try {
                    String nomeAtual = (String) ReflectionUtils.getFieldValue(time, "dm");
                    if (nomeTime.equalsIgnoreCase(nomeAtual)) {
                        timeEncontrado = time;
                        break;
                    }
                } catch (Exception e) { /* Ignorar */ }
            }

            if (timeEncontrado == null) {
                throw new IllegalStateException("Time '" + nomeTime + "' não encontrado");
            }

            System.out.println("✔ Time encontrado! A modificar jogadores...");
            List<?> listaJogadores;
            try {
                listaJogadores = (List<?>) ReflectionUtils.getFieldValue(timeEncontrado, "nd");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Campo 'nd' não encontrado");
            }

            if (listaJogadores == null || listaJogadores.isEmpty()) {
                System.out.println("⚠ Time não possui jogadores");
                return;
            }

            int contador = 0;
            Field campo;
            try {
                campo = listaJogadores.get(0).getClass().getDeclaredField(atributo);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Atributo '" + atributo + "' não existe");
            }

            Object valorConvertido = ReflectionUtils.converterStringParaTipoDoCampo(valorStr, campo.getType());

            for (Object jogador : listaJogadores) {
                try {
                    ReflectionUtils.setFieldValue(jogador, atributo, valorConvertido);
                    contador++;
                } catch (Exception e) { /* Ignora */ }
            }
            
            System.out.println("✔ " + contador + " jogadores do time '" + nomeTime + "' modificados");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao editar time: " + e.getMessage(), e);
        }
    }
}