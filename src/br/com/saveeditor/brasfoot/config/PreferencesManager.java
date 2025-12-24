package br.com.saveeditor.brasfoot.config;

import br.com.saveeditor.brasfoot.model.PlayerPreset;
import br.com.saveeditor.brasfoot.model.UserPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gerenciador singleton de preferências do usuário.
 * Usa JSON para persistência.
 */
public class PreferencesManager {

    private static PreferencesManager instance;
    private UserPreferences preferences;
    private final Gson gson;
    private final Path preferencesFile;

    private PreferencesManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // Diretório de configuração: ~/.brasfoot-editor/
        String userHome = System.getProperty("user.home");
        Path configDir = Paths.get(userHome, ".brasfoot-editor");

        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            System.err.println("⚠ Não foi possível criar diretório de configuração");
        }

        this.preferencesFile = configDir.resolve("preferences.json");
        this.preferences = load();
    }

    /**
     * Obtém a instância singleton.
     */
    public static synchronized PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }

    /**
     * Carrega as preferências do arquivo JSON.
     */
    private UserPreferences load() {
        if (!Files.exists(preferencesFile)) {
            System.out.println("📋 Criando preferências padrão");
            return new UserPreferences();
        }

        try (Reader reader = new FileReader(preferencesFile.toFile())) {
            UserPreferences loaded = gson.fromJson(reader, UserPreferences.class);
            if (loaded != null) {
                System.out.println("✅ Preferências carregadas");
                return loaded;
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar preferências: " + e.getMessage());
        }

        return new UserPreferences();
    }

    /**
     * Salva as preferências no arquivo JSON.
     */
    public void save() {
        try (Writer writer = new FileWriter(preferencesFile.toFile())) {
            preferences.setLastModified(System.currentTimeMillis());
            gson.toJson(preferences, writer);
            System.out.println("💾 Preferências salvas");
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar preferências: " + e.getMessage());
        }
    }

    /**
     * Retorna as preferências atuais.
     */
    public UserPreferences getPreferences() {
        return preferences;
    }

    // ===== ATALHOS DE ACESSO =====

    /**
     * Obtém o último diretório aberto.
     */
    public String getLastOpenDirectory() {
        return preferences.getFiles().getLastOpenDirectory();
    }

    /**
     * Define o último diretório aberto.
     */
    public void setLastOpenDirectory(String directory) {
        preferences.getFiles().setLastOpenDirectory(directory);
        save();
    }

    /**
     * Adiciona um arquivo aos recentes.
     */
    public void addRecentFile(String filePath) {
        preferences.getFiles().addRecentFile(filePath);
        save();
    }

    /**
     * Obtém arquivos recentes.
     */
    public java.util.List<String> getRecentFiles() {
        return preferences.getFiles().getRecentFiles();
    }

    /**
     * Obtém o diretório padrão para saves.
     */
    public String getDefaultSaveDirectory() {
        return preferences.getFiles().getDefaultSaveDirectory();
    }

    /**
     * Define o diretório padrão para saves.
     */
    public void setDefaultSaveDirectory(String directory) {
        preferences.getFiles().setDefaultSaveDirectory(directory);
        save();
    }

    /**
     * Verifica se deve carregar o último save automaticamente.
     */
    public boolean isAutoLoadLastSave() {
        return preferences.getFiles().isAutoLoadLastSave();
    }

    /**
     * Define se deve carregar o último save automaticamente.
     */
    public void setAutoLoadLastSave(boolean autoLoad) {
        preferences.getFiles().setAutoLoadLastSave(autoLoad);
        save();
    }

    /**
     * Salva a posição e tamanho da janela.
     */
    public void saveWindowState(Window window) {
        if (window == null)
            return;

        UserPreferences.UISettings ui = preferences.getUi();

        // Salvar se está maximizado
        if (window instanceof Frame) {
            Frame frame = (Frame) window;
            ui.setMaximized((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH);
        }

        // Salvar tamanho e posição apenas se não estiver maximizado
        if (!ui.isMaximized()) {
            ui.setWindowSize(window.getSize());
            ui.setWindowPosition(window.getLocation());
        }

        save();
    }

    /**
     * Restaura a posição e tamanho da janela.
     */
    public void restoreWindowState(Window window) {
        if (window == null)
            return;

        UserPreferences.UISettings ui = preferences.getUi();

        // Restaurar tamanho
        Dimension size = ui.getWindowSize();
        if (size != null && size.width > 0 && size.height > 0) {
            window.setSize(size);
        }

        // Restaurar posição
        Point position = ui.getWindowPosition();
        if (position != null) {
            window.setLocation(position);
        } else {
            window.setLocationRelativeTo(null); // Centralizar
        }

        // Restaurar se estava maximizado
        if (window instanceof Frame && ui.isMaximized()) {
            Frame frame = (Frame) window;
            frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        }
    }

    /**
     * Salva o divisor do split pane.
     */
    public void saveSplitPaneDivider(int dividerLocation) {
        preferences.getUi().setSplitPaneDivider(dividerLocation);
        save();
    }

    /**
     * Obtém a posição do divisor do split pane.
     */
    public int getSplitPaneDivider() {
        return preferences.getUi().getSplitPaneDivider();
    }

    /**
     * Verifica se auto-refresh está habilitado.
     */
    public boolean isAutoRefreshEnabled() {
        return preferences.getEditor().isAutoRefresh();
    }

    /**
     * Define se auto-refresh está habilitado.
     */
    public void setAutoRefreshEnabled(boolean enabled) {
        preferences.getEditor().setAutoRefresh(enabled);
        save();
    }

    /**
     * Obtém o intervalo de auto-refresh em ms.
     */
    public int getAutoRefreshInterval() {
        return preferences.getEditor().getAutoRefreshInterval();
    }

    /**
     * Obtém tradução customizada de um campo.
     */
    public String getCustomTranslation(String fieldName) {
        return preferences.getCustomTranslations().get(fieldName);
    }

    /**
     * Define tradução customizada de um campo.
     */
    public void setCustomTranslation(String fieldName, String translation) {
        preferences.getCustomTranslations().put(fieldName, translation);
        save();
    }

    /**
     * Adiciona um preset aos favoritos.
     */
    public void addFavoritePreset(String presetId) {
        if (!preferences.getFavoritePresets().contains(presetId)) {
            preferences.getFavoritePresets().add(presetId);
            save();
        }
    }

    /**
     * Remove um preset dos favoritos.
     */
    public void removeFavoritePreset(String presetId) {
        if (preferences.getFavoritePresets().remove(presetId)) {
            save();
        }
    }

    /**
     * Verifica se um preset é favorito.
     */
    public boolean isFavoritePreset(String presetId) {
        return preferences.getFavoritePresets().contains(presetId);
    }

    /**
     * Salva um preset customizado.
     */
    public void saveCustomPreset(PlayerPreset preset) {
        // Adicionar à lista de presets customizados
        preferences.getCustomPresets().removeIf(p -> p.getId().equals(preset.getId()));
        preferences.getCustomPresets().add(preset);
        save();
    }

    /**
     * Remove um preset customizado.
     */
    public void removeCustomPreset(String presetId) {
        preferences.getCustomPresets().removeIf(p -> p.getId().equals(presetId));
        save();
    }

    /**
     * Reset para preferências padrão.
     */
    public void resetToDefaults() {
        this.preferences = new UserPreferences();
        save();
        System.out.println("🔄 Preferências resetadas para padrão");
    }
}
