package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.config.PreferencesManager;
import br.com.saveeditor.brasfoot.model.PlayerPreset;
import br.com.saveeditor.brasfoot.model.PlayerPreset.PresetType;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Serviço para gerenciar e aplicar presets de jogadores.
 */
public class PresetService {

    private final List<PlayerPreset> builtInPresets;
    private final List<PlayerPreset> customPresets;

    public PresetService() {
        this.builtInPresets = new ArrayList<>();
        this.customPresets = new ArrayList<>();
        initializeBuiltInPresets();
        loadCustomPresets();
    }

    private void loadCustomPresets() {
        List<PlayerPreset> savedPresets = PreferencesManager.getInstance().getPreferences().getCustomPresets();
        if (savedPresets != null) {
            for (PlayerPreset p : savedPresets) {
                p.setType(PresetType.CUSTOM);
                this.customPresets.add(p);
            }
        }
    }

    /**
     * Inicializa os presets pré-configurados.
     */
    private void initializeBuiltInPresets() {
        // 1. Buffar ao Máximo
        PlayerPreset buffMax = new PlayerPreset(
                "buff-max",
                "Buffar ao Máximo",
                "Força 100 (máximo absoluto!), idade 16, estrela mundial",
                "⚡",
                PresetType.BUILT_IN);
        buffMax.addAttribute("eq", 100); // Força MÁXIMA
        buffMax.addAttribute("em", 16); // Idade mínima
        buffMax.addBooleanAttribute("el", true); // Estrela Local
        buffMax.addBooleanAttribute("ek", true); // Estrela Mundial
        builtInPresets.add(buffMax);

        // 2. Estrela Mundial
        PlayerPreset worldStar = new PlayerPreset(
                "world-star",
                "Estrela Mundial",
                "Força 95, idade 27, estrela mundial",
                "🌟",
                PresetType.BUILT_IN);
        worldStar.addAttribute("eq", 95);
        worldStar.addAttribute("em", 27);
        worldStar.addBooleanAttribute("el", true);
        worldStar.addBooleanAttribute("ek", true);
        builtInPresets.add(worldStar);

        // 3. Estrela Local
        PlayerPreset localStar = new PlayerPreset(
                "local-star",
                "Estrela Local",
                "Força 85, idade 24, apenas estrela local",
                "⭐",
                PresetType.BUILT_IN);
        localStar.addAttribute("eq", 85);
        localStar.addAttribute("em", 24);
        localStar.addBooleanAttribute("el", true);
        localStar.addBooleanAttribute("ek", false);
        builtInPresets.add(localStar);

        // 4. Jogador Normal
        PlayerPreset normal = new PlayerPreset(
                "normal",
                "Jogador Normal",
                "Força 70, idade 26, sem estrelas",
                "⚽",
                PresetType.BUILT_IN);
        normal.addAttribute("eq", 70);
        normal.addAttribute("em", 26);
        normal.addBooleanAttribute("el", false);
        normal.addBooleanAttribute("ek", false);
        builtInPresets.add(normal);

        // 5. Aposentar
        PlayerPreset retire = new PlayerPreset(
                "retire",
                "Aposentar",
                "Força 50, idade 42, sem estrelas",
                "👴",
                PresetType.BUILT_IN);
        retire.addAttribute("eq", 50);
        retire.addAttribute("em", 42);
        retire.addBooleanAttribute("el", false);
        retire.addBooleanAttribute("ek", false);
        builtInPresets.add(retire);

        // 6. Jovem Promessa
        PlayerPreset youngTalent = new PlayerPreset(
                "young-talent",
                "Jovem Promessa",
                "Força 75, idade 18, sem estrelas ainda",
                "🌱",
                PresetType.BUILT_IN);
        youngTalent.addAttribute("eq", 75);
        youngTalent.addAttribute("em", 18);
        youngTalent.addBooleanAttribute("el", false);
        youngTalent.addBooleanAttribute("ek", false);
        builtInPresets.add(youngTalent);

        // 7. Destruir Time
        PlayerPreset destroyTeam = new PlayerPreset(
                "destroy-team",
                "Destruir Time",
                "Força 1 (mínimo absoluto), idade 45, sem estrelas - Use no time adversário!",
                "💀",
                PresetType.BUILT_IN);
        destroyTeam.addAttribute("eq", 1); // Força MÍNIMA
        destroyTeam.addAttribute("em", 45); // Idade máxima (quase aposentado)
        destroyTeam.addBooleanAttribute("el", false); // Sem estrela local
        destroyTeam.addBooleanAttribute("ek", false); // Sem estrela mundial
        builtInPresets.add(destroyTeam);
    }

