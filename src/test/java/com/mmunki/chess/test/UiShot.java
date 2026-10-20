package com.mmunki.chess.test;

import com.mmunki.chess.ui.BoardPanel;
import com.mmunki.chess.ui.ChessFrame;
import com.mmunki.chess.util.Storage;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Takes a picture of the window of the game.
 *
 * <p>It plays a few moves with the mouse and then takes the picture, so that
 * the score sheet is not empty.  It needs a screen, on a computer without one
 * it does nothing.
 */
public final class UiShot {

    private UiShot() {
    }

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("no screen, no picture");
            return;
        }
        File settings = new File(System.getProperty("java.io.tmpdir"), "chess-shot.properties");
        try (java.io.PrintWriter writer = new java.io.PrintWriter(settings, "UTF-8")) {
            writer.println("bot.level=Expert");
            writer.println("player.name=You");
        }
        final ChessFrame[] frame = new ChessFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            frame[0] = new ChessFrame(new Storage(settings));
            frame[0].setSize(1280, 800);
            frame[0].setLocation(0, 0);
            frame[0].setVisible(true);
        });
        Thread.sleep(1200);

        Robot robot = new Robot();
        robot.setAutoDelay(120);
        // e4 e5, Nf3 Nc6
        click(robot, frame[0], 4, 1);
        click(robot, frame[0], 4, 3);
        click(robot, frame[0], 4, 6);
        click(robot, frame[0], 4, 4);
        click(robot, frame[0], 6, 0);
        click(robot, frame[0], 5, 2);
        click(robot, frame[0], 1, 7);
        click(robot, frame[0], 2, 5);
        Thread.sleep(800);

        BufferedImage image = robot.createScreenCapture(new Rectangle(0, 0, 1280, 800));
        File out = new File(args.length > 0 ? args[0] : "ui.png");
        ImageIO.write(image, "png", out);
        System.out.println("wrote " + out.getAbsolutePath());
        System.exit(0);
    }

    /** Clicks on a square of the board. */
    private static void click(Robot robot, ChessFrame frame, int file, int rank) throws Exception {
        final BoardPanel[] board = new BoardPanel[1];
        SwingUtilities.invokeAndWait(() -> board[0] = frame.boardPanel());
        java.awt.Point location = board[0].getLocationOnScreen();
        int size = Math.min(board[0].getWidth(), board[0].getHeight()) / 8;
        int x = location.x + file * size + size / 2;
        int y = location.y + (7 - rank) * size + size / 2;
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(250);
    }
}
