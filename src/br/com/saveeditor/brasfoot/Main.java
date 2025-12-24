package br.com.saveeditor.brasfoot;

import br.com.saveeditor.brasfoot.gui.MainWindow;

import javax.swing.*;

/**
 * Ponto de entrada único para o Brasfoot Save Editor.
 * Interface gráfica moderna e intuitiva.
 */
public class Main {
    public static void main(String[] args) {
        // Configurar look and feel antes de criar qualquer componente
        SwingUtilities.invokeLater(() -> {
            try {
                // Tentar FlatLaf Dark
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            } catch (Exception e1) {
                try {
                    // Fallback para Nimbus
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e2) {
                    // Usar padrão do sistema
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
            }
            
            // Customizações de UI - Material Design
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.width", 14);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackInsets", new java.awt.Insets(2, 4, 2, 4));
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            
            // Cores Material Design Dark
            UIManager.put("Button.foreground", new java.awt.Color(225, 225, 225));
            UIManager.put("Button.background", new java.awt.Color(66, 66, 66));
            UIManager.put("Button.focusedBackground", new java.awt.Color(80, 80, 80));
            UIManager.put("Button.hoverBackground", new java.awt.Color(75, 75, 75));
            UIManager.put("Button.selectedBackground", new java.awt.Color(187, 134, 252)); // Primary
            
            // Painéis
            UIManager.put("Panel.background", new java.awt.Color(30, 30, 30));
            UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(45, 45, 45));
            
            // Tabelas
            UIManager.put("Table.selectionBackground", new java.awt.Color(187, 134, 252, 100));
            UIManager.put("Table.selectionForeground", new java.awt.Color(255, 255, 255));
            UIManager.put("Table.gridColor", new java.awt.Color(60, 60, 60));
            
            // Tooltips
            UIManager.put("ToolTip.background", new java.awt.Color(66, 66, 66));
            UIManager.put("ToolTip.foreground", new java.awt.Color(255, 255, 255));
            UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(100, 100, 100), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            
            // Iniciar janela principal
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}