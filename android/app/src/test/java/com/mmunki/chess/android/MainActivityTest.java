package com.mmunki.chess.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.mmunki.chess.core.Game;
import com.mmunki.chess.core.Piece;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/**
 * The checks of the game on the phone.
 *
 * <p>They start the game and they draw the board, so that a game that does
 * not start or a board that stays empty is noticed.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MainActivityTest {

    @Test
    public void theGameStarts() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertNotNull(activity);
        BoardView board = find(activity.getWindow().getDecorView(), BoardView.class);
        assertNotNull("the board is not on the screen", board);
        assertEquals("the game does not start with the pieces of the start position",
                32, pieces(new Game()));
    }

    @Test
    public void theBoardIsDrawn() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        BoardView board = find(activity.getWindow().getDecorView(), BoardView.class);
        assertNotNull("the board is not on the screen", board);
        board.measure(320, 320);
        board.layout(0, 0, 320, 320);

        Bitmap bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
        board.draw(new Canvas(bitmap));

        assertTrue("the board has no squares", hasColor(bitmap, Theme.BOARD_LIGHT));
        assertTrue("the board has no squares", hasColor(bitmap, Theme.BOARD_DARK));
        assertTrue("the pieces are not on the board", hasColor(bitmap, Theme.PIECE_WHITE));
        assertTrue("the pieces are not on the board", hasColor(bitmap, Theme.PIECE_BLACK));
    }

    @Test
    public void everyPieceIsDrawn() {
        for (Piece piece : Piece.values()) {
            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            PiecePainter.draw(new Canvas(bitmap), piece,
                    com.mmunki.chess.core.Color.WHITE, 0, 0, 100);
            assertTrue("the piece " + piece + " is not drawn",
                    hasColor(bitmap, Theme.PIECE_WHITE));
        }
    }

    /** The number of pieces on the board. */
    private static int pieces(Game game) {
        int count = 0;
        for (int square = 0; square < 64; square++) {
            if (game.board().pieceAt(square) != null) {
                count++;
            }
        }
        return count;
    }

    /** True when the picture has a pixel of the colour. */
    private static boolean hasColor(Bitmap bitmap, int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                if (Math.abs(Color.red(pixel) - red) < 12
                        && Math.abs(Color.green(pixel) - green) < 12
                        && Math.abs(Color.blue(pixel) - blue) < 12) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The first view of the kind in the tree of the windows. */
    private static <T extends View> T find(View view, Class<T> kind) {
        if (kind.isInstance(view)) {
            return kind.cast(view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                T found = find(group.getChildAt(i), kind);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
