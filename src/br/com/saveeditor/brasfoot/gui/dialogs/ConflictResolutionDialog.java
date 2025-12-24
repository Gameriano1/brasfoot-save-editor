package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.service.FileWatcherService.ResolutionStrategy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Diálogo para resolver conflitos quando o arquivo é modificado externamente
 * e há alterações locais não salvas.
 */
public class ConflictResolutionDialog extends JDialog {
    
    private ResolutionStrategy selectedStrategy;
    private final File file;
    
    public ConflictResolutionDialog(JFrame parent, File file) {
        super(parent, "⚠️ Conflito Detectado", true);
        this.file = file;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(550, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Ícone e título
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(iconLabel, BorderLayout.WEST);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel titleLabel = new JLabel("Conflito Detectado");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("O arquivo foi modificado externamente");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Informações do arquivo
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informações"));
        
        infoPanel.add(new JLabel("📁 Arquivo: " + file.getName()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss");
        String modifiedDate = sdf.format(new Date(file.lastModified()));
        infoPanel.add(new JLabel("📅 Modificado: " + modifiedDate));
        
        infoPanel.add(new JLabel("⚠️ Você tem alterações locais não salvas"));
        
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Opções
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Escolha uma ação:"));
        
        ButtonGroup group = new ButtonGroup();
        
        JRadioButton keepLocalRadio = new JRadioButton(
            "<html><b>Manter minhas alterações</b><br>" +
            "<small>Ignora as mudanças externas e mantém suas edições locais</small></html>");
        keepLocalRadio.setSelected(true);
        group.add(keepLocalRadio);
        optionsPanel.add(keepLocalRadio);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        JRadioButton loadExternalRadio = new JRadioButton(
            "<html><b>Carregar alterações externas</b><br>" +
            "<small>Descarta suas alterações locais e carrega o arquivo modificado</small></html>");
        group.add(loadExternalRadio);
        optionsPanel.add(loadExternalRadio);
        optionsPanel.add(Box.createVerticalStrut(10));
        
        JRadioButton saveAndReloadRadio = new JRadioButton(
            "<html><b>Salvar em novo arquivo e carregar externas</b><br>" +
            "<small>Salva suas alterações em um novo arquivo antes de recarregar</small></html>");
        group.add(saveAndReloadRadio);
        optionsPanel.add(saveAndReloadRadio);
        
        mainPanel.add(optionsPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton continueButton = new JButton("Continuar");
        continueButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        continueButton.addActionListener(e -> {
            if (keepLocalRadio.isSelected()) {
                selectedStrategy = ResolutionStrategy.KEEP_LOCAL;
            } else if (loadExternalRadio.isSelected()) {
                selectedStrategy = ResolutionStrategy.LOAD_EXTERNAL;
            } else if (saveAndReloadRadio.isSelected()) {
                selectedStrategy = ResolutionStrategy.SAVE_AND_RELOAD;
            }
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
            selectedStrategy = null;
            dispose();
        });
        
        buttonPanel.add(continueButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public ResolutionStrategy getSelectedStrategy() {
        return selectedStrategy;
    }
    
    /**
     * Mostra o diálogo e retorna a estratégia escolhida.
     */
    public static ResolutionStrategy showDialog(JFrame parent, File file) {
        ConflictResolutionDialog dialog = new ConflictResolutionDialog(parent, file);
        dialog.setVisible(true);
        return dialog.getSelectedStrategy();
    }
}
