package br.ufscar.dc.cg.jogo;

import java.io.IOException;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameGUI {

    private Game game;
    private Polygon arrow;

    private long window;
    private static int WIDTH = 600;
    private static int HEIGHT = 600;
    private static int SHOT_DEBOUNCE_DELAY = 500; // in milliseconds
    private static double SHOT_INCREMENT = 0.04;

    /* Status flags */
    private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private boolean spaceKeyDown = false;
    private long lastShotTime = 0L;
    private double rotate = 1;
    private double down = 0;
    private boolean shot = false;
    private boolean paint = false;
    /* testeeeeeeeeeee */
    private int ite;

    /*
        Callbacks
     */
    private GLFWKeyCallback keyCallback;

    private void init() throws IOException {
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
                if (key == GLFW_KEY_R && action == GLFW_RELEASE) {
                    game.reset_level();
                }
                if (key == GLFW_KEY_P && action == GLFW_RELEASE) {
                    if (game.getState() == GameState.PLAYING) {
                        game.pause();
                    } else {
                        game.resume();
                    }
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
        if (spaceKeyDown && (thisTime - lastShotTime >= 1E6 * SHOT_DEBOUNCE_DELAY)) {
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

    void drawCircle(double cx, double cy, double r) {
        int num_segments = 100;

        glBegin(GL_LINE_LOOP);
        for (int ii = 0; ii < num_segments; ii++) {
            double theta = 2.0f * Math.PI * ii / num_segments; //get the current angle 
            double x = r * Math.cos(theta); //calculate the x component 
            double y = r * Math.sin(theta); //calculate the y component 
            glVertex2d(x + cx, y + cy); //output vertex 
        }
        glEnd();
    }

    private void drawPolygon() {
        Polygon pol = game.getPolygon();

        ite = (ite + 1) % pol._poly.size();

        if (paint) {
            //double nx = rotationX(i.getX(),i.getY(),rotate);
            //double ny = rotationY(i.getX(),i.getY(),rotate);
            int mi = pol.intersectAfterRotation(rotate);
            System.out.println("intersects " + mi);

            pol._poly.get(mi).color.R = 0.7f;
            int next_vertex = (mi + 1) % pol._poly.size();
            pol._poly.get(next_vertex).color.R = 0.7f;
            //System.out.println(mi);
            paint = false;
        }
        //shot = true;
        //System.out.println(ite);
        //double maior = -2;
        //int mi = 0;
        glPushMatrix();
        glRotated(rotate, 0.0, 0.0, 1.0);
        //glTranslated(-pol._gravity_center.getX(), -pol._gravity_center.getY(), 0.0);
        glBegin(GL_POLYGON);

        //System.out.println(rotate);
        for (int i = 0; i < pol._poly.size(); i++) {
            Point p = pol._poly.get(i);
            glColor3d(p.color.R, p.color.G, p.color.B);
            glVertex2d(p.getX(), p.getY());
            //System.out.println(ite);
        }
        glEnd();

        if (!shot && game.getState() == GameState.PLAYING) {
            rotate += 0.7;
            rotate = (rotate + 0.7f) % 360;
        }

        for (int i = 0; i < pol._poly.size(); i += 2) {
            Point p = pol._poly.get(i);
            glColor3d(i * 1.0 / pol._poly.size(), 0.1, 0.1);
            drawCircle(p.getX(), p.getY(), 0.05f);

            p = pol._poly.get((i + 1) % pol._poly.size());
            glColor3d(i * 1.0 / pol._poly.size(), 0.1, 0.1);
            drawCircle(p.getX(), p.getY(), 0.05f);
        }

        glPopMatrix();
    }

    private void drawCursor() {
        glPushMatrix();
        glColor3d(0.0, 1.0, 0.0);
        glTranslated(0.0, down, 0.0);
        glBegin(GL_POLYGON);
        for (Point i : arrow._poly) {
            glVertex2d(i.getX(), i.getY());
        }
        glEnd();
        glPopMatrix();
        if (shot) {
            down -= SHOT_INCREMENT;
            if (down < -1) {
                shot = false; // trocar para colide
            }
        } else {
            down = 0;
        }
    }
}
