package br.com.saveeditor.brasfoot.gui;

import br.com.saveeditor.brasfoot.config.PreferencesManager;
import br.com.saveeditor.brasfoot.gui.components.*;
import br.com.saveeditor.brasfoot.gui.dialogs.*;
import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.model.UserPreferences;
import br.com.saveeditor.brasfoot.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Optional;

/**
 * Janela principal da aplicação com interface moderna e intuitiva.
 */
public class MainWindow extends JFrame {

    private final SaveFileService saveFileService;
    private final EditorService editorService;
    private final SearchService searchService;
    private final PresetService presetService;
    private final PreferencesManager preferencesManager;
    private FileWatcherService fileWatcherService;

    private NavegacaoState currentState;

    // Componentes principais
    private TabManager tabManager;
    private StatusPanel statusPanel;
    private ToolBarPanel toolBarPanel;
    private LogPanel logPanel;

    public MainWindow() {
        this.saveFileService = new SaveFileService();
        this.editorService = new EditorService();
        this.searchService = new SearchService();
        this.presetService = new PresetService();
        this.preferencesManager = PreferencesManager.getInstance();
        this.fileWatcherService = new FileWatcherService();

        initializeUI();
        applyTheme(); // Aplicar tema inicial
        preferencesManager.restoreWindowState(this);
        showWelcomeDialog();
    }

