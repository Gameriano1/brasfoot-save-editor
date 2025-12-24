package br.com.saveeditor.brasfoot.gui.components;

import br.com.saveeditor.brasfoot.model.NavegacaoState;

import javax.swing.*;
import java.awt.*;

/**
 * Representa uma aba de edição com seus próprios painéis.
 */
public class EditorTab extends JPanel {
    
    private final NavegacaoState state;
    private final NavigationPanel navigationPanel;
    private final DataTablePanel dataTablePanel;
    private final String filePath;
    private boolean modified = false;
    
    public EditorTab(NavegacaoState state, NavigationPanel navigationPanel, 
                     DataTablePanel dataTablePanel, String filePath) {
        this.state = state;
        this.navigationPanel = navigationPanel;
        this.dataTablePanel = dataTablePanel;
        this.filePath = filePath;
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(30, 30, 30));
        
        // Split principal (navegação | dados)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(350);
        mainSplit.setDividerSize(4);
        mainSplit.setBackground(new Color(60, 60, 60));
        mainSplit.setLeftComponent(navigationPanel);
        mainSplit.setRightComponent(dataTablePanel);
        
        add(mainSplit, BorderLayout.CENTER);
    }
    
    public NavegacaoState getState() {
        return state;
    }
    
    public NavigationPanel getNavigationPanel() {
        return navigationPanel;
    }
    
    public DataTablePanel getDataTablePanel() {
        return dataTablePanel;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getFileName() {
        return new java.io.File(filePath).getName();
    }
    
    public boolean isModified() {
        return modified;
    }
    
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
    public void refresh() {
        navigationPanel.refresh();
        dataTablePanel.refresh();
    }
}