    /**
     * Aplica um preset a um jogador específico.
     */
    public void applyPresetToPlayer(PlayerPreset preset, Object playerObject) throws Exception {
        if (preset == null) {
            throw new IllegalArgumentException("Preset não pode ser nulo");
        }
        if (playerObject == null) {
            throw new IllegalArgumentException("Jogador não pode ser nulo");
        }

        preset.applyTo(playerObject);
    }

    /**
     * Aplica um preset a múltiplos jogadores.
     */
    public int applyPresetToPlayers(PlayerPreset preset, List<?> players) {
        if (preset == null || players == null) {
            return 0;
        }

        int successCount = 0;
        for (Object player : players) {
            try {
                preset.applyTo(player);
                successCount++;
            } catch (Exception e) {
                System.err.println("❌ Erro ao aplicar preset: " + e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * Aplica um preset a todos os jogadores de um time.
     */
    public int applyPresetToTeam(PlayerPreset preset, Object teamObject) throws Exception {
        if (teamObject == null) {
            throw new IllegalArgumentException("Time não pode ser nulo");
        }

        // Obter lista de jogadores do time (campo 'nd')
        List<?> players = (List<?>) ReflectionUtils.getFieldValue(teamObject, "nd");

        if (players == null || players.isEmpty()) {
            return 0;
        }

        return applyPresetToPlayers(preset, players);
    }

    /**
     * Busca um preset por ID (built-in ou custom).
     */
    public Optional<PlayerPreset> getPresetById(String id) {
        // Buscar em built-in primeiro
        for (PlayerPreset preset : builtInPresets) {
            if (preset.getId().equals(id)) {
                return Optional.of(preset);
            }
        }

        // Buscar em custom
        for (PlayerPreset preset : customPresets) {
            if (preset.getId().equals(id)) {
                return Optional.of(preset);
            }
        }

        return Optional.empty();
    }

    /**
     * Adiciona um preset customizado.
     */
    public void addCustomPreset(PlayerPreset preset) {
        if (preset == null) {
            throw new IllegalArgumentException("Preset não pode ser nulo");
        }

        if (!preset.validate()) {
            throw new IllegalArgumentException("Preset inválido");
        }

        // Remover se já existe
        customPresets.removeIf(p -> p.getId().equals(preset.getId()));

        // Adicionar
        preset.setType(PresetType.CUSTOM);
        customPresets.add(preset);

        // Persistir
        PreferencesManager.getInstance().saveCustomPreset(preset);
    }

    /**
     * Remove um preset customizado.
     */
    public boolean removeCustomPreset(String id) {
        boolean removed = customPresets.removeIf(p -> p.getId().equals(id));
        if (removed) {
            PreferencesManager.getInstance().removeCustomPreset(id);
        }
        return removed;
    }

    /**
     * Retorna todos os presets disponíveis.
     */
    public List<PlayerPreset> getAllPresets() {
        List<PlayerPreset> all = new ArrayList<>();
        all.addAll(builtInPresets);
        all.addAll(customPresets);
        return all;
    }

    /**
     * Retorna apenas presets built-in.
     */
    public List<PlayerPreset> getBuiltInPresets() {
        return new ArrayList<>(builtInPresets);
    }

    /**
     * Retorna apenas presets customizados.
     */
    public List<PlayerPreset> getCustomPresets() {
        return new ArrayList<>(customPresets);
    }

    /**
     * Aplica preset em massa a todos os jogadores do save.
     */
    public int applyPresetToAllPlayers(PlayerPreset preset, Object rootObject) {
        Set<Object> visited = new HashSet<>();
        int count = applyPresetRecursive(preset, rootObject, visited);
        System.out.println("✅ Preset aplicado a " + count + " jogadores");
        return count;
    }

    /**
     * Aplica preset a todos os jogadores de um time específico pelo nome.
     * Usa a mesma lógica do EditorService.editarTime.
     */
    public int applyPresetToTeamByName(PlayerPreset preset, Object rootObject, String teamName) {
        if (preset == null || rootObject == null || teamName == null || teamName.trim().isEmpty()) {
            return 0;
        }

        System.out.println("🔍 Procurando pelo time: '" + teamName + "'...");

        // Buscar lista de times no campo 'aj' do objeto raiz (igual
        // EditorService.editarTime)
        List<?> listaDeTimes;
        try {
            listaDeTimes = (List<?>) ReflectionUtils.getFieldValue(rootObject, "aj");
        } catch (Exception e) {
            System.out.println("❌ Campo 'aj' (lista de times) não encontrado");
            return 0;
        }

        // Buscar o time pelo nome
        Object timeEncontrado = null;
        for (Object time : listaDeTimes) {
            try {
                String nomeAtual = (String) ReflectionUtils.getFieldValue(time, "dm");
                if (teamName.trim().equalsIgnoreCase(nomeAtual)) {
                    timeEncontrado = time;
                    System.out.println("✅ Time encontrado: " + nomeAtual);
                    break;
                }
            } catch (Exception e) {
                // Ignorar
            }
        }

        if (timeEncontrado == null) {
            System.out.println("⚠️ Time '" + teamName + "' não encontrado");
            return 0;
        }

        System.out.println("✅ Time encontrado! Aplicando preset aos jogadores...");

        // Pegar lista de jogadores do campo 'nd' (igual EditorService.editarTime)
        List<?> listaJogadores;
        try {
            listaJogadores = (List<?>) ReflectionUtils.getFieldValue(timeEncontrado, "nd");
        } catch (Exception e) {
            System.out.println("❌ Campo 'nd' (lista de jogadores) não encontrado");
            return 0;
        }

        if (listaJogadores == null || listaJogadores.isEmpty()) {
            System.out.println("⚠️ Time não possui jogadores");
            return 0;
        }

        // Aplicar preset em cada jogador (igual EditorService.editarTime)
        int contador = 0;
        for (Object jogador : listaJogadores) {
            try {
                preset.applyTo(jogador);
                contador++;
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao aplicar preset em jogador: " + e.getMessage());
            }
        }

        System.out.println("✅ Preset aplicado a " + contador + " jogadores do time '" + teamName + "'");
        return contador;
    }

    /**
     * Busca recursiva e aplicação de preset.
     */
    private int applyPresetRecursive(PlayerPreset preset, Object currentObject, Set<Object> visited) {
        if (currentObject == null || !ReflectionUtils.isComplexObject(currentObject)
                || visited.contains(currentObject)) {
            return 0;
        }
        visited.add(currentObject);

        int count = 0;

        // Se é um jogador (classe F), aplicar preset
        if ("best.F".equals(currentObject.getClass().getName())) {
            try {
                preset.applyTo(currentObject);
                count++;
            } catch (Exception e) {
                System.err.println("⚠ Erro ao aplicar preset: " + e.getMessage());
            }
        }

        // Continuar busca recursiva
        if (currentObject instanceof Collection) {
            for (Object item : (Collection<?>) currentObject) {
                count += applyPresetRecursive(preset, item, visited);
            }
        } else if (currentObject.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(currentObject); i++) {
                count += applyPresetRecursive(preset, Array.get(currentObject, i), visited);
            }
        } else {
            for (java.lang.reflect.Field field : currentObject.getClass().getDeclaredFields()) {
                try {
                    Object fieldValue = ReflectionUtils.getFieldValue(currentObject, field.getName());
                    count += applyPresetRecursive(preset, fieldValue, visited);
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }

        return count;
    }
}
