package br.com.saveeditor.brasfoot.gui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog de boas-vindas inicial.
 */
public class WelcomeDialog extends JDialog {
    
    private boolean openFile = false;
    
    public WelcomeDialog(JFrame parent) {
        super(parent, "Bem-vindo ao Brasfoot Save Editor", true);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Logo/Título
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("🎮 BRASFOOT SAVE EDITOR");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("Versão 2.0 - Interface Gráfica Moderna");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(versionLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Conteúdo central
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        String[] features = {
            "✅ Navegação intuitiva em árvore",
            "✅ Edição visual de valores",
            "✅ Busca avançada de dados",
            "✅ Paginação inteligente",
            "✅ Backup automático",
            "✅ Edição rápida de jogadores e times",
            "✅ Interface moderna e responsiva",
            "✅ Atalhos de teclado"
        };
        
        for (String feature : features) {
            JLabel label = new JLabel(feature);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(label);
            contentPanel.add(Box.createVerticalStrut(8));
        }
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton openButton = new JButton("📂 Abrir Save");
        openButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openButton.setPreferredSize(new Dimension(180, 40));
        openButton.addActionListener(e -> {
            openFile = true;
            dispose();
        });
        
        JButton laterButton = new JButton("Mais Tarde");
        laterButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        laterButton.setPreferredSize(new Dimension(150, 40));
        laterButton.addActionListener(e -> dispose());
        
        JCheckBox dontShowAgain = new JCheckBox("Não mostrar novamente");
        dontShowAgain.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        buttonPanel.add(openButton);
        buttonPanel.add(laterButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(dontShowAgain, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    public boolean shouldOpenFile() {
        return openFile;
    }
}