    private void initializeUI() {
        setTitle("🎮 Brasfoot Save Editor v3.0");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Dimension size = preferencesManager.getPreferences().getUi().getWindowSize();
        setSize(size);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onWindowClosing();
            }
        });

        // Ícone da aplicação (se disponível)
        try {
            // TODO: Adicionar ícone
        } catch (Exception e) {
            // Ignorar se não encontrar
        }

        // Layout principal
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));

        // Barra de ferramentas
        toolBarPanel = new ToolBarPanel(this);
        contentPane.add(toolBarPanel, BorderLayout.NORTH);

        // Painel central com abas e log
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setDividerLocation(750);
        mainSplit.setResizeWeight(0.85);
        mainSplit.setDividerSize(4);

        // Gerenciador de abas
        tabManager = new TabManager(this);
        mainSplit.setTopComponent(tabManager);

        // Log
        logPanel = new LogPanel();
        mainSplit.setBottomComponent(logPanel);

        contentPane.add(mainSplit, BorderLayout.CENTER);

        // Barra de status
        statusPanel = new StatusPanel();
        contentPane.add(statusPanel, BorderLayout.SOUTH);

        // Atalhos de teclado
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+O: Abrir
        KeyStroke openKey = KeyStroke.getKeyStroke("control O");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(openKey, "open");
        getRootPane().getActionMap().put("open", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFile();
            }
        });

        // Ctrl+D: Duplicar Aba
        KeyStroke duplicateKey = KeyStroke.getKeyStroke("control D");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(duplicateKey, "duplicate");
        getRootPane().getActionMap().put("duplicate", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                duplicateCurrentTab();
            }
        });

        // Ctrl+S: Salvar
        KeyStroke saveKey = KeyStroke.getKeyStroke("control S");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKey, "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveFile();
            }
        });

        // Ctrl+F: Buscar
        KeyStroke searchKey = KeyStroke.getKeyStroke("control F");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(searchKey, "search");
        getRootPane().getActionMap().put("search", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showSearchDialog();
            }
        });

        // F1: Ajuda
        KeyStroke helpKey = KeyStroke.getKeyStroke("F1");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(helpKey, "help");
        getRootPane().getActionMap().put("help", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showHelpDialog();
            }
        });
    }

    private void showWelcomeDialog() {
        // Verificar auto-load
        if (preferencesManager.isAutoLoadLastSave()) {
            String defaultDir = preferencesManager.getDefaultSaveDirectory();
            java.util.List<String> recent = preferencesManager.getRecentFiles();

            if (!recent.isEmpty()) {
                File lastFile = new File(recent.get(0));
                if (lastFile.exists()) {
                    loadFile(lastFile);
                    return;
                }
            } else if (defaultDir != null) {
                File dir = new File(defaultDir);
                if (dir.exists() && dir.isDirectory()) {
                    openFileWithDir(dir);
                    return;
                }
            }
        }

        WelcomeDialog welcome = new WelcomeDialog(this);
        welcome.setVisible(true);

        if (welcome.shouldOpenFile()) {
            openFile();
        }
    }

    // ===== AÇÕES PÚBLICAS =====

    public void openFile() {
        String startDir = System.getProperty("user.dir");
        String defaultDir = preferencesManager.getDefaultSaveDirectory();

        if (defaultDir != null) {
            File dir = new File(defaultDir);
            if (dir.exists() && dir.isDirectory()) {
                startDir = defaultDir;
            }
        } else {
            // Fallback para último diretório aberto
            String lastDir = preferencesManager.getLastOpenDirectory();
            if (lastDir != null) {
                File dir = new File(lastDir);
                if (dir.exists()) {
                    startDir = lastDir;
                }
            }
        }

        openFileWithDir(new File(startDir));
    }

    private void openFileWithDir(File startDir) {
        JFileChooser fileChooser = new JFileChooser(startDir);
        fileChooser.setDialogTitle("Abrir Save do Brasfoot");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Brasfoot Save Files (*.s22)", "s22"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Salvar diretório como último aberto se não for o default
            if (preferencesManager.getDefaultSaveDirectory() == null) {
                preferencesManager.setLastOpenDirectory(file.getParent());
            }
            loadFile(file);
        }
    }

    public void loadFile(File file) {
        statusPanel.setStatus("⏳ Carregando arquivo...");
        logPanel.log("📂 Carregando: " + file.getName());

        // Usar SwingWorker para não travar a UI
        SwingWorker<Optional<NavegacaoState>, Void> worker = new SwingWorker<Optional<NavegacaoState>, Void>() {
            @Override
            protected Optional<NavegacaoState> doInBackground() {
                return saveFileService.carregarSave(file.getAbsolutePath());
            }

            @Override
            protected void done() {
                try {
                    Optional<NavegacaoState> result = get();
                    if (result.isPresent()) {
                        currentState = result.get();
                        createNewTab(result.get(), file.getAbsolutePath());

                        // Adicionar aos recentes para o auto-load funcionar
                        preferencesManager.addRecentFile(file.getAbsolutePath());

                        statusPanel.setStatus("✅ Arquivo carregado: " + file.getName());
                        logPanel.log("✅ Arquivo carregado com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Não foi possível carregar o arquivo.\nVerifique se está corrompido ou se é um arquivo válido .s22",
                                "Erro ao Carregar",
                                JOptionPane.ERROR_MESSAGE);
                        statusPanel.setStatus("❌ Erro ao carregar arquivo");
                        logPanel.log("❌ Falha ao carregar arquivo");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logPanel.log("❌ Erro: " + e.getMessage());
                    statusPanel.setStatus("❌ Erro");
                }
            }
        };
        worker.execute();
    }

    /**
     * Cria uma nova aba para um arquivo carregado.
     */
    private void createNewTab(NavegacaoState state, String filePath) {
        NavigationPanel navPanel = new NavigationPanel(this);
        DataTablePanel dataPanel = new DataTablePanel(this);

        navPanel.loadState(state);
        dataPanel.loadState(state);

        EditorTab tab = new EditorTab(state, navPanel, dataPanel, filePath);
        tabManager.addTab(tab);

        toolBarPanel.enableActions(true);
        updateTitle();
    }

    /**
     * Duplica a aba atual com navegação independente.
     */
    public void duplicateCurrentTab() {
        EditorTab currentTab = tabManager.getCurrentTab();
        if (currentTab == null) {
            JOptionPane.showMessageDialog(this,
                    "Nenhuma aba para duplicar!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Criar nova aba com o mesmo state raiz mas navegação independente
        String filePath = currentTab.getFilePath();

        // Recarregar o arquivo para ter um state independente
        statusPanel.setStatus("⏳ Duplicando aba...");
        logPanel.log("📋 Duplicando: " + currentTab.getFileName());

        SwingWorker<Optional<NavegacaoState>, Void> worker = new SwingWorker<Optional<NavegacaoState>, Void>() {
            @Override
            protected Optional<NavegacaoState> doInBackground() {
                // Recarregar o mesmo arquivo
                return saveFileService.carregarSave(filePath);
            }

            @Override
            protected void done() {
                try {
                    Optional<NavegacaoState> result = get();
                    if (result.isPresent()) {
                        createNewTab(result.get(), filePath);
                        statusPanel.setStatus("✅ Aba duplicada!");
                        logPanel.log("✅ Aba duplicada com navegação independente!");
                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Erro ao duplicar a aba.",
                                "Erro",
                                JOptionPane.ERROR_MESSAGE);
                        statusPanel.setStatus("❌ Erro ao duplicar");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logPanel.log("❌ Erro ao duplicar: " + e.getMessage());
                    statusPanel.setStatus("❌ Erro");
                }
            }
        };
        worker.execute();
    }

    /**
     * Chamado quando a aba atual muda.
     */
    public void onTabChanged(EditorTab tab) {
        this.currentState = tab.getState();
        updateTitle();
        statusPanel.setStatus("✅ Arquivo: " + tab.getFileName());
    }

    private void updateTitle() {
        int tabCount = tabManager.getTabCount();
        if (tabCount > 0) {
            EditorTab current = tabManager.getCurrentTab();
            if (current != null) {
                setTitle("🎮 Brasfoot Save Editor v3.0 - " + current.getFileName() +
                        " [" + tabCount + " aba(s)]");
            }
        } else {
            setTitle("🎮 Brasfoot Save Editor v3.0");
        }
    }

    public void saveFile() {
        EditorTab currentTab = tabManager.getCurrentTab();
        if (currentTab == null) {
            JOptionPane.showMessageDialog(this,
                    "Nenhum arquivo carregado!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SaveDialog saveDialog = new SaveDialog(this, currentTab.getState());
        saveDialog.setVisible(true);

        if (saveDialog.wasSaved()) {
            currentTab.setModified(false);
            logPanel.log("💾 Arquivo salvo: " + currentTab.getFileName());
            statusPanel.setStatus("✅ Salvo!");
        }
    }

    public void refreshView() {
        EditorTab currentTab = tabManager.getCurrentTab();
        if (currentTab != null) {
            currentTab.refresh();
            logPanel.log("🔄 Visualização atualizada: " + currentTab.getFileName());
        }
    }

    public void showSearchDialog() {
        if (currentState == null) {
            JOptionPane.showMessageDialog(this,
                    "Carregue um arquivo primeiro!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SearchDialog searchDialog = new SearchDialog(this, currentState, searchService);
        searchDialog.setVisible(true);
    }

    public void showHelpDialog() {
        HelpDialog helpDialog = new HelpDialog(this);
        helpDialog.setVisible(true);
    }

    public void showEditPlayerDialog() {
        if (currentState == null)
            return;

        EditPlayerDialog dialog = new EditPlayerDialog(this, currentState, editorService);
        dialog.setVisible(true);

        if (dialog.wasEdited()) {
            refreshView();
        }
    }

    public void showEditTeamDialog() {
        if (currentState == null)
            return;

        EditTeamDialog dialog = new EditTeamDialog(this, currentState, editorService);
        dialog.setVisible(true);

        if (dialog.wasEdited()) {
            refreshView();
        }
    }

    public void showSearchPlayerDialog() {
        EditorTab currentTab = tabManager.getCurrentTab();
        if (currentTab == null) {
            JOptionPane.showMessageDialog(this,
                    "Carregue um arquivo primeiro!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SearchPlayerDialog dialog = new SearchPlayerDialog(this, currentTab.getState(), editorService);
        dialog.setVisible(true);

        if (dialog.wasEdited()) {
            currentTab.setModified(true);
            refreshView();
            logPanel.log("✅ Jogador editado via busca");
        }
    }

    // ===== GETTERS =====

    public NavegacaoState getCurrentState() {
        return currentState;
    }

    public EditorService getEditorService() {
        return editorService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public LogPanel getLogPanel() {
        return logPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public PresetService getPresetService() {
        return presetService;
    }

    // ===== NOVOS MÉTODOS V3.0 =====

    /**
     * Abre o diálogo de gerenciamento de presets.
     */
    public void showPresetManagerDialog() {
        EditorTab currentTab = tabManager.getCurrentTab();
        if (currentTab == null) {
            JOptionPane.showMessageDialog(this,
                    "Carregue um arquivo primeiro!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        PresetManagerDialog dialog = new PresetManagerDialog(this, currentTab.getState(), presetService);
        dialog.setVisible(true);

        if (dialog.wasApplied()) {
            currentTab.setModified(true);
            fileWatcherService.markLocalChanges(true);
            refreshView();
            logPanel.log("⭐ Preset aplicado em: " + currentTab.getFileName());
        }
    }

    /**
     * Abre o diálogo de preferências.
     */
    public void showPreferencesDialog() {
        PreferencesDialog dialog = new PreferencesDialog(this);
        dialog.setVisible(true);

        if (dialog.hasChanged()) {
            applyTheme(); // Reaplicar tema se mudou
            logPanel.log("⚙️ Preferências atualizadas");
        }
    }

    /**
     * Aplica o tema e configurações de UI.
     */
    public void applyTheme() {
        UserPreferences.UISettings ui = preferencesManager.getPreferences().getUi();
        boolean isDark = "dark".equalsIgnoreCase(ui.getTheme());
        int fontSize = ui.getFontSize();

        // Cores
        Color bgColor = isDark ? new Color(30, 30, 30) : new Color(240, 240, 240);
        Color fgColor = isDark ? new Color(220, 220, 220) : new Color(50, 50, 50);

        // Atualizar UIManager (básico)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Nota: Para um suporte completo a temas, seria ideal usar FlatLaf ou similar.
            // Aqui faremos ajustes manuais nos componentes principais.
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Aplicar fonte globalmente (hack simples)
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                Font oldFont = (Font) value;
                UIManager.put(key,
                        new javax.swing.plaf.FontUIResource(oldFont.getName(), oldFont.getStyle(), fontSize));
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Método chamado ao fechar a janela.
     */
    private void onWindowClosing() {
        preferencesManager.saveWindowState(this);

        if (fileWatcherService != null) {
            fileWatcherService.stopWatching();
        }

        logPanel.log("👋 Encerrando aplicação...");
        System.exit(0);
    }
}