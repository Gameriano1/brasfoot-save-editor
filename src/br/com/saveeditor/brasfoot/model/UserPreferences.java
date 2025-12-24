package br.com.saveeditor.brasfoot.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo para armazenar todas as preferências do usuário.
 */
public class UserPreferences {
    
    // Versão do formato de preferências
    private String version = "3.0";
    private long lastModified;
    
    // Configurações de UI
    private UISettings ui;
    
    // Configurações de arquivos
    private FileSettings files;
    
    // Configurações do editor
    private EditorSettings editor;
    
    // Traduções customizadas
    private Map<String, String> customTranslations;
    
    // Presets favoritos
    private List<String> favoritePresets;
    
    // Presets customizados
    private List<PlayerPreset> customPresets;
    
    public UserPreferences() {
        this.ui = new UISettings();
        this.files = new FileSettings();
        this.editor = new EditorSettings();
        this.customTranslations = new HashMap<>();
        this.favoritePresets = new ArrayList<>();
        this.customPresets = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * Configurações de UI
     */
    public static class UISettings {
        private String theme = "dark";
        private int windowWidth = 1600;
        private int windowHeight = 1000;
        private int windowX = -1;  // -1 = centralizado
        private int windowY = -1;
        private int splitPaneDivider = 350;
        private int fontSize = 13;
        private boolean showTooltips = true;
        private boolean maximized = false;
        
        public Dimension getWindowSize() {
            return new Dimension(windowWidth, windowHeight);
        }
        
        public void setWindowSize(Dimension size) {
            this.windowWidth = size.width;
            this.windowHeight = size.height;
        }
        
        public Point getWindowPosition() {
            if (windowX < 0 || windowY < 0) {
                return null;  // Centralizar
            }
            return new Point(windowX, windowY);
        }
        
        public void setWindowPosition(Point position) {
            if (position != null) {
                this.windowX = position.x;
                this.windowY = position.y;
            }
        }
        
        // Getters e Setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public int getWindowWidth() { return windowWidth; }
        public void setWindowWidth(int windowWidth) { this.windowWidth = windowWidth; }
        public int getWindowHeight() { return windowHeight; }
        public void setWindowHeight(int windowHeight) { this.windowHeight = windowHeight; }
        public int getWindowX() { return windowX; }
        public void setWindowX(int windowX) { this.windowX = windowX; }
        public int getWindowY() { return windowY; }
        public void setWindowY(int windowY) { this.windowY = windowY; }
        public int getSplitPaneDivider() { return splitPaneDivider; }
        public void setSplitPaneDivider(int splitPaneDivider) { this.splitPaneDivider = splitPaneDivider; }
        public int getFontSize() { return fontSize; }
        public void setFontSize(int fontSize) { this.fontSize = fontSize; }
        public boolean isShowTooltips() { return showTooltips; }
        public void setShowTooltips(boolean showTooltips) { this.showTooltips = showTooltips; }
        public boolean isMaximized() { return maximized; }
        public void setMaximized(boolean maximized) { this.maximized = maximized; }
    }
    
    /**
     * Configurações de arquivos
     */
    public static class FileSettings {
        private String lastOpenDirectory = System.getProperty("user.home");
        private String defaultSaveDirectory = null; // Diretório padrão para saves
        private boolean autoLoadLastSave = false;   // Carregar último save automaticamente
        private List<String> recentFiles = new ArrayList<>();
        private int maxRecentFiles = 10;
        private boolean autoBackup = true;
        private String backupDirectory = null;  // null = mesmo diretório do arquivo
        
        public void addRecentFile(String filePath) {
            // Remove se já existe
            recentFiles.remove(filePath);
            // Adiciona no início
            recentFiles.add(0, filePath);
            // Limita o tamanho
            while (recentFiles.size() > maxRecentFiles) {
                recentFiles.remove(recentFiles.size() - 1);
            }
        }
        
        // Getters e Setters
        public String getLastOpenDirectory() { return lastOpenDirectory; }
        public void setLastOpenDirectory(String lastOpenDirectory) { this.lastOpenDirectory = lastOpenDirectory; }
        public String getDefaultSaveDirectory() { return defaultSaveDirectory; }
        public void setDefaultSaveDirectory(String defaultSaveDirectory) { this.defaultSaveDirectory = defaultSaveDirectory; }
        public boolean isAutoLoadLastSave() { return autoLoadLastSave; }
        public void setAutoLoadLastSave(boolean autoLoadLastSave) { this.autoLoadLastSave = autoLoadLastSave; }
        public List<String> getRecentFiles() { return new ArrayList<>(recentFiles); }
        public void setRecentFiles(List<String> recentFiles) { this.recentFiles = new ArrayList<>(recentFiles); }
        public int getMaxRecentFiles() { return maxRecentFiles; }
        public void setMaxRecentFiles(int maxRecentFiles) { this.maxRecentFiles = maxRecentFiles; }
        public boolean isAutoBackup() { return autoBackup; }
        public void setAutoBackup(boolean autoBackup) { this.autoBackup = autoBackup; }
        public String getBackupDirectory() { return backupDirectory; }
        public void setBackupDirectory(String backupDirectory) { this.backupDirectory = backupDirectory; }
    }
    
    /**
     * Configurações do editor
     */
    public static class EditorSettings {
        private boolean autoRefresh = true;
        private int autoRefreshInterval = 5000;  // ms
        private boolean confirmBeforeApplyPreset = true;
        private boolean showFieldDescriptions = true;
        private boolean highlightModifiedFields = true;
        private String activeLocale = "pt_BR";
        private boolean enableCustomLabels = true;
        
        // Getters e Setters
        public boolean isAutoRefresh() { return autoRefresh; }
        public void setAutoRefresh(boolean autoRefresh) { this.autoRefresh = autoRefresh; }
        public int getAutoRefreshInterval() { return autoRefreshInterval; }
        public void setAutoRefreshInterval(int autoRefreshInterval) { this.autoRefreshInterval = autoRefreshInterval; }
        public boolean isConfirmBeforeApplyPreset() { return confirmBeforeApplyPreset; }
        public void setConfirmBeforeApplyPreset(boolean confirmBeforeApplyPreset) { this.confirmBeforeApplyPreset = confirmBeforeApplyPreset; }
        public boolean isShowFieldDescriptions() { return showFieldDescriptions; }
        public void setShowFieldDescriptions(boolean showFieldDescriptions) { this.showFieldDescriptions = showFieldDescriptions; }
        public boolean isHighlightModifiedFields() { return highlightModifiedFields; }
        public void setHighlightModifiedFields(boolean highlightModifiedFields) { this.highlightModifiedFields = highlightModifiedFields; }
        public String getActiveLocale() { return activeLocale; }
        public void setActiveLocale(String activeLocale) { this.activeLocale = activeLocale; }
        public boolean isEnableCustomLabels() { return enableCustomLabels; }
        public void setEnableCustomLabels(boolean enableCustomLabels) { this.enableCustomLabels = enableCustomLabels; }
    }
    
    // Getters e Setters principais
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public UISettings getUi() { return ui; }
    public void setUi(UISettings ui) { this.ui = ui; }
    public FileSettings getFiles() { return files; }
    public void setFiles(FileSettings files) { this.files = files; }
    public EditorSettings getEditor() { return editor; }
    public void setEditor(EditorSettings editor) { this.editor = editor; }
    public Map<String, String> getCustomTranslations() { return new HashMap<>(customTranslations); }
    public void setCustomTranslations(Map<String, String> customTranslations) { this.customTranslations = new HashMap<>(customTranslations); }
    public List<String> getFavoritePresets() { return new ArrayList<>(favoritePresets); }
    public void setFavoritePresets(List<String> favoritePresets) { this.favoritePresets = new ArrayList<>(favoritePresets); }
    public List<PlayerPreset> getCustomPresets() { return customPresets != null ? customPresets : new ArrayList<>(); }
    public void setCustomPresets(List<PlayerPreset> customPresets) { this.customPresets = customPresets != null ? new ArrayList<>(customPresets) : new ArrayList<>(); }
}
