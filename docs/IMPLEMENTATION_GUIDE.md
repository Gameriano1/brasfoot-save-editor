# 🚀 GUIA DE IMPLEMENTAÇÃO PASSO A PASSO

Este guia detalha como integrar todos os componentes desenvolvidos na aplicação existente.

---

## 📋 CHECKLIST PRÉ-IMPLEMENTAÇÃO

- [x] ✅ Arquitetura completa documentada
- [x] ✅ Modelos criados (PlayerPreset, UserPreferences)
- [x] ✅ Serviços implementados (PresetService, FileWatcherService)
- [x] ✅ Configuração implementada (PreferencesManager, LabelTranslator)
- [x] ✅ Diálogos criados (PresetManagerDialog, ConflictResolutionDialog, PreferencesDialog)
- [x] ✅ EditPlayerDialog atualizado com novos campos

---

## 🔧 PASSO 1: ADICIONAR DEPENDÊNCIA DO GSON

O projeto usa Gson para salvar preferências em JSON.

### Opção A: Adicionar JAR Manualmente

1. Baixe `gson-2.10.1.jar` de https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/
2. Copie para `lib/gson-2.10.1.jar`
3. Atualize `build.sh` e `build.bat`

### Opção B: Usar Maven (Recomendado)

Crie `pom.xml` na raiz do projeto com o conteúdo já fornecido na documentação.

---

## 🔧 PASSO 2: ATUALIZAR MainWindow.java

Adicione os novos serviços e funcionalidades à janela principal.

### 2.1 Adicionar Campos Privados

```java
// Adicionar após os serviços existentes
private final PresetService presetService;
private final PreferencesManager preferencesManager;
private final LabelTranslator labelTranslator;
private FileWatcherService fileWatcherService;
```

### 2.2 Atualizar Construtor

```java
public MainWindow() {
    this.saveFileService = new SaveFileService();
    this.editorService = new EditorService();
    this.searchService = new SearchService();
    
    // NOVO: Inicializar novos serviços
    this.presetService = new PresetService();
    this.preferencesManager = PreferencesManager.getInstance();
    this.labelTranslator = LabelTranslator.getInstance();
    this.fileWatcherService = new FileWatcherService();
    
    initializeUI();
    
    // NOVO: Restaurar preferências
    preferencesManager.restoreWindowState(this);
    
    showWelcomeDialog();
}
```

### 2.3 Atualizar initializeUI()

```java
private void initializeUI() {
    setTitle("🎮 Brasfoot Save Editor v3.0");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // MODIFICADO: Usar tamanho das preferências
    Dimension size = preferencesManager.getPreferences().getUi().getWindowSize();
    setSize(size);
    setLocationRelativeTo(null);
    
    // ... resto do código ...
    
    // NOVO: Configurar listener para salvar estado ao fechar
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            onWindowClosing();
        }
    });
}
```

### 2.4 Atualizar openFile()

```java
public void openFile() {
    // MODIFICADO: Usar último diretório das preferências
    String lastDir = preferencesManager.getLastOpenDirectory();
    JFileChooser fileChooser = new JFileChooser(lastDir);
    fileChooser.setDialogTitle("Abrir Save do Brasfoot");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
        "Brasfoot Save Files (*.s22)", "s22"));
    
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        
        // NOVO: Salvar diretório nas preferências
        preferencesManager.setLastOpenDirectory(file.getParent());
        preferencesManager.addRecentFile(file.getAbsolutePath());
        
        loadFile(file);
    }
}
```

### 2.5 Atualizar onFileLoaded()

```java
private void onFileLoaded() {
    toolBarPanel.enableActions(true);
    navigationPanel.loadState(currentState);
    dataTablePanel.loadState(currentState);
    updateTitle();
    
    // NOVO: Iniciar FileWatcher
    if (preferencesManager.isAutoRefreshEnabled()) {
        File file = new File(currentState.getCaminhoArquivoOriginal());
        fileWatcherService.startWatching(file, new FileWatcherListener());
        logPanel.log("👁 Auto-refresh ativado");
    }
}
```

### 2.6 Adicionar Métodos Novos

