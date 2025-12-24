package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.config.LabelTranslator;
import br.com.saveeditor.brasfoot.config.PreferencesManager;
import br.com.saveeditor.brasfoot.model.UserPreferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 * Diálogo de configurações/preferências da aplicação.
 */
public class PreferencesDialog extends JDialog {

    private final PreferencesManager preferencesManager;
    private final LabelTranslator labelTranslator;

    private JCheckBox autoRefreshCheck;
    private JSpinner autoRefreshIntervalSpinner;
    private JCheckBox confirmBeforePresetCheck;
    private JCheckBox showTooltipsCheck;
    private JCheckBox highlightModifiedCheck;
    private JCheckBox autoBackupCheck;
    private JComboBox<String> localeCombo;
    private JCheckBox enableCustomLabelsCheck;

    // Novos campos
    private JTextField defaultSaveDirField;
    private JCheckBox autoLoadCheck;
    private JComboBox<String> themeCombo;
    private JSpinner fontSizeSpinner;

    private boolean changed = false;

    public PreferencesDialog(JFrame parent) {
        super(parent, "⚙️ Preferências", true);
        this.preferencesManager = PreferencesManager.getInstance();
        this.labelTranslator = LabelTranslator.getInstance();
        initComponents();
        loadCurrentSettings();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 500);
        setLocationRelativeTo(getParent());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tab: Geral
        tabbedPane.addTab("⚙️ Geral", createGeneralPanel());

        // Tab: Editor
        tabbedPane.addTab("✏️ Editor", createEditorPanel());

        // Tab: Traduções
        tabbedPane.addTab("🌍 Traduções", createTranslationPanel());

        // Tab: Avançado
        tabbedPane.addTab("🔧 Avançado", createAdvancedPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton saveButton = new JButton("💾 Salvar");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.addActionListener(e -> save());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JButton resetButton = new JButton("🔄 Restaurar Padrões");
        resetButton.addActionListener(e -> resetToDefaults());

        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Diretórios
        JPanel dirPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        dirPanel.setBorder(BorderFactory.createTitledBorder("📂 Diretórios"));

        JPanel saveDirContainer = new JPanel(new BorderLayout(5, 0));
        saveDirContainer.add(new JLabel("Pasta Padrão de Saves:"), BorderLayout.NORTH);

        defaultSaveDirField = new JTextField();
        defaultSaveDirField.setEditable(false);
        saveDirContainer.add(defaultSaveDirField, BorderLayout.CENTER);

        JButton selectDirButton = new JButton("Selecionar...");
        selectDirButton.addActionListener(e -> selectDefaultSaveDirectory());
        saveDirContainer.add(selectDirButton, BorderLayout.EAST);

        dirPanel.add(saveDirContainer);

        autoLoadCheck = new JCheckBox("Carregar último save/diretório automaticamente ao iniciar");
        dirPanel.add(autoLoadCheck);

        panel.add(dirPanel);
        panel.add(Box.createVerticalStrut(10));

        // Auto-refresh
        JPanel autoRefreshPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        autoRefreshPanel.setBorder(BorderFactory.createTitledBorder("🔄 Auto-Refresh"));

        autoRefreshCheck = new JCheckBox("Detectar mudanças externas automaticamente");
        autoRefreshPanel.add(autoRefreshCheck);

        JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        intervalPanel.add(new JLabel("Intervalo de verificação:"));
        autoRefreshIntervalSpinner = new JSpinner(new SpinnerNumberModel(5000, 1000, 60000, 1000));
        intervalPanel.add(autoRefreshIntervalSpinner);
        intervalPanel.add(new JLabel("ms"));
        autoRefreshPanel.add(intervalPanel);

        panel.add(autoRefreshPanel);
        panel.add(Box.createVerticalStrut(10));

        // Backup
        JPanel backupPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        backupPanel.setBorder(BorderFactory.createTitledBorder("💾 Backup"));

        autoBackupCheck = new JCheckBox("Criar backup automático ao abrir arquivo");
        backupPanel.add(autoBackupCheck);

        panel.add(backupPanel);
        panel.add(Box.createVerticalStrut(10));

        // UI
        JPanel uiPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        uiPanel.setBorder(BorderFactory.createTitledBorder("🎨 Interface"));

        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.add(new JLabel("Tema:"));
        themeCombo = new JComboBox<>(new String[] { "Dark", "Light" });
        themePanel.add(themeCombo);
        uiPanel.add(themePanel);

        JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fontPanel.add(new JLabel("Tamanho da Fonte:"));
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(13, 10, 24, 1));
        fontPanel.add(fontSizeSpinner);
        uiPanel.add(fontPanel);

