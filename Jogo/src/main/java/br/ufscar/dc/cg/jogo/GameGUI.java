package br.ufscar.dc.cg.jogo;

import br.ufscar.dc.cg.jogo.audio.AudioTracks;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import static br.ufscar.dc.cg.jogo.audio.OpenALInfo.checkALCError;
import java.util.Random;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC11.*;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALUtil;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GameGUI {

    private Scene scene;
    private final Game game;
    private final Polygon arrow;
    private static final Random gerador = new Random();

    private long window;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int SHOT_DEBOUNCE_DELAY = 200; // in milliseconds
    private static final double SHOT_INCREMENT = 0.09;
    private static double ROTATION_INCREMENT = 0.2;
    private static int ROTATION_ORIENTATION = 1;
    private static final double ROTATION_BASE_INCREMENT = 0.5;

    long context;
    long device;

    AudioTracks audioTracks;

    /* Status flags */
    private boolean spaceKeyDown = false;
    private long lastShotTime = 0L;
    private double rotate = 1;
    private double down = 0;
    private boolean shot = false;
    private boolean sound_on = true;
    private double colide;
    private int Mscore = 0;

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
        window = glfwCreateWindow(WIDTH, HEIGHT, "Touch", NULL, NULL);
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
                            if (game.getLevel() < 100) {
                                game.next_level();
                            }
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
                    case GLFW_KEY_LEFT_SHIFT:
                        if (action == GLFW_PRESS && !game.getCooldown().running) {
                            //ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
                            game.getCooldown().start();
                        } else if (action == GLFW_RELEASE) {
                            game.getCooldown().stop();
                            if (game.getCooldown().hasReset()) {
                                ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
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
                if (key == GLFW_KEY_N && action == GLFW_RELEASE) {
                    audioTracks.play(0);
                    game.next_level();
                } else if (key == GLFW_KEY_R && action == GLFW_RELEASE) {
                    audioTracks.play(1);
                    game.reset_level();
                } else if (key == GLFW_KEY_O && action == GLFW_RELEASE) {
                    sound_on = !sound_on;
                } else if (key == GLFW_KEY_Q && action == GLFW_RELEASE) {
                    ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
                    ROTATION_INCREMENT += 0.1;
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

                if (scene == Scene.INSTRUCTIONS_1) {
                    if (key == GLFW_KEY_C) {
                        scene = Scene.INSTRUCTIONS_2;
                    }
                    if (key == GLFW_KEY_P) {
                        scene = Scene.HOME;
                    }
                } else if (scene == Scene.INSTRUCTIONS_2) {
                    if (key == GLFW_KEY_P) {
                        scene = Scene.INSTRUCTIONS_1;
                    }
                    if (key == GLFW_KEY_C) {
                        scene = Scene.HOME;
                    }
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

        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

//        assertTrue(deviceCaps.OpenALC10);
//        System.out.println("OpenALC10: " + deviceCaps.OpenALC10);
//        System.out.println("OpenALC11: " + deviceCaps.OpenALC11);
//        System.out.println("caps.ALC_EXT_EFX = " + deviceCaps.ALC_EXT_EFX);
        if (deviceCaps.OpenALC11) {
            List<String> devices = ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);
            if (devices == null) {
                checkALCError(NULL);
            } else {
//                for (int i = 0; i < devices.size(); i++) {
//                    System.out.println(i + ": " + devices.get(i));
//                }
            }
        }

//        String defaultDeviceSpecifier = alcGetString(NULL, ALC_DEFAULT_DEVICE_SPECIFIER);
//        assertTrue(defaultDeviceSpecifier != null);
//        System.out.println("Default device: " + defaultDeviceSpecifier);
        context = alcCreateContext(device, (IntBuffer) null);
        alcSetThreadContext(context);
        AL.createCapabilities(deviceCaps);

//        System.out.println("ALC_FREQUENCY: " + alcGetInteger(device, ALC_FREQUENCY) + "Hz");
//        System.out.println("ALC_REFRESH: " + alcGetInteger(device, ALC_REFRESH) + "Hz");
//        System.out.println("ALC_SYNC: " + (alcGetInteger(device, ALC_SYNC) == ALC_TRUE));
//        System.out.println("ALC_MONO_SOURCES: " + alcGetInteger(device, ALC_MONO_SOURCES));
//        System.out.println("ALC_STEREO_SOURCES: " + alcGetInteger(device, ALC_STEREO_SOURCES));
        audioTracks = new AudioTracks("music.ogg", "a.ogg", "b.ogg", "c.ogg", "d.ogg", "e.ogg", "f.ogg", "g.ogg");

        audioTracks.setGain(0, 1);
        for (int i = 1; i < 8; ++i) {
            audioTracks.setGain(i, 0.5f);
        }
        audioTracks.setToLoop(0);
        audioTracks.play(0);
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
            if (game.getState() == GameState.GAME_OVER) {
                game.reset();
                shot = false;
                scene = Scene.HOME;
            } else if (scene == Scene.GAME) {
                update();
                render();
            } else if (scene == Scene.HOME) {
                drawHome();
            } else if (scene == Scene.INSTRUCTIONS_1) {
                drawInstructions1();
            } else if (scene == Scene.INSTRUCTIONS_2) {
                drawInstructions2();
            } else if (scene == Scene.ABOUT) {
                drawAbout();
            }

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

            //play source 0
            if (sound_on) {
                int numero = gerador.nextInt(6);

                audioTracks.play(numero + 1);
            }

            // do shot
            Polygon pol = game.getPolygon();
            int mi = pol.intersectAfterRotation(rotate);
            colide = pol.colide;

            game.do_move(mi);
        }

        if (game.getState() == GameState.NEXT_LEVEL && game.getLevel() < 100) {
            game.next_level();

            double random = Math.random();
            if (random < 0.5) {
                ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
            }
        }
        if (game.getLevel() > Mscore) {
            Mscore = game.getLevel();
        }
        if (game.getState() == GameState.PLAYING) {
//            if (game.getCooldown().running) {
//                rotate += ROTATION_ORIENTATION * ROTATION_BASE_INCREMENT;
//            } else 
            if (game.getCooldown().hasReset()) {
                ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
            }
            {
                rotate += ROTATION_ORIENTATION * (ROTATION_BASE_INCREMENT + ROTATION_INCREMENT) * (1 + game.getLevel() / 20) * (game.getCooldown().curr_value + 0.2);
            }
            rotate %= 360;
        }

        // Atualiza o deslocamento vertical da seta
        if (shot) {
            down -= SHOT_INCREMENT;

            if (0.87 + down < colide) {
                shot = false;
            }
        } else {
            down = Math.min(0, down + SHOT_INCREMENT);
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
            game.getCooldown().timer.cancel();
            glfwTerminate();

            audioTracks.close();

            alcDestroyContext(context);
            alcCloseDevice(device);
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

    private void drawHome() {
        Text.drawString("Touch", -2f, 4, 0.7f, 2f);
        Text.drawString("s  to  start", -3f, 1f, 0.5f, 2f);
        Text.drawString("c  to  controls", -3f, -1f, 0.5f, 2f);
        Text.drawString("a  to  about", -3f, -3f, 0.5f, 2f);
        Text.drawString("Best score: ", -6f, -6f, 0.5f, 2.1f);
        Text.drawString(Integer.toString(Mscore), 3, -6f, 0.5f, 2.1f);
    }

    private void drawInstructions1() {
        Text.drawString("Teclas", -1.5f, 5, 0.6f, 2f);
        Text.drawString("espaco                 tiro", -5.5f, 3, 0.5f, 1.5f);
        Text.drawString("shift         modo  lento", -5.5f, 0, 0.5f, 1.5f);
        Text.drawString("esq                   voltar", -5.5f, -2.5f, 0.5f, 1.5f);
        Text.drawString("P   p r e vio u s        C   c o n tin u e", -8f, -7f, 0.42f, 1.3f);
    }

    private void drawInstructions2() {
        Text.drawString("Objetivo", -2f, 5, 0.6f, 2f);
        Text.drawString("Acerte todas as arestas", -6f, 2, 0.5f, 1.5f);
        Text.drawString("uma unica vez", -6f, 0, 0.5f, 1.5f);
        Text.drawString("P   p r e vio u s        C   c o n tin u e", -8f, -7f, 0.42f, 1.3f);
    }

    private void drawAbout() {
        Text.drawString("About", -1.5f, 5, 0.6f, 2f);
        Text.drawString("Trabalho  de  Computacao", -7f, 2, 0.5f, 1.5f);
        Text.drawString("grafica    2016  UFS C a r ", -7f, 0, 0.5f, 1.5f);
        Text.drawString("Anotino Falcao Petri", -7f, -4f, 0.5f, 1.5f);
        Text.drawString("Thiago Yonamine", -7f, -6f, 0.5f, 1.5f);
    }

    private void drawInfos() {

        Text.drawString("Niv el:", -8, 7.5f, 0.45f, 2f);
        Text.drawString(Integer.toString(game.getLevel()), -4.5f, 7.5f, 0.45f, 1f);

        Text.drawString("La d o s:", 4, 7.5f, 0.45f, 2f);
        Text.drawString(Integer.toString(game.getCount_edges()), 8f, 7.5f, 0.45f, 1f);

        Text.drawString("Coold ow n:", -8, -7f, 0.45f, 1.5f);
        Text.drawString(String.format("%.2f", game.getCooldown().curr_value), -2.0f, -7f, 0.45f, 1f);
    }
}
