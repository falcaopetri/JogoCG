package br.ufscar.dc.cg.jogo;

import br.ufscar.dc.cg.jogo.audio.AudioTracks;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import static br.ufscar.dc.cg.jogo.audio.OpenALInfo.checkALCError;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC11.*;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALUtil;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
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

    long context;
    long device;

    AudioTracks audioTracks;

    /* Status flags */
    private final boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private boolean spaceKeyDown = false;
    private long lastShotTime = 0L;
    private double rotate = 1;
    private double down = 0;
    private boolean shot = false;
    private boolean paint = false;
    private boolean sound_on = true;
    private double disparo;
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
                    audioTracks.play(0);
                    game.next_level();
                }
                if (key == GLFW_KEY_R && action == GLFW_RELEASE) {
                    audioTracks.play(1);
                    game.reset_level();
                }
                if (key == GLFW_KEY_S && action == GLFW_RELEASE) {
                    sound_on = !sound_on;
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

        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        assertTrue(deviceCaps.OpenALC10);

        System.out.println("OpenALC10: " + deviceCaps.OpenALC10);
        System.out.println("OpenALC11: " + deviceCaps.OpenALC11);
        System.out.println("caps.ALC_EXT_EFX = " + deviceCaps.ALC_EXT_EFX);

        if (deviceCaps.OpenALC11) {
            List<String> devices = ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);
            if (devices == null) {
                checkALCError(NULL);
            } else {
                for (int i = 0; i < devices.size(); i++) {
                    System.out.println(i + ": " + devices.get(i));
                }
            }
        }

        String defaultDeviceSpecifier = alcGetString(NULL, ALC_DEFAULT_DEVICE_SPECIFIER);
        assertTrue(defaultDeviceSpecifier != null);
        System.out.println("Default device: " + defaultDeviceSpecifier);

        context = alcCreateContext(device, (IntBuffer) null);
        alcSetThreadContext(context);
        AL.createCapabilities(deviceCaps);

        System.out.println("ALC_FREQUENCY: " + alcGetInteger(device, ALC_FREQUENCY) + "Hz");
        System.out.println("ALC_REFRESH: " + alcGetInteger(device, ALC_REFRESH) + "Hz");
        System.out.println("ALC_SYNC: " + (alcGetInteger(device, ALC_SYNC) == ALC_TRUE));
        System.out.println("ALC_MONO_SOURCES: " + alcGetInteger(device, ALC_MONO_SOURCES));
        System.out.println("ALC_STEREO_SOURCES: " + alcGetInteger(device, ALC_STEREO_SOURCES));

        audioTracks = new AudioTracks("test.ogg", "test.ogg");
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
            //play source 0
            if (sound_on) {
                audioTracks.play(0);
            }
        }

        if (paint) {
            Polygon pol = game.getPolygon();
            /*int mi = 0;
            double maior = -2;
            for (int i = 0; i < pol._poly.size(); ++i) {
                Point p = pol._poly.get(i);
                double ny = Point.rotationY(p.getX(), p.getY(), rotate + 10);
                if (disparo == maior) {
                    colide = disparo;
                    colide += ny;
                    colide /= 2;
                    colide = 1 - Math.abs(colide);
                    colide *= -1;
                    if (colide > -0.5) {
                        colide = -0.5;
                    }
                }

                if (maior <= ny) {
                    maior = ny;
                    double nx = Point.rotationX(p.getX(), p.getY(), rotate + 10);
                    disparo = maior;// - Math.abs(nx)/2.0;
                    if (nx > 0.8) {
                        mi = i - 1;
                    } else {
                        mi = i;
                    }

                }
            }*/
            int mi = pol.intersectAfterRotation(rotate);
            System.out.println("intersects " + mi);

            game.do_move(mi);

            paint = false;
        }

        if (game.getState() == GameState.NEXT_LEVEL) {
            game.next_level();
            double random = Math.random();
            if (random < 0.5) {
                ROTATION_ORIENTATION = -ROTATION_ORIENTATION;
            }
        }

        if (/*!shot &&*/game.getState() == GameState.PLAYING) {
            rotate = (rotate + ROTATION_ORIENTATION * (ROTATION_BASE_INCREMENT + ROTATION_INCREMENT) * (1 + game.getLevel() / 10)) % 360;
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

            audioTracks.close();

            alcDestroyContext(context);
            alcCloseDevice(device);
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
            //System.out.println(colide);
            //if (down < colide) {
            if (down < -1) {
                shot = false; // trocar para colide
                //stop source 0
                //alSourceStop(source);
                //checkALError();
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

    private void assertTrue(boolean OpenALC10) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
