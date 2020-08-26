import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class TextureUtil {
    private static final Texture errTexture = getErrTexture(); //when i delete this row, the rectangle is visible

    private static Texture getErrTexture() { //loads a default texture, which will be shown if some texture misses
        try {
            return TextureLoader.getTexture("PNG", new FileInputStream(new File("res/textures/err.png")));
        } catch (Exception e) {
            Display.destroy();
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public static void drawTexture(Texture texture, int x, int y, int width, int height) { //draws a texture on the given position
        texture.bind();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);        GL11.glVertex2i(x, y);
        GL11.glTexCoord2f(1, 0);        GL11.glVertex2i(x + width, y);
        GL11.glTexCoord2f(1, 1);        GL11.glVertex2i(x + width, y + height);
        GL11.glTexCoord2f(0, 1);        GL11.glVertex2i(x, y + height);
        GL11.glEnd();
    }

    public static void drawRect() { //draw some test-rect

        // set the color of the quad (R,G,B,A)
        GL11.glColor3f(10.5f,2.5f,1.0f);

        // draw quad
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(100,100);
        GL11.glVertex2f(100+200,100);
        GL11.glVertex2f(100+200,100+200);
        GL11.glVertex2f(100,100+200);
        GL11.glEnd();

    }
}