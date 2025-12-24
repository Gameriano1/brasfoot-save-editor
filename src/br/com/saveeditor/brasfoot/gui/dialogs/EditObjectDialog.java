package br.com.saveeditor.brasfoot.gui.dialogs;

import br.com.saveeditor.brasfoot.config.LabelTranslator;
import br.com.saveeditor.brasfoot.gui.MainWindow;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Diálogo para editar todos os campos de um objeto.
 */
public class EditObjectDialog extends JDialog {

    private final MainWindow mainWindow;
    private final Object targetObject;
    private final Map<String, JTextField> fieldMap;
    private boolean saved = false;

    public EditObjectDialog(MainWindow mainWindow, Object targetObject) {
        super(mainWindow, "✏️ Editar Objeto", true);
        this.mainWindow = mainWindow;
        this.targetObject = targetObject;
        this.fieldMap = new HashMap<>();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 600);
        setLocationRelativeTo(getParent());

        // Scroll Panel para os campos
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        LabelTranslator translator = LabelTranslator.getInstance();
        Field[] fields = targetObject.getClass().getDeclaredFields();

        int row = 0;
        for (Field field : fields) {
            if (field.isSynthetic())
                continue;

            // Ignorar campos complexos (listas, arrays, outros objetos)
            if (!isPrimitiveOrString(field.getType()))
                continue;

            field.setAccessible(true);
            String fieldName = field.getName();
            String labelText = translator.getLabelWithIcon(fieldName);
            String tooltip = translator.getDescription(fieldName);

            // Label
            JLabel label = new JLabel(labelText + ":");
            if (tooltip != null)
                label.setToolTipText(tooltip);
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.3;
            fieldsPanel.add(label, gbc);

            // Input
            Object value = null;
            try {
                value = field.get(targetObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            JTextField input = new JTextField(value != null ? value.toString() : "");
            fieldMap.put(fieldName, input);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            fieldsPanel.add(input, gbc);

            row++;
        }

        // Adicionar filler para empurrar tudo para cima
        gbc.gridy = row;
        gbc.weighty = 1.0;
        fieldsPanel.add(Box.createVerticalGlue(), gbc);

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("💾 Salvar Tudo");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.addActionListener(e -> save());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private boolean isPrimitiveOrString(Class<?> type) {
        return type.isPrimitive() ||
                Number.class.isAssignableFrom(type) ||
                Boolean.class.isAssignableFrom(type) ||
                String.class.equals(type);
    }

    private void save() {
        int successCount = 0;
        int errorCount = 0;

        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String fieldName = entry.getKey();
            String valueStr = entry.getValue().getText();

            try {
                // Usar EditorService para modificar (garante conversão correta)
                mainWindow.getEditorService().modificarValor(
                        targetObject,
                        fieldName + " = " + valueStr);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                System.err.println("Erro ao salvar campo " + fieldName + ": " + e.getMessage());
            }
        }

        if (errorCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "✅ Todos os campos salvos com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Salvo com " + errorCount + " erros.\nVerifique o log para detalhes.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            saved = true; // Considerar salvo parcialmente
            dispose();
        }
    }

    public boolean wasSaved() {
        return saved;
    }
}