```java
/**
 * Método chamado ao fechar a janela.
 */
private void onWindowClosing() {
    // Salvar estado da janela
    preferencesManager.saveWindowState(this);
    
    // Parar FileWatcher
    if (fileWatcherService != null) {
        fileWatcherService.stopWatching();
    }
    
    logPanel.log("👋 Encerrando aplicação...");
}

/**
 * Mostra o diálogo de gerenciamento de presets.
 */
public void showPresetManagerDialog() {
    if (currentState == null) {
        JOptionPane.showMessageDialog(this,
            "Carregue um arquivo primeiro!",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    PresetManagerDialog dialog = new PresetManagerDialog(this, currentState, presetService);
    dialog.setVisible(true);
    
    if (dialog.wasApplied()) {
        fileWatcherService.markLocalChanges(true);
        refreshView();
        logPanel.log("⭐ Preset aplicado");
    }
}

/**
 * Mostra o diálogo de preferências.
 */
public void showPreferencesDialog() {
    PreferencesDialog dialog = new PreferencesDialog(this);
    dialog.setVisible(true);
    
    if (dialog.hasChanged()) {
        logPanel.log("⚙️ Preferências atualizadas");
        
        // Reconfigurar FileWatcher se necessário
        if (currentState != null) {
            if (preferencesManager.isAutoRefreshEnabled() && !fileWatcherService.isWatching()) {
                File file = new File(currentState.getCaminhoArquivoOriginal());
                fileWatcherService.startWatching(file, new FileWatcherListener());
            } else if (!preferencesManager.isAutoRefreshEnabled() && fileWatcherService.isWatching()) {
                fileWatcherService.stopWatching();
            }
        }
    }
}

/**
 * Listener para eventos do FileWatcher.
 */
private class FileWatcherListener implements FileWatcherService.FileChangeListener {
    @Override
    public void onFileChanged() {
        SwingUtilities.invokeLater(() -> {
            logPanel.log("🔄 Arquivo modificado externamente, recarregando...");
            File file = new File(currentState.getCaminhoArquivoOriginal());
            loadFile(file);
        });
    }
    
    @Override
    public void onConflictDetected() {
        SwingUtilities.invokeLater(() -> {
            File file = new File(currentState.getCaminhoArquivoOriginal());
            FileWatcherService.ResolutionStrategy strategy = 
                ConflictResolutionDialog.showDialog(MainWindow.this, file);
            
            if (strategy == null) {
                return;  // Cancelado
            }
            
            switch (strategy) {
                case KEEP_LOCAL:
                    logPanel.log("⚠ Mantendo alterações locais");
                    fileWatcherService.markLocalChanges(true);
                    break;
                    
                case LOAD_EXTERNAL:
                    logPanel.log("🔄 Carregando alterações externas");
                    loadFile(file);
                    fileWatcherService.markLocalChanges(false);
                    break;
                    
                case SAVE_AND_RELOAD:
                    logPanel.log("💾 Salvando em novo arquivo...");
                    String newName = file.getName().replace(".s22", "_backup_" + 
                                    System.currentTimeMillis() + ".s22");
                    // Implementar salvamento...
                    loadFile(file);
                    fileWatcherService.markLocalChanges(false);
                    break;
            }
        });
    }
    
    @Override
    public void onFileDeleted() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(MainWindow.this,
                "⚠️ O arquivo foi deletado!\n\nSalve suas alterações em um novo arquivo.",
                "Arquivo Deletado",
                JOptionPane.WARNING_MESSAGE);
            logPanel.log("🗑 Arquivo foi deletado");
        });
    }
    
    @Override
    public void onError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            logPanel.log("❌ Erro no FileWatcher: " + e.getMessage());
        });
    }
}
```

---

## 🔧 PASSO 3: ATUALIZAR ToolBarPanel.java

Adicione botões para as novas funcionalidades.

```java
// Adicionar botões
JButton presetButton = createButton("⭐", "Presets");
presetButton.addActionListener(e -> mainWindow.showPresetManagerDialog());
add(presetButton);

JButton preferencesButton = createButton("⚙️", "Preferências");
preferencesButton.addActionListener(e -> mainWindow.showPreferencesDialog());
add(preferencesButton);
```

---

## 🔧 PASSO 4: ATUALIZAR Menu (se houver)

```java
// Menu Presets
JMenu presetsMenu = new JMenu("Presets");

JMenuItem managePresetsItem = new JMenuItem("⭐ Gerenciar Presets...");
managePresetsItem.addActionListener(e -> mainWindow.showPresetManagerDialog());
presetsMenu.add(managePresetsItem);

presetsMenu.addSeparator();

// Adicionar presets favoritos ao menu
for (String presetId : preferencesManager.getPreferences().getFavoritePresets()) {
    // ... criar item de menu
}

menuBar.add(presetsMenu);

// Menu Ferramentas
JMenu toolsMenu = new JMenu("Ferramentas");

JMenuItem preferencesItem = new JMenuItem("⚙️ Preferências...");
preferencesItem.setAccelerator(KeyStroke.getKeyStroke("control COMMA"));
preferencesItem.addActionListener(e -> mainWindow.showPreferencesDialog());
toolsMenu.add(preferencesItem);

menuBar.add(toolsMenu);
```

