package ui;

import service.DbService;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level Swing window that assembles TablePanel (left), DataPanel (center),
 * and CommandPanel (bottom) into a single MiniDB GUI.
 */
public class MainFrame extends JFrame {

    public MainFrame(DbService dbService) {
        super("MiniDB");

        TablePanel tablePanel = new TablePanel(dbService);
        DataPanel dataPanel = new DataPanel(dbService);
        CommandPanel commandPanel = new CommandPanel(dbService, tablePanel, dataPanel);

        tablePanel.setOnTableSelected(dataPanel::showTable);

        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.WEST);
        add(dataPanel, BorderLayout.CENTER);
        add(commandPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        tablePanel.refresh();
        if (!dbService.listTables().isEmpty()) {
            String first = dbService.listTables().get(0);
            dataPanel.showTable(first);
        }
    }
}
