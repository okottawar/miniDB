package ui;

import service.DbService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Left-hand panel: lists all tables in the database and lets the user
 * pick which one to view/edit in the DataPanel.
 */
public class TablePanel extends JPanel {

    private final DbService dbService;
    private final DefaultListModel<String> listModel;
    private final JList<String> tableList;
    private Consumer<String> onTableSelected;

    public TablePanel(DbService dbService) {
        this.dbService = dbService;
        this.listModel = new DefaultListModel<>();
        this.tableList = new JList<>(listModel);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Tables"));
        setPreferredSize(new Dimension(180, 0));

        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = tableList.getSelectedValue();
                if (selected != null && onTableSelected != null) {
                    onTableSelected.accept(selected);
                }
            }
        });

        add(new JScrollPane(tableList), BorderLayout.CENTER);
    }

    public void setOnTableSelected(Consumer<String> callback) {
        this.onTableSelected = callback;
    }

    /**
     * Reloads the list of table names from the DbService.
     */
    public void refresh() {
        String previouslySelected = tableList.getSelectedValue();
        listModel.clear();
        List<String> names = dbService.listTables();
        for (String name : names) {
            listModel.addElement(name);
        }
        if (previouslySelected != null && names.contains(previouslySelected)) {
            tableList.setSelectedValue(previouslySelected, true);
        } else if (!names.isEmpty()) {
            tableList.setSelectedIndex(0);
        }
    }

    public String getSelectedTable() {
        return tableList.getSelectedValue();
    }
}
