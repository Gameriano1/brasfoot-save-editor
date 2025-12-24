package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.model.PlayerPreset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Diálogo para criar presets personalizados.
 */
public class CreatePresetDialog extends JDialog {
    
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner strengthSpinner;
    private JSpinner ageSpinner;
    private JCheckBox localStarCheck;
    private JCheckBox worldStarCheck;
    
    private PlayerPreset createdPreset = null;
    
    public CreatePresetDialog(JFrame parent) {
        super(parent, "✨ Criar Preset Personalizado", true);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 450);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel titleLabel = new JLabel("✨ Criar Novo Preset");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nome do preset
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("📝 Nome:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        nameField = new JTextField(20);
        nameField.setToolTipText("Nome do preset (ex: Meu Time Favorito)");
        formPanel.add(nameField, gbc);
        
        // Descrição
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("📄 Descrição:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setToolTipText("Descreva o que este preset faz");
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        formPanel.add(descScroll, gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Força
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("⚡ Força (eq):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        strengthSpinner = new JSpinner(new SpinnerNumberModel(75, 1, 99, 1));
        strengthSpinner.setToolTipText("Força/Overall do jogador (1-99)");
        formPanel.add(strengthSpinner, gbc);
        
        // Idade
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("📅 Idade (em):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        ageSpinner = new JSpinner(new SpinnerNumberModel(25, 16, 45, 1));
        ageSpinner.setToolTipText("Idade do jogador (16-45)");
        formPanel.add(ageSpinner, gbc);
        
        // Estrela Local
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formPanel.add(new JLabel("⭐ Estrela Local (el):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        localStarCheck = new JCheckBox("Marcar como estrela local");
        formPanel.add(localStarCheck, gbc);
        
        // Estrela Mundial
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        formPanel.add(new JLabel("🌟 Estrela Mundial (ek):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        worldStarCheck = new JCheckBox("Marcar como estrela mundial");
        formPanel.add(worldStarCheck, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton createButton = new JButton("✅ Criar Preset");
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createButton.addActionListener(e -> createPreset());
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter para criar
        getRootPane().setDefaultButton(createButton);
    }
    
    private void createPreset() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, informe um nome para o preset!",
                "Nome Obrigatório",
                JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        if (description.isEmpty()) {
            description = "Preset personalizado criado pelo usuário";
        }
        
        // Criar preset
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("eq", (Integer) strengthSpinner.getValue());
        attributes.put("em", (Integer) ageSpinner.getValue());
        attributes.put("el", localStarCheck.isSelected());
        attributes.put("ek", worldStarCheck.isSelected());
        
        String id = "custom_" + System.currentTimeMillis();
        
        createdPreset = new PlayerPreset(
            id,
            name,
            description,
            "🎨",
            PlayerPreset.PresetType.CUSTOM
        );
        
        // Adicionar atributos
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                createdPreset.addAttribute(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                createdPreset.addBooleanAttribute(entry.getKey(), (Boolean) entry.getValue());
            }
        }
        
        JOptionPane.showMessageDialog(this,
            "✅ Preset '" + name + "' criado com sucesso!",
            "Sucesso",
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    public PlayerPreset getCreatedPreset() {
        return createdPreset;
    }
}
