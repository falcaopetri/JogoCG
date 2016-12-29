package br.ufscar.dc.cg.jogo;

import java.io.IOException;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
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
    private boolean shot = false;
    private boolean paint = false;
    /*testeeeeeeeeeee
     */
    // TODO corR deve ser do tamanho do polígono, ou seja, atualizado constantemente
    private float corR[] = new float[8];
    private int ite;

    /*
        Callbacks
     */
    private GLFWKeyCallback keyCallback;

    private void init() throws IOException {
        corR[0] = 0.0f;
        corR[1] = 0.0f;
        corR[2] = 0.0f;
        corR[3] = 0.0f;
        corR[4] = 0.0f;
        corR[5] = 0.0f;
        corR[6] = 0.0f;
        corR[7] = 0.0f;
        ite = 0;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        arrow = new Polygon();
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
                if (key == GLFW_KEY_N && action == GLFW_RELEASE) {
                    game.next_level();
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
            shot = true;
            paint = true;
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
        Polygon pol = game.getPolygon();
        System.out.println("p: " + pol._poly.size());

        ite = (ite + 1) % pol._poly.size();

        //System.out.println(ite);
        double maior = -2;
        int mi = 0;
        glPushMatrix();
        glRotated(rotate, 0.0, 0.0, 1.0);
        glTranslatef(-pol._gravity_center.getX(), -pol._gravity_center.getY(), 0.0f);
        glBegin(GL_POLYGON);

        //System.out.println(rotate);
        for (int i = 0; i < pol._poly.size(); i++) {
            Point p = pol._poly.get(i);
            glColor3f(corR[i], 0.0f, 1.0f);
            glVertex2f(p.getX(), p.getY());
            //System.out.println(ite);

            double ny = rotationY(p.getX(), p.getY(), rotate);
            if (maior <= ny) {
                maior = ny;
                double nx = rotationX(p.getX(), p.getY(), rotate);
                if (nx > 0.8) {
                    mi = i - 1;
                } else {
                    mi = i;
                }

                if (mi < 0) {
                    mi = pol._poly.size();
                }
            }

        }

        if (paint) {
            //double nx = rotationX(i.getX(),i.getY(),rotate);
            //double ny = rotationY(i.getX(),i.getY(),rotate);
            //pol._edges_states.set(ite, true);               
            corR[mi] = 0.7f;
            //corR[(ite + 1) % 4] = 0.7f;
            //System.out.println(mi);
            paint = false;
        }
        glEnd();
        rotate += 0.7;
        rotate = (rotate + 0.7f) % 360;

        glPopMatrix();
    }

    private double rotationX(float x, float y, double ang) {
        double rad = ang * Math.PI / 180;
        double nx = cos(rad) * x + sin(rad) * -1 * y;
        return nx;
    }

    private double rotationY(float x, float y, double ang) {
        double rad = ang * Math.PI / 180;
        double ny = sin(rad) * x + cos(rad) * y;
        return ny;
    }

    private void drawCursor() {
        glPushMatrix();
        glColor3f(0.0f, 1.0f, 0.0f);
        glTranslatef(0, down, 0.0f);
        glBegin(GL_POLYGON);
        for (Point i : arrow._poly) {
            glVertex2f(i.getX(), i.getY());
        }
        glEnd();
        glPopMatrix();
        if (shot) {
            down -= 0.05;
            if (down < -1) {
                shot = false; // trocar para colide
            }
        } else {
            down = 0;
        }
    }
}