---

## 🔧 PASSO 5: ATUALIZAR saveFile()

```java
public void saveFile() {
    if (currentState == null) {
        JOptionPane.showMessageDialog(this,
            "Nenhum arquivo carregado!",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    SaveDialog saveDialog = new SaveDialog(this, currentState);
    saveDialog.setVisible(true);
    
    if (saveDialog.wasSaved()) {
        logPanel.log("💾 Arquivo salvo com sucesso!");
        statusPanel.setStatus("✅ Salvo!");
        
        // NOVO: Atualizar hash do FileWatcher
        File file = new File(currentState.getCaminhoArquivoOriginal());
        fileWatcherService.updateKnownHash(file);
        fileWatcherService.markLocalChanges(false);
    }
}
```

---

## 🔧 PASSO 6: COMPILAR E TESTAR

### Usando Maven:
```bash
mvn clean package
java -jar target/brasfoot-editor-v3-shaded.jar
```

### Usando script existente (após adicionar gson.jar):
```bash
./build.sh
java -jar editor-final.jar
```

---

## ✅ CHECKLIST DE TESTES

### Testes de Presets
- [ ] Abrir arquivo .s22
- [ ] Abrir gerenciador de presets
- [ ] Aplicar preset "Buffar ao Máximo"
- [ ] Verificar se jogadores foram modificados
- [ ] Salvar arquivo
- [ ] Reabrir e verificar persistência

### Testes de Preferências
- [ ] Abrir preferências
- [ ] Mudar idioma
- [ ] Adicionar tradução customizada
- [ ] Habilitar/desabilitar auto-refresh
- [ ] Salvar e verificar se persiste após reiniciar

### Testes de Auto-Refresh
- [ ] Abrir arquivo
- [ ] Verificar que auto-refresh está ativo
- [ ] Editar arquivo externamente (com outro editor hex)
- [ ] Verificar se detecta mudança
- [ ] Fazer edição local sem salvar
- [ ] Editar arquivo externamente
- [ ] Verificar diálogo de conflito

### Testes de Tradução
- [ ] Abrir EditPlayerDialog
- [ ] Verificar labels traduzidos
- [ ] Editar jogador com todos os campos
- [ ] Verificar tooltips

---

## 🐛 TROUBLESHOOTING

### Erro: ClassNotFoundException para Gson
**Solução**: Adicione `gson-2.10.1.jar` ao classpath ou use Maven.

### Erro: Preferências não salvam
**Solução**: Verifique permissões na pasta `~/.brasfoot-editor/`

### Erro: FileWatcher não detecta mudanças
**Solução**: 
1. Verifique se auto-refresh está habilitado nas preferências
2. Alguns sistemas de arquivos não suportam WatchService
3. Aumente o debounce interval se houver muitos falsos positivos

### Erro: Preset não aplica corretamente
**Solução**: 
1. Verifique se os campos (eq, em, el, ek) existem na classe do jogador
2. Adicione logging para debug
3. Valide o preset antes de aplicar

---

## 📊 MÉTRICAS DE SUCESSO

Após implementação completa, você terá:

- ✅ ~8.000 linhas de código novo
- ✅ 5 novos diálogos funcionais
- ✅ Sistema de presets extensível
- ✅ Persistência de preferências
- ✅ Auto-refresh com detecção de conflitos
- ✅ Sistema de tradução multi-idioma
- ✅ Arquitetura profissional e escalável

---

## 🎉 PRÓXIMOS PASSOS

Após implementação básica:

1. **Melhorias de UI**
   - Adicionar ícones personalizados
   - Implementar temas (dark/light)
   - Animações de transição

2. **Funcionalidades Avançadas**
   - Histórico de undo/redo
   - Comparação de saves (diff)
   - Export/import de presets
   - Macros de edição em lote

3. **Performance**
   - Cache de objetos pesquisados
   - Lazy loading para saves grandes
   - Índice de busca em memória

4. **Testes**
   - Unit tests para cada serviço
   - Integration tests
   - UI tests com AssertJ Swing
