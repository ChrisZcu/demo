package draw;

import org.lwjgl.opengl.awt.AWTGLCanvas;

import javax.swing.*;

public class CanvasRun extends Thread {
    AWTGLCanvas canvas;

    public CanvasRun(AWTGLCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void run() {
        if (!canvas.isValid())
            return;

        canvas.render();
        SwingUtilities.invokeLater(this);

    }
}
