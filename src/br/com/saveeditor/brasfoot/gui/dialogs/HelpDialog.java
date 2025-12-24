package br.com.saveeditor.brasfoot.gui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog de ajuda com instruções completas.
 */
public class HelpDialog extends JDialog {
    
    public HelpDialog(JFrame parent) {
        super(parent, "❓ Ajuda - Brasfoot Save Editor", true);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Aba: Início Rápido
        tabbedPane.addTab("🚀 Início Rápido", createQuickStartPanel());
        
        // Aba: Navegação
        tabbedPane.addTab("🌳 Navegação", createNavigationPanel());
        
        // Aba: Edição
        tabbedPane.addTab("✏️ Edição", createEditingPanel());
        
        // Aba: Atalhos
        tabbedPane.addTab("⌨️ Atalhos", createShortcutsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Botão fechar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createQuickStartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        textArea.setText(
            "🎮 BEM-VINDO AO BRASFOOT SAVE EDITOR!\n" +
            "\n" +
            "PASSOS BÁSICOS:\n" +
            "\n" +
            "1️⃣ ABRIR UM SAVE\n" +
            "   • Clique em \"📂 Abrir\" ou pressione Ctrl+O\n" +
            "   • Selecione um arquivo .s22 do Brasfoot\n" +
            "   • O arquivo será carregado automaticamente\n" +
            "   • Um backup .bak é criado automaticamente\n" +
            "\n" +
            "2️⃣ NAVEGAR PELOS DADOS\n" +
            "   • Use a árvore à esquerda para ver a estrutura\n" +
            "   • A tabela à direita mostra os dados do item atual\n" +
            "   • Duplo-clique em objetos/listas para navegar\n" +
            "   • Use os botões \"Voltar\" e \"Raiz\" para navegar\n" +
            "\n" +
            "3️⃣ EDITAR VALORES\n" +
            "   • Duplo-clique em valores verdes na tabela\n" +
            "   • Digite o novo valor e confirme\n" +
            "   • Os valores são destacados até salvar\n" +
            "\n" +
            "4️⃣ SALVAR ALTERAÇÕES\n" +
            "   • Clique em \"💾 Salvar\" ou pressione Ctrl+S\n" +
            "   • Escolha um nome para o arquivo\n" +
            "   • Confirme para salvar\n" +
            "\n" +
            "5️⃣ RECURSOS AVANÇADOS\n" +
            "   • Use \"🔍 Buscar\" (Ctrl+F) para encontrar dados\n" +
            "   • Use \"⚽ Editar Jogador\" para edição rápida\n" +
            "   • Use \"🏆 Editar Time\" para modificar times\n" +
            "\n" +
            "⚠️ DICAS IMPORTANTES:\n" +
            "   • Sempre faça backup antes de editar\n" +
            "   • Valores em verde são editáveis\n" +
            "   • Valores em cinza são objetos/listas (navegáveis)\n" +
            "   • Use paginação para navegar em listas grandes\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        textArea.setText(
            "🌳 NAVEGAÇÃO\n" +
            "\n" +
            "ÁRVORE DE NAVEGAÇÃO (PAINEL ESQUERDO)\n" +
            "• Mostra a estrutura hierárquica do save\n" +
            "• 📁 = Objeto complexo\n" +
            "• [n] = Item de lista/array\n" +
            "• 📄 = Campo simples\n" +
            "\n" +
            "BREADCRUMB (TOPO)\n" +
            "• Mostra o caminho atual: raiz → Objeto → SubObjeto\n" +
            "• Ajuda a saber onde você está\n" +
            "\n" +
            "TABELA DE DADOS (PAINEL DIREITO)\n" +
            "• Mostra os campos/itens do objeto atual\n" +
            "• Coluna \"Campo/Índice\": Nome do campo ou índice [n]\n" +
            "• Coluna \"Tipo\": Tipo do dado (int, String, etc)\n" +
            "• Coluna \"Valor\": Valor atual\n" +
            "• Coluna \"Ações\": 🔍 Ver (navegar) ou ✏️ Editar\n" +
            "\n" +
            "DUPLO-CLIQUE:\n" +
            "• Em valores VERDES → Abre dialog de edição\n" +
            "• Em valores CINZA → Navega para dentro do objeto/lista\n" +
            "\n" +
            "BOTÕES DE NAVEGAÇÃO:\n" +
            "• ⬆️ Voltar: Volta um nível\n" +
            "• 🏠 Raiz: Volta para o início\n" +
            "• 📖 Expandir: Expande toda a árvore\n" +
            "\n" +
            "PAGINAÇÃO:\n" +
            "• Use ◀ Anterior / Próxima ▶ para navegar\n" +
            "• Digite o número da página direto\n" +
            "• Ajuste \"Itens/página\" conforme preferir (10-500)\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createEditingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        textArea.setText(
            "✏️ EDIÇÃO DE VALORES\n" +
            "\n" +
            "EDIÇÃO BÁSICA:\n" +
            "1. Duplo-clique em um valor VERDE na tabela\n" +
            "2. Digite o novo valor no dialog\n" +
            "3. Confirme ou pressione Enter\n" +
            "\n" +
            "TIPOS DE DADOS:\n" +
            "• int/Integer: Números inteiros (ex: 25, -10, 2030)\n" +
            "• long/Long: Números inteiros grandes\n" +
            "• double/Double: Números decimais (ex: 99.5, 3.14)\n" +
            "• float/Float: Números decimais menores\n" +
            "• boolean/Boolean: true ou false\n" +
            "• String: Qualquer texto (ex: \"Palmeiras\")\n" +
            "\n" +
            "EDIÇÃO RÁPIDA DE JOGADOR:\n" +
            "1. Clique em \"⚽ Editar Jogador\"\n" +
            "2. Digite o nome do jogador\n" +
            "3. Informe nova idade e força (over)\n" +
            "4. Confirme\n" +
            "\n" +
            "EDIÇÃO RÁPIDA DE TIME:\n" +
            "1. Clique em \"🏆 Editar Time\"\n" +
            "2. Digite o nome do time\n" +
            "3. Escolha o atributo a modificar\n" +
            "4. Digite o novo valor\n" +
            "5. Confirme (modifica TODOS os jogadores do time)\n" +
            "\n" +
            "SALVAR ALTERAÇÕES:\n" +
            "• As alterações ficam em memória até salvar\n" +
            "• Use Ctrl+S ou clique em \"💾 Salvar\"\n" +
            "• Escolha o nome do arquivo\n" +
            "• Recomendado: salvar com nome diferente primeiro\n" +
            "\n" +
            "⚠️ CUIDADOS:\n" +
            "• Valores inválidos podem corromper o save\n" +
            "• Sempre teste em um save de teste primeiro\n" +
            "• O backup .bak é criado automaticamente\n" +
            "• Se algo der errado, use o arquivo .bak\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createShortcutsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[][] shortcuts = {
            {"Ctrl+O", "Abrir arquivo .s22"},
            {"Ctrl+S", "Salvar alterações"},
            {"Ctrl+F", "Abrir busca"},
            {"F1", "Mostrar esta ajuda"},
            {"Enter", "Confirmar em dialogs"},
            {"Esc", "Cancelar em dialogs"},
            {"Duplo-clique", "Editar/Navegar"},
            {"", ""},
            {"NAVEGAÇÃO:", ""},
            {"⬆️ Voltar", "Volta um nível na navegação"},
            {"🏠 Raiz", "Volta para o início"},
            {"◀ Anterior", "Página anterior"},
            {"Próxima ▶", "Próxima página"},
            {"", ""},
            {"DICAS:", ""},
            {"Valores VERDES", "São editáveis"},
            {"Valores CINZA", "São navegáveis"},
            {"[número]", "Indica índice em lista/array"},
        };
        
        JTable table = new JTable(shortcuts, new String[]{"Atalho", "Descrição"});
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setEnabled(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
}