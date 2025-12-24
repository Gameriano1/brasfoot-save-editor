package br.com.saveeditor.brasfoot.gui.components;

import br.com.saveeditor.brasfoot.gui.MainWindow;
import br.com.saveeditor.brasfoot.gui.dialogs.EditValueDialog;
import br.com.saveeditor.brasfoot.model.NavegacaoState;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * Painel com tabela de dados e paginação.
 */
public class DataTablePanel extends JPanel {

    private final MainWindow mainWindow;
    private JTable table;
    private DefaultTableModel tableModel;
    private NavegacaoState state;

    private JLabel pageLabel;
    private JButton prevButton;
    private JButton nextButton;
    private JSpinner pageSpinner;

    private int currentPage = 1;
    private int itemsPerPage = 50;
    private int totalItems = 0;
    private int totalPages = 0;

    public DataTablePanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel titleLabel = new JLabel("📊 DADOS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(187, 134, 252));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Tabela
        String[] columns = { "Campo/Índice", "Tipo", "Valor", "Ações", "ID Técnico" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Edição via dialog
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Consolas", Font.PLAIN, 12));
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(new Color(40, 40, 40));
        table.setForeground(new Color(220, 220, 220));
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(new Color(187, 134, 252, 80));
        table.setSelectionForeground(new Color(255, 255, 255));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Esconder coluna de ID Técnico
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);

        // Renderer para valor
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Destacar valores editáveis
                String tipo = (String) table.getValueAt(row, 1);
                if (isPrimitive(tipo)) {
                    c.setBackground(isSelected ? table.getSelectionBackground() : new Color(40, 60, 50));
                    c.setForeground(isSelected ? Color.WHITE : new Color(144, 238, 144)); // Light green
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : new Color(40, 40, 40));
                    c.setForeground(isSelected ? Color.WHITE : new Color(180, 180, 180));
                }

