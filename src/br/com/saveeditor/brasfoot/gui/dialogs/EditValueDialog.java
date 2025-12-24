package br.com.saveeditor.brasfoot.gui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog para editar um valor de campo com suporte melhorado para boolean.
 */
public class EditValueDialog extends JDialog {
    
    private JTextField valueField;
    private JComboBox<String> booleanCombo;
    private boolean confirmed = false;
    private String newValue;
    private final boolean isBoolean;
    
    public EditValueDialog(JFrame parent, String fieldName, String type, String currentValue) {
        super(parent, "Editar " + fieldName, true);
        this.isBoolean = type.equals("boolean") || type.equals("Boolean");
        initComponents(fieldName, type, currentValue);
    }
    
    private void initComponents(String fieldName, String type, String currentValue) {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 250);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Campo:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel fieldLabel = new JLabel(fieldName);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mainPanel.add(fieldLabel, gbc);
        
        // Tipo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Tipo:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        mainPanel.add(typeLabel, gbc);
        
        // Valor atual
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Valor Atual:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel currentLabel = new JLabel(currentValue);
        currentLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        currentLabel.setForeground(Color.GRAY);
        mainPanel.add(currentLabel, gbc);
        
        // Novo valor (TextField ou ComboBox dependendo do tipo)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Novo Valor:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        
        if (isBoolean) {
            // ComboBox para boolean
            String[] options = {"true", "false"};
            booleanCombo = new JComboBox<>(options);
            booleanCombo.setFont(new Font("Consolas", Font.PLAIN, 13));
            
            // Selecionar valor atual
            if ("true".equalsIgnoreCase(currentValue)) {
                booleanCombo.setSelectedIndex(0);
            } else {
                booleanCombo.setSelectedIndex(1);
            }
            
            mainPanel.add(booleanCombo, gbc);
        } else {
            // TextField para outros tipos
            valueField = new JTextField(currentValue);
            valueField.setFont(new Font("Consolas", Font.PLAIN, 13));
            valueField.selectAll();
            mainPanel.add(valueField, gbc);
        }
        
        // Dica de tipo
        gbc.gridx = 1;
        gbc.gridy = 4;
        JLabel hintLabel = new JLabel(getHintForType(type));
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hintLabel.setForeground(Color.LIGHT_GRAY);
        mainPanel.add(hintLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton confirmButton = new JButton("✅ Confirmar");
        confirmButton.addActionListener(e -> {
            if (isBoolean) {
                newValue = (String) booleanCombo.getSelectedItem();
            } else {
                newValue = valueField.getText();
            }
            confirmed = true;
            dispose();
        });
        
        JButton cancelButton = new JButton("❌ Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter para confirmar (se não for boolean)
        if (!isBoolean && valueField != null) {
            valueField.addActionListener(e -> confirmButton.doClick());
            SwingUtilities.invokeLater(() -> valueField.requestFocus());
        } else {
            SwingUtilities.invokeLater(() -> confirmButton.requestFocus());
        }
    }
    
    private String getHintForType(String type) {
        switch (type) {
            case "int":
            case "Integer":
            case "long":
            case "Long":
                return "💡 Digite um número inteiro (ex: 25)";
            case "double":
            case "Double":
            case "float":
            case "Float":
                return "💡 Digite um número decimal (ex: 99.5)";
            case "boolean":
            case "Boolean":
                return "💡 Selecione true (verdadeiro) ou false (falso)";
            case "String":
                return "💡 Digite qualquer texto";
            default:
                return "";
        }
    }
    
    public boolean wasConfirmed() {
        return confirmed;
    }
    
    public String getNewValue() {
        return newValue;
    }
}