package br.com.saveeditor.brasfoot.gui.components;

import br.com.saveeditor.brasfoot.gui.MainWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Barra de ferramentas com ações principais.
 */
public class ToolBarPanel extends JPanel {
    
    private final MainWindow mainWindow;
    private JButton saveButton;
    private JButton refreshButton;
    private JButton searchButton;
    private JButton editPlayerButton;
    private JButton editTeamButton;
    private JButton searchPlayerButton;
    private JButton presetButton;  // NOVO V3.0
    private JButton newTabButton;  // NOVO V3.0
    
    public ToolBarPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Painel de ações principais
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionsPanel.setOpaque(false);
        
        JButton openButton = createButton("📂 Abrir", "Abrir arquivo .s22 (Ctrl+O)");
        openButton.addActionListener(e -> mainWindow.openFile());
        
        newTabButton = createButton("➕ Duplicar Aba", "Duplicar aba atual com navegação independente (Ctrl+D)");
        newTabButton.addActionListener(e -> mainWindow.duplicateCurrentTab());
        newTabButton.setEnabled(false);
        
        saveButton = createButton("💾 Salvar", "Salvar alterações (Ctrl+S)");
        saveButton.addActionListener(e -> mainWindow.saveFile());
        saveButton.setEnabled(false);
        
        actionsPanel.add(openButton);
        actionsPanel.add(newTabButton);
        actionsPanel.add(saveButton);
        actionsPanel.add(Box.createHorizontalStrut(5));
        actionsPanel.add(createSeparator());
        actionsPanel.add(Box.createHorizontalStrut(5));
        
        refreshButton = createButton("🔄 Atualizar", "Atualizar visualização");
        refreshButton.addActionListener(e -> mainWindow.refreshView());
        refreshButton.setEnabled(false);
        
        searchButton = createButton("🔍 Buscar", "Buscar dados (Ctrl+F)");
        searchButton.addActionListener(e -> mainWindow.showSearchDialog());
        searchButton.setEnabled(false);
        
        actionsPanel.add(refreshButton);
        actionsPanel.add(searchButton);
        actionsPanel.add(Box.createHorizontalStrut(5));
        actionsPanel.add(createSeparator());
        actionsPanel.add(Box.createHorizontalStrut(5));
        
        // Botões de edição rápida
        searchPlayerButton = createButton("🔍 Buscar Jogador", "Buscar jogador por nome");
        searchPlayerButton.addActionListener(e -> mainWindow.showSearchPlayerDialog());
        searchPlayerButton.setEnabled(false);
        
        editPlayerButton = createButton("⚽ Editar Jogador", "Editar dados de jogador");
        editPlayerButton.addActionListener(e -> mainWindow.showEditPlayerDialog());
        editPlayerButton.setEnabled(false);
        
        editTeamButton = createButton("🏆 Editar Time", "Editar dados de time");
        editTeamButton.addActionListener(e -> mainWindow.showEditTeamDialog());
        editTeamButton.setEnabled(false);
        
        actionsPanel.add(searchPlayerButton);
        actionsPanel.add(editPlayerButton);
        actionsPanel.add(editTeamButton);
        actionsPanel.add(Box.createHorizontalStrut(5));
        actionsPanel.add(createSeparator());
        actionsPanel.add(Box.createHorizontalStrut(5));
        
        // NOVO V3.0: Botões de Presets e Configurações
        presetButton = createButton("⭐ Presets", "Gerenciar e aplicar presets");
        presetButton.addActionListener(e -> mainWindow.showPresetManagerDialog());
        presetButton.setEnabled(false);
        
        JButton preferencesButton = createButton("⚙️ Config", "Preferências da aplicação");
        preferencesButton.addActionListener(e -> mainWindow.showPreferencesDialog());
        
        actionsPanel.add(presetButton);
        actionsPanel.add(preferencesButton);
        actionsPanel.add(Box.createHorizontalStrut(5));
        actionsPanel.add(createSeparator());
        actionsPanel.add(Box.createHorizontalStrut(5));
        
        JButton helpButton = createButton("❓ Ajuda", "Mostrar ajuda (F1)");
        helpButton.addActionListener(e -> mainWindow.showHelpDialog());
        
        actionsPanel.add(helpButton);
        
        add(actionsPanel, BorderLayout.WEST);
        
        // Logo/Título
        JLabel titleLabel = new JLabel("🎮 BRASFOOT SAVE EDITOR V3.0");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(187, 134, 252)); // Primary color
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        add(titleLabel, BorderLayout.CENTER);
    }
    
    private JButton createButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(new Color(230, 230, 230));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(75, 75, 75));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
        });
        
        return button;
    }
    
    private JPanel createSeparator() {
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(1, 30));
        separator.setBackground(new Color(80, 80, 80));
        return separator;
    }
    
    public void enableActions(boolean enabled) {
        saveButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        searchPlayerButton.setEnabled(enabled);
        editPlayerButton.setEnabled(enabled);
        editTeamButton.setEnabled(enabled);
        presetButton.setEnabled(enabled);
        newTabButton.setEnabled(enabled);  // NOVO V3.0
    }
}