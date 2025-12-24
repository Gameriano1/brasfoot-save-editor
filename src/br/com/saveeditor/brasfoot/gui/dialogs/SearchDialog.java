package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.SearchService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Dialog de busca avançada com opções de escopo.
 */
public class SearchDialog extends JDialog {
    
    private final NavegacaoState state;
    private final SearchService searchService;
    
    private JTextField searchField;
    private JTextArea resultsArea;
    private JProgressBar progressBar;
    private JButton searchButton;
    private JRadioButton searchCurrentRadio;
    private JRadioButton searchGlobalRadio;
    
    public SearchDialog(JFrame parent, NavegacaoState state, SearchService searchService) {
        super(parent, "🔍 Busca Avançada", true);
        this.state = state;
        this.searchService = searchService;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(900, 650);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Campo de busca
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Termo de Busca"));
        
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        searchButton = new JButton("🔍 Buscar");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.addActionListener(e -> performSearch());
        
        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 5));
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);
        
        // Opções de escopo
        JPanel scopePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        scopePanel.setBorder(BorderFactory.createTitledBorder("Escopo da Busca"));
        
        searchCurrentRadio = new JRadioButton("📍 Apenas na seleção atual", true);
        searchCurrentRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        searchGlobalRadio = new JRadioButton("🌍 Busca global (todo o save)", false);
        searchGlobalRadio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(searchCurrentRadio);
        scopeGroup.add(searchGlobalRadio);
        
        scopePanel.add(searchCurrentRadio);
        scopePanel.add(searchGlobalRadio);
        
        JLabel hintLabel = new JLabel("💡 A busca não diferencia maiúsculas de minúsculas");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hintLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(searchInputPanel, BorderLayout.NORTH);
        topPanel.add(scopePanel, BorderLayout.CENTER);
        topPanel.add(hintLabel, BorderLayout.SOUTH);
        
        searchPanel.add(topPanel, BorderLayout.CENTER);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Área de resultados
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados"));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Barra de progresso
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setString("Buscando...");
        progressBar.setStringPainted(true);
        
        mainPanel.add(progressBar, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton clearButton = new JButton("🗑️ Limpar");
        clearButton.addActionListener(e -> resultsArea.setText(""));
        
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter para buscar
        searchField.addActionListener(e -> searchButton.doClick());
    }
    
    private void performSearch() {
        String term = searchField.getText().trim();
        
        if (term.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite um termo para buscar!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        resultsArea.setText("");
        searchButton.setEnabled(false);
        progressBar.setVisible(true);
        
        boolean isGlobal = searchGlobalRadio.isSelected();
        
        // Executar busca em background
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                // Capturar output da busca
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                PrintStream old = System.out;
                System.setOut(ps);
                
                try {
                    if (isGlobal) {
                        // Busca global a partir da raiz
                        searchService.iniciarBusca(state.getObjetoRaiz(), "raiz", term, true);
                    } else {
                        // Busca apenas no objeto atual
                        String currentPath = buildCurrentPath();
                        searchService.iniciarBusca(state.getObjetoAtual(), currentPath, term, false);
                    }
                } finally {
                    System.out.flush();
                    System.setOut(old);
                }
                
                return baos.toString();
            }
            
            @Override
            protected void done() {
                try {
                    String results = get();
                    
                    if (results.trim().isEmpty() || 
                        (results.contains("Busca concluída") && results.split("\n").length <= 3)) {
                        resultsArea.setText("❌ Nenhum resultado encontrado para: \"" + term + "\"\n\n" +
                            "Escopo: " + (isGlobal ? "Global (todo o save)" : "Apenas na seleção atual"));
                    } else {
                        // Adicionar cabeçalho
                        String header = "🔍 RESULTADOS DA BUSCA\n" +
                                       "═══════════════════════════════════════════════════════════════\n" +
                                       "Termo: \"" + term + "\"\n" +
                                       "Escopo: " + (isGlobal ? "🌍 Global (todo o save)" : "📍 Seleção atual") + "\n" +
                                       "═══════════════════════════════════════════════════════════════\n\n";
                        
                        resultsArea.setText(header + results);
                        resultsArea.setCaretPosition(0);
                    }
                    
                } catch (Exception e) {
                    resultsArea.setText("❌ Erro durante a busca:\n" + e.getMessage());
                } finally {
                    searchButton.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private String buildCurrentPath() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object obj : state.getTrilhaNavegacao()) {
            if (i++ > 0) sb.append(" → ");
            sb.append(obj.getClass().getSimpleName());
        }
        return sb.toString();
    }
}