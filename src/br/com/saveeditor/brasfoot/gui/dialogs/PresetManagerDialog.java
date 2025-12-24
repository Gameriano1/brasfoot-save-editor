package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.config.PreferencesManager;
import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.model.PlayerPreset;
import br.com.saveeditor.brasfoot.service.PresetService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Diálogo para gerenciar e aplicar presets de jogadores.
 */
public class PresetManagerDialog extends JDialog {
    
    private final NavegacaoState state;
    private final PresetService presetService;
    private final PreferencesManager preferencesManager;
    
    private JList<PlayerPreset> presetList;
    private DefaultListModel<PlayerPreset> listModel;
    private JTextArea descriptionArea;
    private JButton applyButton;
    private JButton applyAllButton;
    private JButton favoriteButton;
    
    private boolean applied = false;
    
    public PresetManagerDialog(JFrame parent, NavegacaoState state, PresetService presetService) {
        super(parent, "⭐ Gerenciador de Presets", true);
        this.state = state;
        this.presetService = presetService;
        this.preferencesManager = PreferencesManager.getInstance();
        initComponents();
        loadPresets();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel titleLabel = new JLabel("⭐ Selecione um Preset para Aplicar");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Painel central: lista + descrição
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // Lista de presets
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setBorder(BorderFactory.createTitledBorder("Presets Disponíveis"));
        
        listModel = new DefaultListModel<>();
        presetList = new JList<>(listModel);
        presetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        presetList.setCellRenderer(new PresetCellRenderer());
        presetList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDescription();
            }
        });
        
        JScrollPane listScroll = new JScrollPane(presetList);
        listPanel.add(listScroll, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(listPanel);
        
        // Painel de descrição
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBorder(BorderFactory.createTitledBorder("Detalhes do Preset"));
        
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descPanel.add(descScroll, BorderLayout.CENTER);
        
        splitPane.setRightComponent(descPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton createButton = new JButton("✨ Criar Preset");
        createButton.setToolTipText("Criar preset personalizado");
        createButton.addActionListener(e -> createCustomPreset());
        
        favoriteButton = new JButton("⭐ Favorito");
        favoriteButton.setToolTipText("Marcar/desmarcar como favorito");
        favoriteButton.addActionListener(e -> toggleFavorite());
        
        applyButton = new JButton("✅ Aplicar a Time Específico");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applyButton.setToolTipText("Aplica a todos os jogadores de um time específico");
        applyButton.addActionListener(e -> applyToTeam());
        
        applyAllButton = new JButton("🌍 Aplicar a TODOS");
        applyAllButton.setToolTipText("Aplica a todos os jogadores do save");
        applyAllButton.addActionListener(e -> applyToAll());
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(favoriteButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(applyAllButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadPresets() {
        listModel.clear();
        
        List<PlayerPreset> allPresets = presetService.getAllPresets();
        
        // Adicionar favoritos primeiro
        for (PlayerPreset preset : allPresets) {
            if (preferencesManager.isFavoritePreset(preset.getId())) {
                listModel.addElement(preset);
            }
        }
        
        // Depois built-in
        for (PlayerPreset preset : presetService.getBuiltInPresets()) {
            if (!preferencesManager.isFavoritePreset(preset.getId())) {
                listModel.addElement(preset);
            }
        }
        
        // Por último custom
        for (PlayerPreset preset : presetService.getCustomPresets()) {
            if (!preferencesManager.isFavoritePreset(preset.getId())) {
                listModel.addElement(preset);
            }
        }
        
        if (listModel.getSize() > 0) {
            presetList.setSelectedIndex(0);
        }
    }
    
    private void updateDescription() {
        PlayerPreset selected = presetList.getSelectedValue();
        
        if (selected == null) {
            descriptionArea.setText("");
            applyButton.setEnabled(false);
            applyAllButton.setEnabled(false);
            favoriteButton.setEnabled(false);
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(selected.getIcon()).append(" ").append(selected.getName()).append("\n\n");
        sb.append("📝 Descrição:\n");
        sb.append(selected.getDescription()).append("\n\n");
        sb.append("⚙️ Atributos que serão modificados:\n");
        
        for (Map.Entry<String, Object> entry : selected.getAttributes().entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            
            String fieldLabel = getFieldLabel(fieldName);
            sb.append("  • ").append(fieldLabel).append(": ").append(formatValue(value)).append("\n");
        }
        
        sb.append("\n");
        
        if (preferencesManager.isFavoritePreset(selected.getId())) {
            sb.append("⭐ Este preset está nos seus favoritos\n");
        }
        
        if (selected.getType() == PlayerPreset.PresetType.BUILT_IN) {
            sb.append("🔒 Preset built-in (não pode ser editado)");
        } else {
            sb.append("✏️ Preset customizado");
        }
        
        descriptionArea.setText(sb.toString());
        descriptionArea.setCaretPosition(0);
        
        applyButton.setEnabled(true);
        applyAllButton.setEnabled(true);
        favoriteButton.setEnabled(true);
        
        // Atualizar texto do botão de favorito
        if (preferencesManager.isFavoritePreset(selected.getId())) {
            favoriteButton.setText("⭐ Remover Favorito");
        } else {
            favoriteButton.setText("⭐ Adicionar Favorito");
        }
    }
    
    private String getFieldLabel(String fieldName) {
        switch (fieldName) {
            case "eq": return "⚡ Força";
            case "em": return "📅 Idade";
            case "el": return "⭐ Estrela Local";
            case "ek": return "🌟 Estrela Mundial";
            default: return fieldName;
        }
    }
    
    private String formatValue(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "Sim ✓" : "Não ✗";
        }
        return value.toString();
    }
    
    private void toggleFavorite() {
        PlayerPreset selected = presetList.getSelectedValue();
        if (selected == null) return;
        
        if (preferencesManager.isFavoritePreset(selected.getId())) {
            preferencesManager.removeFavoritePreset(selected.getId());
        } else {
            preferencesManager.addFavoritePreset(selected.getId());
        }
        
        loadPresets();
        presetList.setSelectedValue(selected, true);
    }
    
    private void applyToTeam() {
        PlayerPreset selected = presetList.getSelectedValue();
        if (selected == null) return;
        
        // Pedir nome do time
        String teamName = JOptionPane.showInputDialog(this,
            "Digite o nome do time para aplicar o preset '" + selected.getName() + "':\n\n" +
            "Exemplo: Flamengo, Palmeiras, Corinthians, etc.",
            "Nome do Time",
            JOptionPane.QUESTION_MESSAGE);
        
        if (teamName == null || teamName.trim().isEmpty()) {
            return;
        }
        
        final String finalTeamName = teamName.trim();
        
        // Confirmar
        int result = JOptionPane.showConfirmDialog(this,
            "Tem certeza que deseja aplicar o preset '" + selected.getName() + 
            "' a TODOS os jogadores do time '" + finalTeamName + "'?\n\n" +
            "Esta ação modificará TODOS os jogadores deste time!\n\n" +
            "Esta ação não pode ser desfeita sem restaurar o backup!",
            "Confirmar Aplicação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Aplicar ao time específico
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return presetService.applyPresetToTeamByName(selected, state.getObjetoRaiz(), finalTeamName);
            }
            
            @Override
            protected void done() {
                try {
                    Integer count = get();
                    
                    if (count == 0) {
                        JOptionPane.showMessageDialog(PresetManagerDialog.this,
                            "⚠️ Nenhum jogador encontrado!\n\n" +
                            "Time '" + finalTeamName + "' não foi encontrado ou não possui jogadores.\n\n" +
                            "Verifique se o nome está correto e tente novamente.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        applied = true;
                        
                        JOptionPane.showMessageDialog(PresetManagerDialog.this,
                            "✅ Preset aplicado com sucesso!\n\n" +
                            "Time: " + finalTeamName + "\n" +
                            "Total de jogadores modificados: " + count + "\n\n" +
                            "Lembre-se de salvar o arquivo para manter as alterações!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        dispose();
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PresetManagerDialog.this,
                        "❌ Erro ao aplicar preset:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void createCustomPreset() {
        CreatePresetDialog dialog = new CreatePresetDialog((JFrame) getOwner());
        dialog.setVisible(true);
        
        PlayerPreset newPreset = dialog.getCreatedPreset();
        if (newPreset != null) {
            // Adicionar ao PresetService
            presetService.addCustomPreset(newPreset);
            
            // Salvar nas preferências
            preferencesManager.saveCustomPreset(newPreset);
            
            // Recarregar lista
            loadPresets();
            
            // Selecionar o novo preset
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getId().equals(newPreset.getId())) {
                    presetList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void applyToAll() {
        PlayerPreset selected = presetList.getSelectedValue();
        if (selected == null) return;
        
        // Confirmar
        int result = JOptionPane.showConfirmDialog(this,
            "⚠️ ATENÇÃO! ⚠️\n\n" +
            "Você está prestes a aplicar o preset '" + selected.getName() + 
            "' a TODOS OS JOGADORES do save!\n\n" +
            "Esta é uma operação em massa que modificará centenas/milhares de jogadores.\n\n" +
            "Certifique-se de ter um backup antes de continuar!\n\n" +
            "Deseja continuar?",
            "⚠️ Confirmação Crítica",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Aplicar em background
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return presetService.applyPresetToAllPlayers(selected, state.getObjetoRaiz());
            }
            
            @Override
            protected void done() {
                try {
                    Integer count = get();
                    applied = true;
                    
                    JOptionPane.showMessageDialog(PresetManagerDialog.this,
                        "✅ Preset aplicado com sucesso!\n\n" +
                        "Total de jogadores modificados: " + count + "\n\n" +
                        "Lembre-se de salvar o arquivo para manter as alterações!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    dispose();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PresetManagerDialog.this,
                        "❌ Erro ao aplicar preset:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean wasApplied() {
        return applied;
    }
    
    /**
     * Renderer customizado para a lista de presets.
     */
    private class PresetCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof PlayerPreset) {
                PlayerPreset preset = (PlayerPreset) value;
                
                String text = preset.getIcon() + " " + preset.getName();
                
                // Adicionar estrela se for favorito
                if (preferencesManager.isFavoritePreset(preset.getId())) {
                    text = "⭐ " + text;
                }
                
                label.setText(text);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
            }
            
            return label;
        }
    }
}
