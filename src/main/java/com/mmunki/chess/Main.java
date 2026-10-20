package com.mmunki.chess;

import com.mmunki.chess.ui.ChessFrame;
import com.mmunki.chess.ui.Theme;
import com.mmunki.chess.util.Storage;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.GraphicsEnvironment;

/**
 * The start of the chess game.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("chess needs a screen, it can not run without one");
            System.exit(1);
        }
        setLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            Storage storage = new Storage();
            ChessFrame frame = new ChessFrame(storage);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /** Makes the windows of the game look like the rest of it. */
    private static void setLookAndFeel() {
        UIManager.put("Panel.background", Theme.PANEL);
        UIManager.put("OptionPane.background", Theme.PANEL);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        UIManager.put("OptionPane.messageFont", Theme.font(java.awt.Font.PLAIN, 15));
        UIManager.put("Button.background", Theme.PANEL_LIGHT);
        UIManager.put("Button.foreground", Theme.TEXT);
        UIManager.put("Button.font", Theme.font(java.awt.Font.BOLD, 14));
        UIManager.put("Label.foreground", Theme.TEXT);
        UIManager.put("Label.font", Theme.font(java.awt.Font.PLAIN, 14));
        UIManager.put("ComboBox.background", Theme.PANEL_LIGHT);
        UIManager.put("ComboBox.foreground", Theme.TEXT);
        UIManager.put("TextField.background", Theme.PANEL_LIGHT);
        UIManager.put("TextField.foreground", Theme.TEXT);
        UIManager.put("CheckBox.background", Theme.PANEL);
        UIManager.put("CheckBox.foreground", Theme.TEXT);
        UIManager.put("RadioButton.background", Theme.PANEL);
        UIManager.put("RadioButton.foreground", Theme.TEXT);
        UIManager.put("Slider.background", Theme.PANEL);
        UIManager.put("Slider.foreground", Theme.ACCENT);
        UIManager.put("ScrollPane.background", Theme.PANEL);
        UIManager.put("ScrollBar.background", Theme.PANEL);
        UIManager.put("ScrollBar.thumb", Theme.PANEL_LIGHT);
        UIManager.put("Table.background", Theme.PANEL);
        UIManager.put("Table.foreground", Theme.TEXT);
        UIManager.put("Table.gridColor", Theme.BORDER);
        UIManager.put("Table.selectionBackground", Theme.PANEL_LIGHT);
        UIManager.put("TableHeader.background", Theme.PANEL);
        UIManager.put("TableHeader.foreground", Theme.TEXT);
        UIManager.put("Viewport.background", Theme.PANEL);
        UIManager.put("MenuBar.background", Theme.PANEL);
        UIManager.put("Menu.background", Theme.PANEL);
        UIManager.put("Menu.foreground", Theme.TEXT);
        UIManager.put("MenuItem.background", Theme.PANEL);
        UIManager.put("MenuItem.foreground", Theme.TEXT);
        UIManager.put("PopupMenu.background", Theme.PANEL);
        UIManager.put("TextArea.background", Theme.PANEL);
        UIManager.put("TextArea.foreground", Theme.TEXT);
        UIManager.put("nimbusBase", Theme.PANEL);
        UIManager.put("control", Theme.PANEL_LIGHT);
        UIManager.put("nimbusSelectionBackground", Theme.ACCENT);
        UIManager.put("nimbusSelectionText", Color.WHITE);
    }
}
