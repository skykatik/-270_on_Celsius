package core;

import core.EventHandling.EventHandler;
import core.UI.GUI.Menu.MouseCalibration;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.awt.*;
import java.util.Arrays;

import static core.Window.addResource;
import static core.Window.glfwWindow;
import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    static final int PRESSED_ARRAY_SIZE = 349;
    static final int CLICKED_ARRAY_SIZE = 8; // GLFW_MOUSE_BUTTON_1 ~ GLFW_MOUSE_BUTTON_8

    private final long[] pressed, clicked;
    private final long[] justPressed, justClicked;
    private final Point mousePos = new Point(0, 0);

    private long lastMouseMoveTimestamp;

    public InputHandler() {
        justPressed = createBitSet(PRESSED_ARRAY_SIZE);
        justClicked = createBitSet(CLICKED_ARRAY_SIZE);

        pressed = createBitSet(PRESSED_ARRAY_SIZE);
        clicked = createBitSet(CLICKED_ARRAY_SIZE);
    }

    public void init() {
        glfwSetCursorPosCallback(glfwWindow, addResource(new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                double mouseX = xpos * MouseCalibration.xMultiplier;
                double mouseY = ypos / MouseCalibration.yMultiplier;
                double invertedY = EventHandler.height - mouseY;

                lastMouseMoveTimestamp = System.currentTimeMillis();
                mousePos.setLocation(mouseX, invertedY);
            }
        }));
        glfwSetKeyCallback(glfwWindow, addResource(new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                switch (action) {
                    case GLFW_PRESS -> {
                        setBit(pressed, key);
                        setBit(justPressed, key);
                    }
                    case GLFW_RELEASE -> {
                        unsetBit(pressed, key);
                        setBit(justPressed, key);
                    }
                }
            }
        }));
        glfwSetMouseButtonCallback(glfwWindow, addResource(new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                switch (action) {
                    case GLFW_PRESS -> {
                        setBit(clicked, button);
                        setBit(justClicked, button);
                    }
                    case GLFW_RELEASE -> {
                        unsetBit(clicked, button);
                        setBit(justClicked, button);
                    }
                }
            }
        }));
    }

    public void update() {
        Arrays.fill(justPressed, 0);
        Arrays.fill(justClicked, 0);

        glfwPollEvents();
    }

    // region Public API

    public long getLastMouseMoveTimestamp() {
        return lastMouseMoveTimestamp;
    }

    public Point mousePos() {
        return mousePos;
    }

    public boolean pressed(int keycode) {
        return isSet(pressed, keycode);
    }

    public boolean justPressed(int keycode) {
        return isSet(pressed, keycode) && isSet(justPressed, keycode);
    }

    public boolean clicked(int button) {
        return isSet(clicked, button);
    }

    public boolean justClicked(int button) {
        return isSet(clicked, button) && isSet(justClicked, button);
    }

    // endregion

    private static long[] createBitSet(int n) {
        return new long[((n - 1) >> 6) + 1];
    }

    private static void setBit(long[] bits, int i) {
        int idx = i >> 6;
        if (idx < 0 || idx >= bits.length)
            throw new IllegalArgumentException("Unexpected button: " + i);
        bits[idx] |= 1L << i;
    }

    private static void unsetBit(long[] bits, int i) {
        int idx = i >> 6;
        if (idx < 0 || idx >= bits.length)
            throw new IllegalArgumentException("Unexpected button: " + i);
        bits[idx] &= ~(1L << i);
    }

    private static boolean isSet(long[] bits, int i) {
        int idx = i >> 6;
        if (idx < 0 || idx >= bits.length)
            throw new IllegalArgumentException("Unexpected button: " + i);
        return (bits[idx] & (1L << i)) != 0;
    }
}
