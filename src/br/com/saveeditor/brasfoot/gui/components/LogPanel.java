package br.com.saveeditor.brasfoot.gui.components;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Painel de log de atividades.
 */
public class LogPanel extends JPanel {
    
    private JTextArea logArea;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    public LogPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(35, 35, 35));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        
        JLabel titleLabel = new JLabel("📝 LOG DE ATIVIDADES");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(187, 134, 252));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Botão para limpar log no header
        JButton clearButton = new JButton("🗑️ Limpar");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 10));
        clearButton.setFocusPainted(false);
        clearButton.setBackground(new Color(60, 60, 60));
        clearButton.setForeground(new Color(230, 230, 230));
        clearButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clear());
        headerPanel.add(clearButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(25, 25, 25));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.getViewport().setBackground(new Color(25, 25, 25));
        
        add(scrollPane, BorderLayout.CENTER);
        
        log("✅ Brasfoot Save Editor V3.0 iniciado");
    }
    
    public void log(String message) {
        String timestamp = timeFormat.format(new Date());
        logArea.append(String.format("[%s] %s%n", timestamp, message));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    public void clear() {
        logArea.setText("");
        log("Log limpo");
    }
}