        showTooltipsCheck = new JCheckBox("Mostrar tooltips/dicas");
        uiPanel.add(showTooltipsCheck);

        panel.add(uiPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel presetPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        presetPanel.setBorder(BorderFactory.createTitledBorder("⭐ Presets"));

        confirmBeforePresetCheck = new JCheckBox("Confirmar antes de aplicar preset");
        presetPanel.add(confirmBeforePresetCheck);

        panel.add(presetPanel);
        panel.add(Box.createVerticalStrut(10));

        JPanel visualPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        visualPanel.setBorder(BorderFactory.createTitledBorder("👁️ Visualização"));

        highlightModifiedCheck = new JCheckBox("Destacar campos modificados");
        visualPanel.add(highlightModifiedCheck);

        panel.add(visualPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createTranslationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        // Locale
        JPanel localePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        localePanel.add(new JLabel("🌍 Idioma:"));

        String[] locales = labelTranslator.getAvailableLocales();
        String[] localeNames = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            localeNames[i] = labelTranslator.getLocaleName(locales[i]);
        }

        localeCombo = new JComboBox<>(localeNames);
        localePanel.add(localeCombo);
        topPanel.add(localePanel);

        // Custom labels
        enableCustomLabelsCheck = new JCheckBox("Habilitar traduções customizadas");
        topPanel.add(enableCustomLabelsCheck);

        panel.add(topPanel, BorderLayout.NORTH);

        // Lista de traduções customizadas
        JPanel customPanel = new JPanel(new BorderLayout(5, 5));
        customPanel.setBorder(BorderFactory.createTitledBorder("✏️ Traduções Customizadas"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        Map<String, String> customTranslations = labelTranslator.getCustomTranslations();
        for (Map.Entry<String, String> entry : customTranslations.entrySet()) {
            listModel.addElement(entry.getKey() + " → " + entry.getValue());
        }

        JList<String> translationList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(translationList);
        customPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("➕ Adicionar");
        addButton.addActionListener(e -> addCustomTranslation(listModel));
        JButton removeButton = new JButton("➖ Remover");
        removeButton.addActionListener(e -> removeCustomTranslation(translationList, listModel));

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        customPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(customPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAdvancedPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextArea infoArea = new JTextArea(
                "⚙️ CONFIGURAÇÕES AVANÇADAS\n\n" +
                        "Diretório de configuração:\n" +
                        System.getProperty("user.home") + "/.brasfoot-editor/\n\n" +
                        "Arquivo de preferências:\n" +
                        "preferences.json\n\n" +
                        "Para resetar completamente as configurações,\n" +
                        "delete o diretório acima e reinicie a aplicação.");
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(panel.getBackground());

        panel.add(infoArea);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void loadCurrentSettings() {
        UserPreferences prefs = preferencesManager.getPreferences();

        // Geral
        String defaultDir = prefs.getFiles().getDefaultSaveDirectory();
        defaultSaveDirField.setText(defaultDir != null ? defaultDir : "");
        autoLoadCheck.setSelected(prefs.getFiles().isAutoLoadLastSave());

        autoRefreshCheck.setSelected(prefs.getEditor().isAutoRefresh());
        autoRefreshIntervalSpinner.setValue(prefs.getEditor().getAutoRefreshInterval());
        autoBackupCheck.setSelected(prefs.getFiles().isAutoBackup());
        showTooltipsCheck.setSelected(prefs.getUi().isShowTooltips());

        // UI
        themeCombo.setSelectedItem(prefs.getUi().getTheme().equalsIgnoreCase("light") ? "Light" : "Dark");
        fontSizeSpinner.setValue(prefs.getUi().getFontSize());

        // Editor
        confirmBeforePresetCheck.setSelected(prefs.getEditor().isConfirmBeforeApplyPreset());
        highlightModifiedCheck.setSelected(prefs.getEditor().isHighlightModifiedFields());

        // Traduções
        String currentLocale = prefs.getEditor().getActiveLocale();
        String localeName = labelTranslator.getLocaleName(currentLocale);
        localeCombo.setSelectedItem(localeName);
        enableCustomLabelsCheck.setSelected(prefs.getEditor().isEnableCustomLabels());
    }

    private void save() {
        UserPreferences prefs = preferencesManager.getPreferences();

        // Geral
        String dir = defaultSaveDirField.getText();
        prefs.getFiles().setDefaultSaveDirectory(dir.isEmpty() ? null : dir);
        prefs.getFiles().setAutoLoadLastSave(autoLoadCheck.isSelected());

        prefs.getEditor().setAutoRefresh(autoRefreshCheck.isSelected());
        prefs.getEditor().setAutoRefreshInterval((Integer) autoRefreshIntervalSpinner.getValue());
        prefs.getFiles().setAutoBackup(autoBackupCheck.isSelected());
        prefs.getUi().setShowTooltips(showTooltipsCheck.isSelected());

        // UI
        prefs.getUi().setTheme(((String) themeCombo.getSelectedItem()).toLowerCase());
        prefs.getUi().setFontSize((Integer) fontSizeSpinner.getValue());

        // Editor
        prefs.getEditor().setConfirmBeforeApplyPreset(confirmBeforePresetCheck.isSelected());
        prefs.getEditor().setHighlightModifiedFields(highlightModifiedCheck.isSelected());

        // Traduções
        String selectedLocaleName = (String) localeCombo.getSelectedItem();
        String[] locales = labelTranslator.getAvailableLocales();
        for (String locale : locales) {
            if (labelTranslator.getLocaleName(locale).equals(selectedLocaleName)) {
                labelTranslator.setLocale(locale);
                break;
            }
        }
        prefs.getEditor().setEnableCustomLabels(enableCustomLabelsCheck.isSelected());

        preferencesManager.save();
        changed = true;

        JOptionPane.showMessageDialog(this,
                "✅ Preferências salvas com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja restaurar todas as configurações\n" +
                        "para os valores padrão?\n\n" +
                        "Esta ação não pode ser desfeita!",
                "Confirmar Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            preferencesManager.resetToDefaults();
            loadCurrentSettings();
            changed = true;
        }
    }

    private void addCustomTranslation(DefaultListModel<String> listModel) {
        String fieldName = JOptionPane.showInputDialog(this,
                "Digite o nome técnico do campo (ex: eq, em, el, ek):",
                "Adicionar Tradução",
                JOptionPane.QUESTION_MESSAGE);

        if (fieldName != null && !fieldName.trim().isEmpty()) {
            fieldName = fieldName.trim();

            String translation = JOptionPane.showInputDialog(this,
                    "Digite a tradução para '" + fieldName + "':",
                    "Tradução",
                    JOptionPane.QUESTION_MESSAGE);

            if (translation != null && !translation.trim().isEmpty()) {
                labelTranslator.setCustomTranslation(fieldName, translation.trim());
                listModel.addElement(fieldName + " → " + translation.trim());
            }
        }
    }

    private void removeCustomTranslation(JList<String> list, DefaultListModel<String> listModel) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selected = listModel.get(selectedIndex);
            String fieldName = selected.split(" → ")[0];

            labelTranslator.removeCustomTranslation(fieldName);
            listModel.remove(selectedIndex);
        }
    }

    private void selectDefaultSaveDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Selecione a Pasta Padrão de Saves");

        if (defaultSaveDirField.getText() != null && !defaultSaveDirField.getText().isEmpty()) {
            chooser.setCurrentDirectory(new java.io.File(defaultSaveDirField.getText()));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            defaultSaveDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public boolean hasChanged() {
        return changed;
    }
}
