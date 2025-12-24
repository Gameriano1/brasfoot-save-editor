package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.EditorService;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Dialog para buscar jogador por nome e ver informações antes de editar.
 */
public class SearchPlayerDialog extends JDialog {
    
    // Helper para Java 8 (String.repeat foi adicionado no Java 11)
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    private final NavegacaoState state;
    private final EditorService editorService;
    
    private JTextField searchField;
    private JTextArea resultArea;
    private JButton editButton;
    
    private Object foundPlayer = null;
    private boolean edited = false;
    
    public SearchPlayerDialog(JFrame parent, NavegacaoState state, EditorService editorService) {
        super(parent, "⚽ Buscar Jogador", true);
        this.state = state;
        this.editorService = editorService;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 450);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Painel de busca
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍 Buscar Jogador"));
        
        JLabel instructionLabel = new JLabel("Digite o nome do jogador:");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton searchButton = new JButton("🔍 Buscar");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchPlayer());
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(searchField, BorderLayout.CENTER);
        inputPanel.add(searchButton, BorderLayout.EAST);
        
        searchPanel.add(instructionLabel, BorderLayout.NORTH);
        searchPanel.add(inputPanel, BorderLayout.CENTER);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Área de resultados
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📊 Informações do Jogador"));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        editButton = new JButton("✏️ Editar Jogador");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editPlayer());
        
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter para buscar
        searchField.addActionListener(e -> searchButton.doClick());
        
        SwingUtilities.invokeLater(() -> searchField.requestFocus());
    }
    
    private void searchPlayer() {
        String playerName = searchField.getText().trim();
        
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite o nome do jogador!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        resultArea.setText("🔍 Procurando por '" + playerName + "'...\n");
        editButton.setEnabled(false);
        foundPlayer = null;
        
        // Buscar em background
        SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() {
                return findPlayerRecursive(state.getObjetoRaiz(), playerName, new HashSet<>());
            }
            
            @Override
            protected void done() {
                try {
                    Object player = get();
                    
                    if (player != null) {
                        foundPlayer = player;
                        displayPlayerInfo(player, playerName);
                        editButton.setEnabled(true);
                    } else {
                        resultArea.setText("❌ Jogador '" + playerName + "' não encontrado.\n\n" +
                            "💡 Dicas:\n" +
                            "• Verifique se digitou o nome corretamente\n" +
                            "• O nome deve ser exatamente como aparece no jogo\n" +
                            "• A busca diferencia maiúsculas de minúsculas");
                        editButton.setEnabled(false);
                    }
                } catch (Exception e) {
                    resultArea.setText("❌ Erro ao buscar: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private Object findPlayerRecursive(Object obj, String targetName, Set<Object> visited) {
        if (obj == null || !ReflectionUtils.isComplexObject(obj) || visited.contains(obj)) {
            return null;
        }
        visited.add(obj);
        
        // Verificar se é um jogador (classe best.F)
        if ("best.F".equals(obj.getClass().getName())) {
            try {
                String playerName = (String) ReflectionUtils.getFieldValue(obj, "dm");
                if (targetName.equals(playerName)) {
                    return obj;
                }
            } catch (Exception e) {
                // Ignorar
            }
        }
        
        // Buscar recursivamente
        if (obj instanceof Collection) {
            for (Object item : (Collection<?>) obj) {
                Object found = findPlayerRecursive(item, targetName, visited);
                if (found != null) return found;
            }
        } else if (obj.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(obj); i++) {
                Object found = findPlayerRecursive(Array.get(obj, i), targetName, visited);
                if (found != null) return found;
            }
        } else {
            for (Field field : obj.getClass().getDeclaredFields()) {
                try {
                    Object fieldValue = ReflectionUtils.getFieldValue(obj, field.getName());
                    Object found = findPlayerRecursive(fieldValue, targetName, visited);
                    if (found != null) return found;
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }
        
        return null;
    }
    
    private void displayPlayerInfo(Object player, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ Jogador Encontrado!\n");
        sb.append(repeat("═", 50)).append("\n\n");
        
        try {
            // Nome
            sb.append("👤 Nome: ").append(name).append("\n\n");
            
            // Idade
            int age = (int) ReflectionUtils.getFieldValue(player, "em");
            sb.append("📅 Idade: ").append(age).append(" anos\n");
            
            // Força/Over
            int over = (int) ReflectionUtils.getFieldValue(player, "eq");
            sb.append("⚡ Força (Over): ").append(over).append("\n\n");
            
            // Outros atributos (se existirem)
            try {
                int habilidade = (int) ReflectionUtils.getFieldValue(player, "en");
                sb.append("🎯 Habilidade: ").append(habilidade).append("\n");
            } catch (Exception e) { /* Ignorar */ }
            
            try {
                int velocidade = (int) ReflectionUtils.getFieldValue(player, "eo");
                sb.append("🏃 Velocidade: ").append(velocidade).append("\n");
            } catch (Exception e) { /* Ignorar */ }
            
            try {
                int resistencia = (int) ReflectionUtils.getFieldValue(player, "ep");
                sb.append("💪 Resistência: ").append(resistencia).append("\n");
            } catch (Exception e) { /* Ignorar */ }
            
            sb.append("\n").append(repeat("═", 50)).append("\n");
            sb.append("💡 Clique em 'Editar Jogador' para modificar idade e força");
            
        } catch (Exception e) {
            sb.append("\n❌ Erro ao ler informações: ").append(e.getMessage());
        }
        
        resultArea.setText(sb.toString());
    }
    
    private void editPlayer() {
        if (foundPlayer == null) return;
        
        try {
            // Obter valores atuais
            String playerName = (String) ReflectionUtils.getFieldValue(foundPlayer, "dm");
            int currentAge = (int) ReflectionUtils.getFieldValue(foundPlayer, "em");
            int currentOver = (int) ReflectionUtils.getFieldValue(foundPlayer, "eq");
            
            // Dialog de edição
            JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            panel.add(new JLabel("Jogador:"));
            JLabel nameLabel = new JLabel(playerName);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            panel.add(nameLabel);
            
            panel.add(new JLabel("Nova Idade:"));
            JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(currentAge, 15, 45, 1));
            panel.add(ageSpinner);
            
            panel.add(new JLabel("Nova Força (Over):"));
            JSpinner overSpinner = new JSpinner(new SpinnerNumberModel(currentOver, 1, 99, 1));
            panel.add(overSpinner);
            
            int result = JOptionPane.showConfirmDialog(this, panel,
                "Editar " + playerName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                int newAge = (int) ageSpinner.getValue();
                int newOver = (int) overSpinner.getValue();
                
                // Aplicar mudanças
                ReflectionUtils.setFieldValue(foundPlayer, "em", newAge);
                ReflectionUtils.setFieldValue(foundPlayer, "eq", newOver);
                
                edited = true;
                
                JOptionPane.showMessageDialog(this,
                    "✅ Jogador editado com sucesso!\n\n" +
                    "Jogador: " + playerName + "\n" +
                    "Idade: " + currentAge + " → " + newAge + "\n" +
                    "Força: " + currentOver + " → " + newOver,
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Atualizar display
                displayPlayerInfo(foundPlayer, playerName);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao editar jogador: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean wasEdited() {
        return edited;
    }
}