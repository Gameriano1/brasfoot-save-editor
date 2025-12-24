package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.EditorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog para edição em lote de time com atributos predefinidos.
 */
public class EditTeamDialog extends JDialog {
    
    private final NavegacaoState state;
    private final EditorService editorService;
    
    private JTextField teamNameField;
    private JComboBox<String> attributeCombo;
    private JTextField customAttributeField;
    private JTextField valueField;
    private JCheckBox useCustomAttribute;
    
    private boolean edited = false;
    
    // Mapa de atributos comuns
    private static final Map<String, String> COMMON_ATTRIBUTES = new HashMap<>();
    static {
        COMMON_ATTRIBUTES.put("eq - Força/Over", "eq");
        COMMON_ATTRIBUTES.put("em - Idade", "em");
        COMMON_ATTRIBUTES.put("en - Habilidade", "en");
        COMMON_ATTRIBUTES.put("eo - Velocidade", "eo");
        COMMON_ATTRIBUTES.put("ep - Resistência", "ep");
        COMMON_ATTRIBUTES.put("er - Chute", "er");
        COMMON_ATTRIBUTES.put("es - Passe", "es");
        COMMON_ATTRIBUTES.put("et - Cabeceio", "et");
        COMMON_ATTRIBUTES.put("eu - Desarme", "eu");
    }
    
    public EditTeamDialog(JFrame parent, NavegacaoState state, EditorService editorService) {
        super(parent, "🏆 Editar Time", true);
        this.state = state;
        this.editorService = editorService;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("🏆 Editar Todos os Jogadores de um Time");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Nome do time
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Nome do Time:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        teamNameField = new JTextField();
        teamNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainPanel.add(teamNameField, gbc);
        
        // Separador
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        
        // Checkbox para usar atributo customizado
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        useCustomAttribute = new JCheckBox("Usar atributo customizado (avançado)");
        useCustomAttribute.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        useCustomAttribute.addActionListener(e -> toggleAttributeMode());
        mainPanel.add(useCustomAttribute, gbc);
        gbc.gridwidth = 1;
        
        // Atributo predefinido
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Atributo:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        String[] attributes = COMMON_ATTRIBUTES.keySet().toArray(new String[0]);
        attributeCombo = new JComboBox<>(attributes);
        attributeCombo.setFont(new Font("Consolas", Font.PLAIN, 12));
        mainPanel.add(attributeCombo, gbc);
        
        // Atributo customizado (inicialmente invisível)
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        JLabel customLabel = new JLabel("Atributo Customizado:");
        customLabel.setVisible(false);
        mainPanel.add(customLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        customAttributeField = new JTextField();
        customAttributeField.setFont(new Font("Consolas", Font.PLAIN, 12));
        customAttributeField.setVisible(false);
        mainPanel.add(customAttributeField, gbc);
        
        // Guardar referências para toggle
        customLabel.setName("customLabel");
        customAttributeField.setName("customField");
        
        // Valor
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Novo Valor:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        valueField = new JTextField();
        valueField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainPanel.add(valueField, gbc);
        
        // Info
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JTextArea infoArea = new JTextArea(
            "💡 Atributos Predefinidos:\n" +
            "• Selecione um atributo comum da lista\n" +
            "• O tipo será validado automaticamente\n\n" +
            "💡 Atributo Customizado (Avançado):\n" +
            "• Digite o nome exato do campo (ex: 'dm' para nome)\n" +
            "• O sistema verificará se o campo existe\n" +
            "• Útil para campos não listados\n\n" +
            "⚠️ Esta operação modifica TODOS os jogadores do time!"
        );
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoArea.setBackground(mainPanel.getBackground());
        infoArea.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
        mainPanel.add(infoArea, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton editButton = new JButton("✅ Editar Time");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editButton.addActionListener(e -> edit());
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void toggleAttributeMode() {
        boolean custom = useCustomAttribute.isSelected();
        
        // Procurar componentes por nome
        for (Component comp : ((JPanel) getContentPane().getComponent(0)).getComponents()) {
            if ("customLabel".equals(comp.getName()) || "customField".equals(comp.getName())) {
                comp.setVisible(custom);
            }
        }
        
        attributeCombo.setEnabled(!custom);
    }
    
    private void edit() {
        String teamName = teamNameField.getText().trim();
        String value = valueField.getText().trim();
        
        if (teamName.isEmpty() || value.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Preencha todos os campos!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Determinar atributo
        String attribute;
        if (useCustomAttribute.isSelected()) {
            attribute = customAttributeField.getText().trim();
            if (attribute.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Digite o nome do atributo customizado!",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            String selected = (String) attributeCombo.getSelectedItem();
            attribute = COMMON_ATTRIBUTES.get(selected);
        }
        
        // Confirmar
        int result = JOptionPane.showConfirmDialog(this,
            "Você tem certeza?\n\n" +
            "Time: " + teamName + "\n" +
            "Atributo: " + attribute + "\n" +
            "Novo Valor: " + value + "\n\n" +
            "Esta ação modificará TODOS os jogadores do time!",
            "Confirmar Edição em Lote",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Executar
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    String arg = String.format("%s; %s; %s", teamName, attribute, value);
                    editorService.editarTime(state.getObjetoRaiz(), arg);
                    return null;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String error = get();
                    
                    if (error == null) {
                        edited = true;
                        JOptionPane.showMessageDialog(EditTeamDialog.this,
                            "Time editado com sucesso!\n\n" +
                            "Todos os jogadores do time '" + teamName + "'\n" +
                            "tiveram o atributo '" + attribute + "' alterado para '" + value + "'.",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(EditTeamDialog.this,
                            "Erro ao editar time:\n" + error,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(EditTeamDialog.this,
                        "Erro inesperado: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    public boolean wasEdited() {
        return edited;
    }
}