package br.ufscar.dc.cg.jogo;

import java.io.IOException;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameGUI {

    private Scene scene;
    private final Game game;
    private final Polygon arrow;

    private long window;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int SHOT_DEBOUNCE_DELAY = 200; // in milliseconds
    private static final double SHOT_INCREMENT = 0.09;
    private static double ROTATION_INCREMENT = 0.2;
    private static int ROTATION_ORIENTATION = 1;
    private static final double ROTATION_BASE_INCREMENT = 0.5;

    /* Status flags */
    private boolean spaceKeyDown = false;
    private long lastShotTime = 0L;
    private double rotate = 1;
    private double down = 0;
    private boolean shot = false;

    /* Callbacks*/
    private GLFWKeyCallback keyCallback;

    public GameGUI() {
        arrow = new Polygon();
        arrow.add(-0.1f, 1f);
        arrow.add(0.1f, 1f);
        arrow.add(0.0f, 0.8f);

        game = new Game();
        scene = Scene.HOME;
    }

    private void init() throws IOException {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "$NOME", NULL, NULL);
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
                    if (scene == Scene.HOME) {
                        glfwSetWindowShouldClose(window, true);
                    } else {
                        scene = Scene.HOME;
                        game.reset();
                    }
                }

                switch (scene) {
                    case GAME:
                        processGameKeys(key, action);
                        break;
                    case HOME:
                        processHomeKeys(key, action);
                        break;
                    case INSTRUCTIONS_1:
                        processInstructionsKeys(key, action);
                        break;
                    case INSTRUCTIONS_2:
                        processInstructionsKeys(key, action);
                        break;
                }
            }

            private void processGameKeys(int key, int action) {
                switch (key) {
                    case GLFW_KEY_N:
                        if (game.getState() == GameState.PLAYING && action == GLFW_RELEASE) {
                            game.next_level();
                        }
                        break;
                    case GLFW_KEY_R:
                        if (game.getState() == GameState.PLAYING && action == GLFW_RELEASE) {
                            game.reset_level();
                        }
                        break;
                    case GLFW_KEY_Q:
                        if (game.getState() == GameState.PLAYING && action == GLFW_RELEASE) {
                            ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
                            ROTATION_INCREMENT += 0.05;
                        }
                        break;
                    case GLFW_KEY_P:
                        if (action == GLFW_RELEASE) {
                            if (game.getState() == GameState.PLAYING) {
                                game.pause();
                            } else {
                                game.resume();
                            }
                        }
                        break;
                    case GLFW_KEY_SPACE:
                        if (game.getState() == GameState.PLAYING) {
                            spaceKeyDown = (action == GLFW_PRESS);
                        }
                }
            }

            private void processHomeKeys(int key, int action) {
                if (action != GLFW_RELEASE) {
                    return;
                }

                if (key == GLFW_KEY_S) {
                    scene = Scene.GAME;
                } else if (key == GLFW_KEY_A) {
                    scene = Scene.ABOUT;
                } else if (key == GLFW_KEY_C) {
                    scene = Scene.INSTRUCTIONS_1;
                }
            }

            private void processInstructionsKeys(int key, int action) {
                if (action != GLFW_RELEASE) {
                    return;
                }

                if (scene == Scene.INSTRUCTIONS_1 && key == GLFW_KEY_N) {
                    scene = Scene.INSTRUCTIONS_2;
                }

                if (scene == Scene.INSTRUCTIONS_2 && key == GLFW_KEY_P) {
                    scene = Scene.INSTRUCTIONS_1;
                }
            }
        });

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
        /* Let the player shoot */
        if (spaceKeyDown && (thisTime - lastShotTime >= 1E6 * SHOT_DEBOUNCE_DELAY)) {
            lastShotTime = thisTime;
            shot = true;
            spaceKeyDown = false;

            // do shot
            Polygon pol = game.getPolygon();
            int mi = pol.intersectAfterRotation(rotate);

            game.do_move(mi);
        }

        if (game.getState() == GameState.NEXT_LEVEL) {
            game.next_level();
            double random = Math.random();
            if (random < 0.5) {
                ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
            }
        }

        if (game.getState() == GameState.PLAYING) {
            // orientação * velocidade * taxa
            rotate += ROTATION_ORIENTATION * (ROTATION_BASE_INCREMENT + ROTATION_INCREMENT) * (1 + game.getLevel() / 30);
            rotate %= 360;
        }

        // Atualiza o deslocamento vertical da seta
        if (shot) {
            down -= SHOT_INCREMENT;
            if (down < -1) {
                shot = false;
                down = 0;
            }
        }
    }

    private void render() {
        drawPolygon();
        drawCursor();
        drawInfos();
    }

    public void run() {
        try {
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
        glBegin(GL_POLYGON);
        glColor3d(1, 1, 0);
        for (int ii = 0; ii < 100; ii++) {
            double theta = 2.0f * Math.PI * ii / 100;
            double x = r * Math.cos(theta); //calculate the x component 
            double y = r * Math.sin(theta); //calculate the y component 
            glVertex2d(x + cx, y + cy); //output vertex 
        }
        glEnd();
    }

    private void drawPolygon() {
        Polygon pol = game.getPolygon();

        glPushMatrix();
        glTranslated(0.0, -0.12, 0.0);
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

        glTranslated(0.0, down, 0.0);
        glColor3d(0.0, 1.0, 0.0);

        glBegin(GL_POLYGON);
        for (Point i : arrow._poly) {
            glVertex2d(i.getX(), i.getY());
        }
        glEnd();

        glPopMatrix();
    }

    private void drawInfos() {
        if (scene == Scene.GAME) {
            Text.drawString("Nivel:", -8, 7.5f, 0.45f, 3f);
            Text.drawString(Integer.toString(game.getLevel()), -4.5f, 7.5f, 0.45f, 1f);

            Text.drawString("Lados:", 4, 7.5f, 0.45f, 3f);
            Text.drawString(Integer.toString(game.getCount_edges()), 8f, 7.5f, 0.45f, 1f);
        } else if (scene == Scene.HOME) {
            Text.drawString("$NOME", -2f, 1, 0.7f, 3f);
            Text.drawString("(s)tart", -1, -3f, 0.5f, 3f);
            Text.drawString("(c)ontrols", -1, -5f, 0.5f, 3f);
            Text.drawString("(a)bout", -1, -7f, 0.5f, 3f);
        } else if (scene == Scene.INSTRUCTIONS_1) {
            Text.drawString("instructions 1", -2f, 1, 0.4f, 3f);
        } else if (scene == Scene.INSTRUCTIONS_2) {
            Text.drawString("instructions 2", -2f, 1, 0.4f, 3f);
        } else if (scene == Scene.ABOUT) {
            Text.drawString("about", -2f, 1, 0.7f, 3f);
        }
    }
}
