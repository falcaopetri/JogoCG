package br.ufscar.dc.cg.jogo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GLCapabilities;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author petri
 */
public class GameGUI {

    private Game game;
    private Polygon arrow;

    private long window;
    private static int WIDTH = 600;
    private static int HEIGHT = 600;
    private static int shotMilliseconds = 500;

    /*
        Status flags
     */
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private boolean spaceKeyDown = false;
    private long lastShotTime = 0L;
    private float rotate = 1;
    private float down = 0;

    /*
        Callbacks
     */
    private GLFWKeyCallback keyCallback;

    private void init() throws IOException {

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        arrow = new Polygon(3);
        arrow.add(-0.1f, 1f);
        arrow.add(0.1f, 1f);
        arrow.add(0.0f, 0.8f);

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_UNKNOWN) {
                    return;
                }
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (action == GLFW_PRESS/*|| action == GLFW_REPEAT*/) {
                    keyDown[key] = true;
                } else {
                    keyDown[key] = false;
                }
            }
        });

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            update();
            render();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void update() {
        long thisTime = System.nanoTime();

        updateControls();

        /* Let the player shoot */
        if (spaceKeyDown && (thisTime - lastShotTime >= 1E6 * shotMilliseconds)) {
            game.do_move();
            System.out.println("shot executed");
            lastShotTime = thisTime;
        }
    }

    private void updateControls() {
        spaceKeyDown = keyDown[GLFW_KEY_SPACE];
    }

    private void render() {
        //glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        drawPolygon();
        drawCursor();
    }

    public void run() {
        try {
            game = new Game();

            init();
            loop();

            keyCallback.free();
            glfwDestroyWindow(window);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void drawPolygon() {
        glPushMatrix();
        glColor3f(0.0f, 0.0f, 1.0f);
        glTranslatef(0, -0.1f, 0.0f);
        glRotated(rotate, 0.0, 0.0, 1.0);
        glBegin(GL_POLYGON);
        glColor3f(1.0f, 0.0f, 1.0f);
        glVertex2f(-0.3f, -0.3f);
        glColor3f(1.0f, 0.0f, 1.0f);
        glVertex2f(0.3f, -0.3f);
        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex2f(0.3f, 0.3f);
        glVertex2f(-0.3f, 0.3f);
        glEnd();
        rotate += 0.5;
        glPopMatrix();
    }

    private void drawCursor() {
        glPushMatrix();
        glColor3f(0.0f, 1.0f, 0.0f);
        glTranslatef(0, down, 0.0f);
        glBegin(GL_POLYGON);
        glVertex2f(-0.1f, 1);
        glVertex2f(0.1f, 1);
        glVertex2f(0f, 0.8f);
        glEnd();
        glPopMatrix();
        down -= 0.05;
        if (down < -1) {
            down = 0;
        }
    }
}
