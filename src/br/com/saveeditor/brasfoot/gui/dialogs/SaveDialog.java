package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.SaveFileService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Dialog para salvar o arquivo com validação.
 */
public class SaveDialog extends JDialog {
    
    private final NavegacaoState state;
    private final SaveFileService saveFileService;
    private JTextField fileNameField;
    private boolean saved = false;
    
    public SaveDialog(JFrame parent, NavegacaoState state) {
        super(parent, "Salvar Save", true);
        this.state = state;
        this.saveFileService = new SaveFileService();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(550, 300);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("💾 Salvar Alterações");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("Digite o nome do arquivo para salvar:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(titleLabel);
        infoPanel.add(descLabel);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Campo de nome
        JPanel fieldPanel = new JPanel(new BorderLayout(10, 10));
        
        File originalFile = new File(state.getCaminhoArquivoOriginal());
        String defaultName = originalFile.getName().replace(".s22", "_editado.s22");
        
        JLabel pathLabel = new JLabel("📁 " + originalFile.getParent());
        pathLabel.setFont(new Font("Consolas", Font.PLAIN, 10));
        pathLabel.setForeground(Color.GRAY);
        
        fileNameField = new JTextField(defaultName);
        fileNameField.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        fieldPanel.add(pathLabel, BorderLayout.NORTH);
        fieldPanel.add(fileNameField, BorderLayout.CENTER);
        
        mainPanel.add(fieldPanel, BorderLayout.CENTER);
        
        // Avisos
        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
        warningPanel.setBorder(BorderFactory.createTitledBorder("⚠️ Importante"));
        
        String[] warnings = {
            "✅ Um backup do arquivo original foi criado (.bak)",
            "✅ O arquivo será validado após salvar",
            "✅ Você pode salvar com um nome diferente",
            "⚠️ Se salvar com o mesmo nome, o original será substituído"
        };
        
        for (String warning : warnings) {
            JLabel label = new JLabel(warning);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            warningPanel.add(label);
        }
        
        mainPanel.add(warningPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton saveButton = new JButton("💾 Salvar");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.addActionListener(e -> save());
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter para salvar
        fileNameField.addActionListener(e -> saveButton.doClick());
        
        SwingUtilities.invokeLater(() -> fileNameField.selectAll());
    }
    
    private void save() {
        String fileName = fileNameField.getText().trim();
        
        if (fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite um nome para o arquivo!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!fileName.endsWith(".s22")) {
            fileName += ".s22";
        }
        
        // Criar variável final para uso no SwingWorker
        final String finalFileName = fileName;
        
        // Confirmar sobrescrita
        File originalFile = new File(state.getCaminhoArquivoOriginal());
        File targetFile = new File(originalFile.getParent(), finalFileName);
        
        if (targetFile.exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                "O arquivo '" + finalFileName + "' já existe.\nDeseja substituí-lo?",
                "Confirmar Sobrescrita",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Mostrar progresso
        JDialog progressDialog = new JDialog(this, "Salvando...", true);
        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        progressPanel.add(new JLabel("💾 Salvando arquivo..."), BorderLayout.NORTH);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        progressDialog.add(progressPanel);
        
        // Salvar em background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            String savedPath;
            
            @Override
            protected Boolean doInBackground() {
                try {
                    // Salvar
                    saveFileService.salvarSave(state, finalFileName);
                    savedPath = targetFile.getAbsolutePath();
                    
                    // Aguardar um pouco
                    Thread.sleep(500);
                    
                    // Validar
                    return saveFileService.validarArquivoSalvo(savedPath);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    boolean valid = get();
                    
                    if (valid) {
                        saved = true;
                        
                        JOptionPane.showMessageDialog(SaveDialog.this,
                            "✅ Arquivo salvo e validado com sucesso!\n\n" +
                            "📁 " + savedPath,
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(SaveDialog.this,
                            "⚠️ Arquivo foi salvo mas a validação falhou!\n\n" +
                            "O arquivo pode estar corrompido.\n" +
                            "Verifique o backup .bak antes de usar.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SaveDialog.this,
                        "❌ Erro ao salvar arquivo:\n" + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    public boolean wasSaved() {
        return saved;
    }
}