package br.com.saveeditor.brasfoot.gui.components;

import br.com.saveeditor.brasfoot.gui.MainWindow;
import br.com.saveeditor.brasfoot.model.NavegacaoState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador de abas com suporte a split view e comparação.
 */
public class TabManager extends JPanel {
    
    private final MainWindow mainWindow;
    private final JTabbedPane tabbedPane;
    private final List<EditorTab> tabs;
    private JSplitPane splitPane;
    private boolean splitMode = false;
    
    public TabManager(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.tabs = new ArrayList<>();
        this.tabbedPane = new JTabbedPane();
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(30, 30, 30));
        
        // Estilizar JTabbedPane
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(new Color(35, 35, 35));
        tabbedPane.setForeground(new Color(220, 220, 220));
        
        // Listener para mudança de aba
        tabbedPane.addChangeListener(e -> {
            EditorTab currentTab = getCurrentTab();
            if (currentTab != null) {
                mainWindow.onTabChanged(currentTab);
            }
        });
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Painel de controles de visualização
        add(createViewControlPanel(), BorderLayout.NORTH);
    }
    
    private JPanel createViewControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        JButton splitHorizontalBtn = createViewButton("⬌ Split Horizontal");
        splitHorizontalBtn.setToolTipText("Dividir visualização horizontalmente");
        splitHorizontalBtn.addActionListener(e -> toggleSplit(JSplitPane.HORIZONTAL_SPLIT));
        
        JButton splitVerticalBtn = createViewButton("⬍ Split Vertical");
        splitVerticalBtn.setToolTipText("Dividir visualização verticalmente");
        splitVerticalBtn.addActionListener(e -> toggleSplit(JSplitPane.VERTICAL_SPLIT));
        
        JButton closeSplitBtn = createViewButton("✕ Fechar Split");
        closeSplitBtn.setToolTipText("Fechar visualização dividida");
        closeSplitBtn.addActionListener(e -> closeSplit());
        
        JButton closeTabBtn = createViewButton("🗙 Fechar Aba");
        closeTabBtn.setToolTipText("Fechar aba atual");
        closeTabBtn.addActionListener(e -> closeCurrentTab());
        
        JButton closeAllBtn = createViewButton("🗙🗙 Fechar Todas");
        closeAllBtn.setToolTipText("Fechar todas as abas");
        closeAllBtn.addActionListener(e -> closeAllTabs());
        
        panel.add(splitHorizontalBtn);
        panel.add(splitVerticalBtn);
        panel.add(closeSplitBtn);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(closeTabBtn);
        panel.add(closeAllBtn);
        
        return panel;
    }
    
    private JButton createViewButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setFocusPainted(false);
        button.setBackground(new Color(55, 55, 55));
        button.setForeground(new Color(220, 220, 220));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(70, 70, 70));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(55, 55, 55));
            }
        });
        
        return button;
    }
    
    /**
     * Cria painel de controles minimalista para split view.
     */
    private JPanel createSplitControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 60)),
            new EmptyBorder(0, 0, 0, 0)
        ));
        
        JLabel label = new JLabel("⬌ SPLIT VIEW");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(187, 134, 252));
        
        JButton closeSplitBtn = createViewButton("✕ Fechar Split");
        closeSplitBtn.setToolTipText("Voltar para visualização em abas");
        closeSplitBtn.addActionListener(e -> closeSplit());
        
        panel.add(label);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(closeSplitBtn);
        
        return panel;
    }
    
    /**
     * Adiciona uma nova aba ao gerenciador.
     */
    public void addTab(EditorTab tab) {
        tabs.add(tab);
        
        // Criar painel de título com botão de fechar
        JPanel titlePanel = new JPanel(new BorderLayout(5, 0));
        titlePanel.setOpaque(false);
        
        String displayName = tab.getFileName();
        JLabel titleLabel = new JLabel("📄 " + displayName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 10));
        closeBtn.setPreferredSize(new Dimension(20, 20));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> closeTab(tab));
        titlePanel.add(closeBtn, BorderLayout.EAST);
        
        tabbedPane.addTab(null, tab);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(index, titlePanel);
        tabbedPane.setSelectedIndex(index);
        
        mainWindow.getLogPanel().log("📂 Aberto: " + displayName);
    }
    
    /**
     * Fecha uma aba específica.
     */
    public void closeTab(EditorTab tab) {
        if (tab.isModified()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "O arquivo '" + tab.getFileName() + "' foi modificado.\nDeseja salvar antes de fechar?",
                "Salvar alterações?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                mainWindow.saveFile();
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        
        int index = tabbedPane.indexOfComponent(tab);
        if (index >= 0) {
            tabbedPane.removeTabAt(index);
            tabs.remove(tab);
            mainWindow.getLogPanel().log("🗙 Fechado: " + tab.getFileName());
        }
    }
    
    /**
     * Fecha a aba atual.
     */
    public void closeCurrentTab() {
        EditorTab current = getCurrentTab();
        if (current != null) {
            closeTab(current);
        }
    }
    
    /**
     * Fecha todas as abas.
     */
    public void closeAllTabs() {
        if (tabs.isEmpty()) return;
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Deseja fechar todas as " + tabs.size() + " abas?",
            "Fechar todas",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            while (!tabs.isEmpty()) {
                closeTab(tabs.get(0));
            }
        }
    }
    
    /**
     * Ativa/desativa modo split.
     */
    private void toggleSplit(int orientation) {
        if (tabs.size() < 2) {
            JOptionPane.showMessageDialog(this,
                "É necessário ter pelo menos 2 abas abertas para dividir a visualização.",
                "Aviso",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (splitMode) {
            closeSplit();
        }
        
        // Criar split com as duas primeiras abas
        splitPane = new JSplitPane(orientation);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(6);
        splitPane.setBackground(new Color(60, 60, 60));
        splitPane.setResizeWeight(0.5);
        
        // Remover abas do tabbedPane temporariamente
        EditorTab firstTab = tabs.get(0);
        EditorTab secondTab = tabs.size() > 1 ? tabs.get(1) : null;
        
        int firstIndex = tabbedPane.indexOfComponent(firstTab);
        if (firstIndex >= 0) {
            tabbedPane.removeTabAt(firstIndex);
        }
        
        if (secondTab != null) {
            int secondIndex = tabbedPane.indexOfComponent(secondTab);
            if (secondIndex >= 0) {
                tabbedPane.removeTabAt(secondIndex);
            }
        }
        
        // Adicionar no split
        splitPane.setLeftComponent(firstTab);
        if (secondTab != null) {
            splitPane.setRightComponent(secondTab);
        }
        
        removeAll();
        // Adicionar painel minimalista só com botão de fechar split
        add(createSplitControlPanel(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        revalidate();
        repaint();
        
        splitMode = true;
        mainWindow.getLogPanel().log("⬌ Split view ativado");
    }
    
    /**
     * Fecha o modo split.
     */
    private void closeSplit() {
        if (!splitMode) return;
        
        // Remover componentes do split
        Component left = splitPane.getLeftComponent();
        Component right = splitPane.getRightComponent();
        
        splitPane.setLeftComponent(null);
        splitPane.setRightComponent(null);
        
        // Recolocar abas no tabbedPane
        if (left instanceof EditorTab) {
            EditorTab tab = (EditorTab) left;
            int index = tabs.indexOf(tab);
            if (index >= 0) {
                // Recriar o componente de título
                JPanel titlePanel = new JPanel(new BorderLayout(5, 0));
                titlePanel.setOpaque(false);
                
                JLabel titleLabel = new JLabel("📄 " + tab.getFileName());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                titlePanel.add(titleLabel, BorderLayout.CENTER);
                
                JButton closeBtn = new JButton("✕");
                closeBtn.setFont(new Font("Arial", Font.BOLD, 10));
                closeBtn.setPreferredSize(new Dimension(20, 20));
                closeBtn.setFocusPainted(false);
                closeBtn.setBorderPainted(false);
                closeBtn.setContentAreaFilled(false);
                closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                closeBtn.addActionListener(e -> closeTab(tab));
                titlePanel.add(closeBtn, BorderLayout.EAST);
                
                tabbedPane.insertTab(null, null, tab, null, index);
                tabbedPane.setTabComponentAt(index, titlePanel);
            }
        }
        
        if (right instanceof EditorTab) {
            EditorTab tab = (EditorTab) right;
            int index = tabs.indexOf(tab);
            if (index >= 0) {
                // Recriar o componente de título
                JPanel titlePanel = new JPanel(new BorderLayout(5, 0));
                titlePanel.setOpaque(false);
                
                JLabel titleLabel = new JLabel("📄 " + tab.getFileName());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                titlePanel.add(titleLabel, BorderLayout.CENTER);
                
                JButton closeBtn = new JButton("✕");
                closeBtn.setFont(new Font("Arial", Font.BOLD, 10));
                closeBtn.setPreferredSize(new Dimension(20, 20));
                closeBtn.setFocusPainted(false);
                closeBtn.setBorderPainted(false);
                closeBtn.setContentAreaFilled(false);
                closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                closeBtn.addActionListener(e -> closeTab(tab));
                titlePanel.add(closeBtn, BorderLayout.EAST);
                
                tabbedPane.insertTab(null, null, tab, null, index);
                tabbedPane.setTabComponentAt(index, titlePanel);
            }
        }
        
        removeAll();
        add(createViewControlPanel(), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        revalidate();
        repaint();
        
        splitMode = false;
        mainWindow.getLogPanel().log("✕ Split view desativado");
    }
    
    /**
     * Retorna a aba atual.
     */
    public EditorTab getCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index >= 0 && index < tabs.size()) {
            return tabs.get(index);
        }
        return null;
    }
    
    /**
     * Retorna todas as abas abertas.
     */
    public List<EditorTab> getAllTabs() {
        return new ArrayList<>(tabs);
    }
    
    /**
     * Verifica se há abas abertas.
     */
    public boolean hasTabs() {
        return !tabs.isEmpty();
    }
    
    /**
     * Retorna o número de abas abertas.
     */
    public int getTabCount() {
        return tabs.size();
    }
}