                return c;
            }
        });

        // Duplo-clique para navegar ou editar
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        handleDoubleClick(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));
        add(scrollPane, BorderLayout.CENTER);

        // Painel de paginação
        add(createPaginationPanel(), BorderLayout.SOUTH);
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)));

        // Info de paginação
        pageLabel = new JLabel("Nenhum dado");
        pageLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pageLabel.setForeground(new Color(180, 180, 180));
        panel.add(pageLabel, BorderLayout.WEST);

        // Controles de paginação
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlsPanel.setOpaque(false);

        prevButton = createPageButton("◀ Anterior");
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> previousPage());

        nextButton = createPageButton("Próxima ▶");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> nextPage());

        pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        pageSpinner.setPreferredSize(new Dimension(80, 25));
        pageSpinner.addChangeListener(e -> goToPage((Integer) pageSpinner.getValue()));

        JLabel itemsLabel = new JLabel(" Itens/página:");
        JSpinner itemsSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 500, 10));
        itemsSpinner.setPreferredSize(new Dimension(80, 25));
        itemsSpinner.addChangeListener(e -> {
            itemsPerPage = (Integer) itemsSpinner.getValue();
            currentPage = 1;
            refresh();
        });

        controlsPanel.add(prevButton);
        controlsPanel.add(new JLabel("Página:"));
        controlsPanel.add(pageSpinner);
        controlsPanel.add(nextButton);
        controlsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlsPanel.add(itemsLabel);
        controlsPanel.add(itemsSpinner);

        panel.add(controlsPanel, BorderLayout.CENTER);

        // Botão de edição completa
        JButton editObjectButton = new JButton("✏️ Editar Objeto Completo");
        editObjectButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        editObjectButton.addActionListener(e -> showEditObjectDialog());
        panel.add(editObjectButton, BorderLayout.EAST);

        return panel;
    }

    public void loadState(NavegacaoState state) {
        this.state = state;
        this.currentPage = 1;
        refresh();
    }

    public void refresh() {
        if (state == null) {
            tableModel.setRowCount(0);
            updatePaginationControls();
            return;
        }

        Object current = state.getObjetoAtual();

        if (current instanceof Collection || current.getClass().isArray()) {
            showCollectionData(current);
        } else {
            showObjectData(current);
        }

        updatePaginationControls();
    }

    private void showCollectionData(Object coll) {
        tableModel.setRowCount(0);

        totalItems = (coll instanceof Collection) ? ((Collection<?>) coll).size() : Array.getLength(coll);
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0)
            totalPages = 1;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        for (int i = startIndex; i < endIndex; i++) {
            Object item = (coll instanceof Collection) ? ((List<?>) coll).get(i) : Array.get(coll, i);

            String fieldName = "[" + i + "]";
            String type = item != null ? item.getClass().getSimpleName() : "null";
            String value = formatValue(item);
            String action = isComplex(item) ? "🔍 Ver" : "✏️ Editar";

            tableModel.addRow(new Object[] { fieldName, type, value, action, fieldName });
        }
    }

    private void showObjectData(Object obj) {
        tableModel.setRowCount(0);

        Field[] fields = obj.getClass().getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));

        totalItems = fields.length;
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0)
            totalPages = 1;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        for (int i = startIndex; i < endIndex; i++) {
            Field field = fields[i];
            if (field.isSynthetic())
                continue;

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String fieldName = field.getName();

                // Usar tradutor
                br.com.saveeditor.brasfoot.config.LabelTranslator translator = br.com.saveeditor.brasfoot.config.LabelTranslator
                        .getInstance();
                String friendlyName = translator.getLabelWithIcon(fieldName);

                String type = field.getType().getSimpleName();
                String valueStr = formatValue(value);
                String action = isComplex(value) ? "🔍 Ver" : "✏️ Editar";

                tableModel.addRow(new Object[] { friendlyName, type, valueStr, action, fieldName }); // Guardar nome
                                                                                                     // técnico
                                                                                                     // escondido ou
                                                                                                     // usar mapa
            } catch (Exception e) {
                tableModel.addRow(new Object[] { field.getName(), field.getType().getSimpleName(), "[Erro]", "" });
            }
        }
    }

    private String formatValue(Object value) {
        if (value == null)
            return "null";
        if (value instanceof Collection) {
            return "Lista[" + ((Collection<?>) value).size() + " itens]";
        }
        if (value.getClass().isArray()) {
            return "Array[" + Array.getLength(value) + " itens]";
        }
        String str = value.toString();
        return str.length() > 100 ? str.substring(0, 97) + "..." : str;
    }

    private boolean isComplex(Object obj) {
        if (obj == null || obj.getClass().isPrimitive()) {
            return false;
        }
        Package pkg = obj.getClass().getPackage();
        return pkg == null || !pkg.getName().startsWith("java");
    }

    private boolean isPrimitive(String typeName) {
        return Arrays.asList("int", "Integer", "long", "Long", "double", "Double",
                "float", "Float", "boolean", "Boolean", "String").contains(typeName);
    }

    private JButton createPageButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(new Color(230, 230, 230));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void handleDoubleClick(int row) {
        // O nome técnico agora deve ser recuperado de outra forma se a coluna 0 for
        // traduzida.
        // Vamos assumir que adicionamos uma coluna extra escondida ou vamos fazer um
        // "reverse lookup" se for simples.
        // Melhor: Adicionar coluna escondida no modelo.

        // Se mudamos o modelo no passo anterior para ter 5 colunas, precisamos pegar da
        // coluna 4 (índice 4)
        // Mas o modelo original tinha 4 colunas. Vou atualizar o modelo no próximo
        // passo.
        // Por enquanto, vou assumir que a coluna 0 AINDA é o ID técnico se for lista,
        // ou nome traduzido se for objeto.
        // Isso vai quebrar se eu não atualizar o modelo.

        // CORREÇÃO: Vou atualizar o modelo para ter 5 colunas: FriendlyName, Type,
        // Value, Action, TechnicalName
        String technicalName = (String) tableModel.getValueAt(row, 4);
        String type = (String) tableModel.getValueAt(row, 1);
        String currentValue = (String) tableModel.getValueAt(row, 2);

        if (isPrimitive(type)) {
            // Editar valor
            showEditDialog(technicalName, type, currentValue);
        } else {
            // Navegar
            navigateToField(technicalName);
        }
    }

    private void showEditDialog(String fieldName, String type, String currentValue) {
        EditValueDialog dialog = new EditValueDialog(mainWindow, fieldName, type, currentValue);
        dialog.setVisible(true);

        if (dialog.wasConfirmed()) {
            String newValue = dialog.getNewValue();
            try {
                mainWindow.getEditorService().modificarValor(
                        state.getObjetoAtual(),
                        fieldName + " = " + newValue);
                refresh();
                mainWindow.getLogPanel().log("✏️ Campo '" + fieldName + "' modificado");
                mainWindow.getStatusPanel().setStatus("✅ Modificado (não salvo)");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainWindow,
                        "Erro ao modificar: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateToField(String fieldName) {
        try {
            if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
                // Item de lista
                int index = Integer.parseInt(fieldName.substring(1, fieldName.length() - 1));
                mainWindow.getEditorService().entrarEmItemDeLista(state, String.valueOf(index));
            } else {
                // Campo de objeto
                mainWindow.getEditorService().entrarEmCampo(state, fieldName);
            }

            currentPage = 1;
            refresh();
            mainWindow.refreshView();
            mainWindow.getLogPanel().log("📂 Navegou para: " + fieldName);

        } catch (Exception e) {
            mainWindow.getLogPanel().log("❌ Erro ao navegar: " + e.getMessage());
        }
    }

    private void updatePaginationControls() {
        if (totalItems == 0) {
            pageLabel.setText("Nenhum dado");
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            pageSpinner.setEnabled(false);
            return;
        }

        int startIndex = (currentPage - 1) * itemsPerPage + 1;
        int endIndex = Math.min(currentPage * itemsPerPage, totalItems);

        pageLabel.setText(String.format("Mostrando %d-%d de %d itens", startIndex, endIndex, totalItems));

        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);

        SpinnerNumberModel model = (SpinnerNumberModel) pageSpinner.getModel();
        model.setMaximum(totalPages);
        model.setValue(currentPage);
        pageSpinner.setEnabled(totalPages > 1);
    }

    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            refresh();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            refresh();
        }
    }

    private void goToPage(int page) {
        if (page >= 1 && page <= totalPages && page != currentPage) {
            currentPage = page;
            refresh();
        }
    }

    private void showEditObjectDialog() {
        if (state == null || state.getObjetoAtual() == null)
            return;

        Object current = state.getObjetoAtual();
        if (current instanceof java.util.Collection || current.getClass().isArray()) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Selecione um item específico para editar (entre nele primeiro).",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        br.com.saveeditor.brasfoot.gui.dialogs.EditObjectDialog dialog = new br.com.saveeditor.brasfoot.gui.dialogs.EditObjectDialog(
                mainWindow, current);
        dialog.setVisible(true);

        if (dialog.wasSaved()) {
            refresh();
            mainWindow.refreshView();
            mainWindow.getLogPanel().log("✏️ Objeto editado completamente");
        }
    }
}