package br.ufscar.dc.cg.jogo;

import java.io.IOException;
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
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int SHOT_DEBOUNCE_DELAY = 200; // in milliseconds
    private static final double SHOT_INCREMENT = 0.09;
    private static double ROTATION_INCREMENT = 0.2;
    private static int ROTATION_ORIENTATION = 1;
    private static final double ROTATION_BASE_INCREMENT = 0.5;

    /* Status flags */
    private final boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private boolean spaceKeyDown = false;
    private long lastShotTime = 0L;
    private double rotate = 1;
    private double down = 0;
    private boolean shot = false;
    private boolean paint = false;
    private double colide;

    /* Callbacks*/
    private GLFWKeyCallback keyCallback;

    private void init() throws IOException {
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
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
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
                if (key == GLFW_KEY_Q && action == GLFW_RELEASE) {
                    ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
                    ROTATION_INCREMENT += 0.1;
                }
                if (key == GLFW_KEY_P && action == GLFW_RELEASE) {
                    if (game.getState() == GameState.PLAYING) {
                        game.pause();
                    } else {
                        game.resume();
                    }
                }
                keyDown[key] = (action == GLFW_PRESS);
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
            System.out.println("shot executed");
            lastShotTime = thisTime;
            shot = true;
            paint = true;
        }

        if (paint) {
            Polygon pol = game.getPolygon();
            int mi = pol.intersectAfterRotation(rotate);
            colide = pol.colide;
            System.out.println(colide);

            pol._poly.get(mi).color = new RGBColor(Point.DEFAULT_COLOR3);
            int next_vertex = (mi + 1) % pol._poly.size();
            pol._poly.get(next_vertex).color = new RGBColor(Point.DEFAULT_COLOR3);
            //System.out.println(mi);
            paint = false;
        }

        if (!shot && game.getState() == GameState.PLAYING) {
            rotate += 0.7;
            rotate = (rotate + 0.7f) % 360;
        }

    }

    private void updateControls() {
        spaceKeyDown = keyDown[GLFW_KEY_SPACE];
    }

    private void render() {
        drawPolygon();
        drawCursor();
        drawInfos();
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

        glBegin(GL_POLYGON);
        glColor3d(1, 1, 0);
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

        glPushMatrix();
        glRotated(rotate, 0.0, 0.0, 1.0);

        glBegin(GL_TRIANGLE_FAN);

        glColor3d(Point.DEFAULT_COLOR1.R, Point.DEFAULT_COLOR1.G, Point.DEFAULT_COLOR1.B);
        glVertex2d(0.0, 0.0); //center of triangles
        for (int i = 0; i < pol._poly.size() + 1; ++i) {
            Point p = pol._poly.get((i + 1) % pol._poly.size());
            glColor3d(p.color.R, p.color.G, p.color.B);
            glVertex2d(p.getX(), p.getY());
        }

        glEnd();
        for (Point p : pol._poly) {
            drawCircle(p.getX(), p.getY(), 0.01);
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
            
            System.out.println(down);
            //if (down < colide) {
            if (0.75+down < colide) {
                shot = false; // trocar para colide
                /*try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }*/
            }
        } else {
            down = 0;
        }
    }

    private void drawInfos() {
        Text.drawString("Nivel:", -8, 7.5f, 0.45f, 3f);
        Text.drawString(Integer.toString(game.getLevel()), -4.5f, 7.5f, 0.45f, 1f);

        Text.drawString("Lados:", 4, 7.5f, 0.45f, 3f);
        Text.drawString(Integer.toString(game.getCount_edges()), 8f, 7.5f, 0.45f, 1f);
    }
}
