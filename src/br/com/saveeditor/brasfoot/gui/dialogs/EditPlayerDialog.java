package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.config.LabelTranslator;
import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.EditorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog para edição rápida de jogador.
 */
public class EditPlayerDialog extends JDialog {
    
    private final NavegacaoState state;
    private final EditorService editorService;
    private final LabelTranslator labelTranslator;
    
    private JTextField nameField;
    private JSpinner ageSpinner;
    private JSpinner strengthSpinner;
    private JCheckBox localStarCheck;
    private JCheckBox worldStarCheck;
    
    private boolean edited = false;
    
    public EditPlayerDialog(JFrame parent, NavegacaoState state, EditorService editorService) {
        super(parent, "⚽ Editar Jogador", true);
        this.state = state;
        this.editorService = editorService;
        this.labelTranslator = LabelTranslator.getInstance();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 450);
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
        JLabel titleLabel = new JLabel("⚽ Editar Jogador");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Nome
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainPanel.add(new JLabel(labelTranslator.getLabelWithIcon("dm") + ":"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setToolTipText(labelTranslator.getDescription("dm"));
        mainPanel.add(nameField, gbc);
        
        // Idade
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        mainPanel.add(new JLabel(labelTranslator.getLabelWithIcon("em") + ":"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        ageSpinner = new JSpinner(new SpinnerNumberModel(25, 15, 45, 1));
        ageSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ageSpinner.setToolTipText(labelTranslator.getDescription("em"));
        mainPanel.add(ageSpinner, gbc);
        
        // Força
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        mainPanel.add(new JLabel(labelTranslator.getLabelWithIcon("eq") + ":"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        strengthSpinner = new JSpinner(new SpinnerNumberModel(80, 1, 99, 1));
        strengthSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        strengthSpinner.setToolTipText(labelTranslator.getDescription("eq"));
        mainPanel.add(strengthSpinner, gbc);
        
        // Estrela Local
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        localStarCheck = new JCheckBox(labelTranslator.getLabelWithIcon("el"));
        localStarCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        localStarCheck.setToolTipText(labelTranslator.getDescription("el"));
        mainPanel.add(localStarCheck, gbc);
        
        // Estrela Mundial
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        worldStarCheck = new JCheckBox(labelTranslator.getLabelWithIcon("ek"));
        worldStarCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        worldStarCheck.setToolTipText(labelTranslator.getDescription("ek"));
        mainPanel.add(worldStarCheck, gbc);
        
        // Info
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JTextArea infoArea = new JTextArea(
            "💡 Digite o nome exato do jogador.\n" +
            "A busca encontrará o jogador e modificará os atributos:\n" +
            "• eq (força), em (idade), el (estrela local), ek (estrela mundial)"
        );
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoArea.setBackground(mainPanel.getBackground());
        infoArea.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        mainPanel.add(infoArea, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton editButton = new JButton("✅ Editar");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editButton.addActionListener(e -> edit());
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void edit() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite o nome do jogador!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int age = (Integer) ageSpinner.getValue();
        int strength = (Integer) strengthSpinner.getValue();
        boolean localStar = localStarCheck.isSelected();
        boolean worldStar = worldStarCheck.isSelected();
        
        // Executar em background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            boolean found = false;
            
            @Override
            protected Void doInBackground() {
                try {
                    // Buscar e modificar jogador
                    found = editarJogadorCompleto(name, age, strength, localStar, worldStar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (found) {
                    edited = true;
                    JOptionPane.showMessageDialog(EditPlayerDialog.this,
                        "✅ Jogador editado com sucesso!\n\n" +
                        "Jogador: " + name + "\n" +
                        "Idade: " + age + "\n" +
                        "Força: " + strength + "\n" +
                        "Estrela Local: " + (localStar ? "Sim" : "Não") + "\n" +
                        "Estrela Mundial: " + (worldStar ? "Sim" : "Não"),
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(EditPlayerDialog.this,
                        "❌ Jogador '" + name + "' não encontrado.",
                        "Não Encontrado",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Edita um jogador com todos os campos.
     */
    private boolean editarJogadorCompleto(String nomeJogador, int idade, int forca, 
                                          boolean estrelaLocal, boolean estrelaMundial) {
        try {
            Object raiz = state.getObjetoRaiz();
            return buscarEEditarJogador(raiz, nomeJogador, idade, forca, 
                                       estrelaLocal, estrelaMundial, new java.util.HashSet<>());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean buscarEEditarJogador(Object currentObject, String nomeAlvo, 
                                         int idade, int forca, boolean estrelaLocal, 
                                         boolean estrelaMundial, java.util.Set<Object> visited) {
        if (currentObject == null || visited.contains(currentObject)) {
            return false;
        }
        
        visited.add(currentObject);
        
        // Verificar se é um jogador (classe F)
        if ("best.F".equals(currentObject.getClass().getName())) {
            try {
                java.lang.reflect.Field dmField = currentObject.getClass().getDeclaredField("dm");
                dmField.setAccessible(true);
                String nome = (String) dmField.get(currentObject);
                
                if (nomeAlvo.equals(nome)) {
                    // Encontrou! Modificar
                    setField(currentObject, "em", idade);
                    setField(currentObject, "eq", forca);
                    setField(currentObject, "el", estrelaLocal);
                    setField(currentObject, "ek", estrelaMundial);
                    
                    System.out.println("✅ Jogador '" + nome + "' editado com sucesso!");
                    return true;
                }
            } catch (Exception e) {
                // Ignorar
            }
        }
        
        // Busca recursiva
        if (currentObject instanceof java.util.Collection) {
            for (Object item : (java.util.Collection<?>) currentObject) {
                if (buscarEEditarJogador(item, nomeAlvo, idade, forca, 
                                        estrelaLocal, estrelaMundial, visited)) {
                    return true;
                }
            }
        } else if (currentObject.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(currentObject);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(currentObject, i);
                if (buscarEEditarJogador(item, nomeAlvo, idade, forca, 
                                        estrelaLocal, estrelaMundial, visited)) {
                    return true;
                }
            }
        } else {
            try {
                for (java.lang.reflect.Field field : currentObject.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(currentObject);
                    if (buscarEEditarJogador(fieldValue, nomeAlvo, idade, forca, 
                                            estrelaLocal, estrelaMundial, visited)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // Ignorar
            }
        }
        
        return false;
    }
    
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    public boolean wasEdited() {
        return edited;
    }
}