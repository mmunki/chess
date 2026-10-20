package com.mmunki.chess.ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

/**
 * The list of the moves that were played, the way a score sheet shows them.
 */
public final class MoveListPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"", "White", "Black"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public MoveListPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER);
        table.setShowVerticalLines(false);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(Theme.font(Font.PLAIN, 14));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean selected, boolean focused,
                                                           int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, selected,
                        focused, row, column);
                setBorder(null);
                setFont(Theme.font(column == 0 ? Font.PLAIN : Font.BOLD, 14));
                if (selected) {
                    component.setBackground(Theme.PANEL_LIGHT);
                    component.setForeground(Theme.TEXT);
                } else {
                    component.setBackground(Theme.PANEL);
                    component.setForeground(column == 0 ? Theme.TEXT_MUTED : Theme.TEXT);
                }
                return component;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        table.setTableHeader(null);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);
    }

    /** Shows the moves of the game. */
    public void setMoves(List<String> scoreSheet) {
        model.setRowCount(0);
        int number = 1;
        for (int i = 0; i < scoreSheet.size(); i++) {
            String token = scoreSheet.get(i);
            if (token.endsWith(".")) {
                model.addRow(new Object[]{token, "", ""});
            } else if (model.getRowCount() == 0
                    || model.getValueAt(model.getRowCount() - 1, 1).equals("")) {
                int row = model.getRowCount() - 1;
                if (row < 0) {
                    model.addRow(new Object[]{"", "", ""});
                    row = 0;
                }
                model.setValueAt(token, row, 1);
            } else {
                model.setValueAt(token, model.getRowCount() - 1, 2);
            }
            number++;
        }
        table.repaint();
    }

    /** Scrolls to the last move. */
    public void scrollToBottom() {
        int rows = model.getRowCount();
        if (rows > 0) {
            table.scrollRectToVisible(table.getCellRect(rows - 1, 0, true));
        }
    }
}
