package br.com.saveeditor.brasfoot.gui.components;

import br.com.saveeditor.brasfoot.gui.MainWindow;
import br.com.saveeditor.brasfoot.model.NavegacaoState;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Stack;

/**
 * Painel de navegação em árvore.
 */
public class NavigationPanel extends JPanel {
    
    private final MainWindow mainWindow;
    private JTree tree;
    private JLabel breadcrumbLabel;
    private NavegacaoState state;
    
    public NavigationPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(35, 35, 35));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("🌳 NAVEGAÇÃO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(187, 134, 252));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Breadcrumb
        breadcrumbLabel = new JLabel("Nenhum arquivo carregado");
        breadcrumbLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        breadcrumbLabel.setForeground(new Color(180, 180, 180));
        breadcrumbLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        headerPanel.add(breadcrumbLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Árvore
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Raiz");
        tree = new JTree(root);
        tree.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tree.setBackground(new Color(40, 40, 40));
        tree.setForeground(new Color(220, 220, 220));
        tree.setRowHeight(28);
        tree.addTreeSelectionListener(e -> onTreeSelection());
        
        // Adicionar listener de duplo-clique
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        onTreeDoubleClick(path);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));
        add(scrollPane, BorderLayout.CENTER);
        
        // Botões
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton backButton = createNavButton("⬆️ Voltar");
        backButton.addActionListener(e -> navigateBack());
        
        JButton rootButton = createNavButton("🏠 Raiz");
        rootButton.addActionListener(e -> navigateToRoot());
        
        JButton expandButton = createNavButton("📖 Expandir");
        expandButton.addActionListener(e -> expandAll());
        
        buttonPanel.add(backButton);
        buttonPanel.add(rootButton);
        buttonPanel.add(expandButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(new Color(230, 230, 230));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(75, 75, 75));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
        });
        
        return button;
    }
    
    public void loadState(NavegacaoState state) {
        this.state = state;
        buildTree();
        updateBreadcrumb();
    }
    
    public void refresh() {
        if (state != null) {
            buildTree();
            updateBreadcrumb();
        }
    }
    
    private void buildTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("📁 Raiz");
        if (state != null) {
            buildNode(root, state.getObjetoRaiz(), 0);
        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        tree.setModel(model);
        tree.expandRow(0);
    }
    
    private void buildNode(DefaultMutableTreeNode parent, Object obj, int depth) {
        if (obj == null || depth > 2) return;
        
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            int count = 0;
            for (Object item : coll) {
                if (count >= 50) {
                    parent.add(new DefaultMutableTreeNode("... (" + (coll.size() - 50) + " itens restantes)"));
                    break;
                }
                String label = String.format("[%d] %s", count, getLabel(item));
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);
                node.setUserObject(new TreeNodeData(label, count, true)); // Guardar metadados
                parent.add(node);
                if (depth < 2) buildNode(node, item, depth + 1);
                count++;
            }
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < Math.min(len, 50); i++) {
                Object item = Array.get(obj, i);
                String label = String.format("[%d] %s", i, getLabel(item));
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);
                node.setUserObject(new TreeNodeData(label, i, true)); // Guardar metadados
                parent.add(node);
                if (depth < 2) buildNode(node, item, depth + 1);
            }
            if (len > 50) {
                parent.add(new DefaultMutableTreeNode("... (" + (len - 50) + " itens restantes)"));
            }
        } else {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isSynthetic()) continue;
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    String label = String.format("📄 %s: %s", field.getName(), getLabel(value));
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);
                    node.setUserObject(new TreeNodeData(label, field.getName(), false)); // Guardar metadados
                    parent.add(node);
                    if (depth < 2 && isComplex(value)) {
                        buildNode(node, value, depth + 1);
                    }
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }
    }
    
    private String getLabel(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Collection) {
            return "Lista[" + ((Collection<?>) obj).size() + "]";
        }
        if (obj.getClass().isArray()) {
            return "Array[" + Array.getLength(obj) + "]";
        }
        if (obj.getClass().getName().startsWith("java.lang")) {
            String str = obj.toString();
            return str.length() > 40 ? str.substring(0, 37) + "..." : str;
        }
        return obj.getClass().getSimpleName();
    }
    
    private boolean isComplex(Object obj) {
        if (obj == null || obj.getClass().isPrimitive()) {
            return false;
        }
        Package pkg = obj.getClass().getPackage();
        return pkg == null || !pkg.getName().startsWith("java");
    }
    
    private void updateBreadcrumb() {
        if (state == null) {
            breadcrumbLabel.setText("Nenhum arquivo carregado");
            return;
        }
        
        StringBuilder sb = new StringBuilder("📍 ");
        Stack<Object> trail = state.getTrilhaNavegacao();
        int i = 0;
        for (Object obj : trail) {
            if (i++ > 0) sb.append(" → ");
            sb.append(obj.getClass().getSimpleName());
            if (sb.length() > 80) {
                sb.append("...");
                break;
            }
        }
        breadcrumbLabel.setText(sb.toString());
    }
    
    private void onTreeSelection() {
        // Pode ser usado para sincronizar seleção com a tabela no futuro
    }
    
    private void onTreeDoubleClick(TreePath path) {
        if (state == null) return;
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        
        // Ignorar nós de "..." (restantes)
        if (userObject instanceof String && ((String) userObject).startsWith("...")) {
            return;
        }
        
        // Ignorar raiz
        if (node.isRoot()) {
            return;
        }
        
        // Extrair informação do nó
        String fieldName = null;
        Integer index = null;
        boolean isArrayItem = false;
        
        if (userObject instanceof TreeNodeData) {
            TreeNodeData data = (TreeNodeData) userObject;
            isArrayItem = data.isArrayItem;
            if (isArrayItem) {
                index = (Integer) data.identifier;
            } else {
                fieldName = (String) data.identifier;
            }
        } else {
            // Fallback: extrair do label
            String label = userObject.toString();
            if (label.startsWith("[") && label.contains("]")) {
                // É um item de array/lista
                isArrayItem = true;
                String indexStr = label.substring(1, label.indexOf("]"));
                try {
                    index = Integer.parseInt(indexStr);
                } catch (NumberFormatException e) {
                    return;
                }
            } else if (label.startsWith("📄 ")) {
                // É um campo
                String resto = label.substring(2); // Remove "📄 "
                if (resto.contains(":")) {
                    fieldName = resto.substring(0, resto.indexOf(":")).trim();
                }
            }
        }
        
        // Navegar
        try {
            if (isArrayItem && index != null) {
                mainWindow.getEditorService().entrarEmItemDeLista(state, String.valueOf(index));
                mainWindow.getLogPanel().log("📂 Navegou para item [" + index + "] via árvore");
            } else if (fieldName != null) {
                mainWindow.getEditorService().entrarEmCampo(state, fieldName);
                mainWindow.getLogPanel().log("📂 Navegou para campo '" + fieldName + "' via árvore");
            } else {
                return; // Não conseguiu identificar
            }
            
            // Atualizar interfaces
            refresh();
            // A aba já gerencia a sincronia entre painéis
            
        } catch (Exception e) {
            mainWindow.getLogPanel().log("❌ Erro ao navegar via árvore: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Não foi possível navegar para este item:\n" + e.getMessage(),
                "Erro de Navegação",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void navigateBack() {
        if (state != null) {
            state.voltar();
            refresh();
            mainWindow.refreshView();
            mainWindow.getLogPanel().log("⬆️ Voltou um nível");
        }
    }
    
    private void navigateToRoot() {
        if (state != null) {
            state.irParaTopo();
            refresh();
            mainWindow.refreshView();
            mainWindow.getLogPanel().log("🏠 Voltou à raiz");
        }
    }
    
    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
    
    /**
     * Classe interna para guardar metadados dos nós da árvore.
     */
    private static class TreeNodeData {
        String label;
        Object identifier; // Pode ser String (nome do campo) ou Integer (índice)
        boolean isArrayItem;
        
        TreeNodeData(String label, Object identifier, boolean isArrayItem) {
            this.label = label;
            this.identifier = identifier;
            this.isArrayItem = isArrayItem;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
}