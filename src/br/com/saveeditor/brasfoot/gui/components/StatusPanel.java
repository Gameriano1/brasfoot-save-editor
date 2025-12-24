package br.com.saveeditor.brasfoot.gui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Barra de status na parte inferior.
 */
public class StatusPanel extends JPanel {
    
    private JLabel statusLabel;
    
    public StatusPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(35, 35, 35));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        statusLabel = new JLabel("✅ Pronto");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(76, 175, 80)); // Success color
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        add(statusLabel, BorderLayout.WEST);
        
        // Painel direito com informações
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JLabel versionLabel = new JLabel("🚀 v3.0");
        versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        versionLabel.setForeground(new Color(187, 134, 252));
        
        rightPanel.add(versionLabel);
        add(rightPanel, BorderLayout.EAST);
    }
    
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
}