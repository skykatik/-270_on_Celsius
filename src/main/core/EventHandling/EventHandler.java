package core.EventHandling;

import core.EventHandling.Logging.Config;
import core.EventHandling.Logging.Json;
import core.EventHandling.Logging.Logger;
import core.UI.API.Element;
import core.UI.GUI.CreateElement;
import core.UI.GUI.Menu.MouseCalibration;
import core.UI.GUI.Menu.Pause;
import core.UI.GUI.Menu.Settings;
import core.UI.GUI.Objects.ButtonObject;
import core.UI.GUI.Objects.SliderObject;
import core.Utils.SimpleColor;
import core.World.Creatures.Player.Player;
import core.World.WorldGenerator;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import java.awt.*;

import static core.EventHandling.Logging.Logger.log;
import static core.UI.GUI.CreateElement.*;
import static core.Utils.Commandline.updateLine;
import static core.Window.*;
import static core.World.Creatures.Physics.updates;
import static org.lwjgl.glfw.GLFW.*;

public class EventHandler extends Thread {
    public static long lastMouseMovedTime = System.currentTimeMillis();
    public static boolean mouseMoved;
    public static final Point prevMousePos = new Point();
    private static long lastSecond = System.currentTimeMillis();
    public static final Point mousePos = new Point(0, 0);
    private static boolean keyLogging = false;
    public static String keyLoggingText = "";
    private static final boolean[] pressedButtons = new boolean[349];
    private static int handlerUpdates = 0;
    public static int width, height; // TODO: scaling

    public EventHandler() {
        log("Thread: Event handling started");

        glfwSetCursorPosCallback(glfwWindow, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                double mouseX = xpos * MouseCalibration.xMultiplier;
                double mouseY = ypos / MouseCalibration.yMultiplier;
                double invertedY = height - mouseY;

                lastMouseMovedTime = System.currentTimeMillis();
                mousePos.setLocation(mouseX, invertedY);
            }
        });
        glfwSetWindowSizeCallback(glfwWindow, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                EventHandler.width = width;
                EventHandler.height = height;
            }
        });
        glfwSetKeyCallback(glfwWindow, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_F4 && mods == GLFW.GLFW_MOD_ALT) {
                    Logger.logExit(1863);
                }
            }
        });
    }

    public static void startKeyLogging() {
        keyLogging = true;
    }

    public static void endKeyLogging() {
        keyLoggingText = "";
        keyLogging = false;
    }

    public static Point getMousePos() {
        return mousePos;
    }

    public static boolean getKey(int key) {
        return glfwGetKey(glfwWindow, key) == 1;
    }

    public static boolean getKeyClick(int key) {
        if (!pressedButtons[key] && glfwGetKey(glfwWindow, key) == 1) {
            pressedButtons[key] = true;
            return true;
        } else if (!getKey(key)) {
            pressedButtons[key] = false;
            return false;
        }

        return false;
    }

    public static boolean getMousePress() {
        return glfwGetMouseButton(glfwWindow, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
    }

    public static boolean isMousePressed(Element button) {
        return getRectanglePress(button.x(), button.y(), button.x() + button.width(), button.y() + button.height());
    }

    public static boolean getRectanglePress(int x, int y, int x1, int y1) {
        Point mousePos = getMousePos();

        return mousePos.x >= x && mousePos.x <= x1 && mousePos.y >= y && mousePos.y <= y1 && getMousePress();
    }

    private static void updateSliders() {
        for (SliderObject slider : sliders.values()) {
            if (!slider.visible) {
                slider.isClicked = false;
                continue;
            }

            if (EventHandler.getRectanglePress(slider.x, slider.y, slider.width + slider.x, slider.height + slider.y)) {
                slider.sliderPos = EventHandler.getMousePos().x;
            }
        }
    }

    private static void updateButtons() {
        for (ButtonObject button : buttons.values()) {
            if (button == null || !button.visible || !button.isClickable) {
                if (button != null) {
                    button.isClicked = false;
                }
                continue;
            }

            if (button.swapButton) {
                if (System.currentTimeMillis() - button.lastClickTime >= 150 && EventHandler.getRectanglePress(button.x, button.y, button.width + button.x, button.height + button.y)) {
                    button.isClicked = !button.isClicked;
                    button.lastClickTime = System.currentTimeMillis();
                }
            } else {
                boolean press = EventHandler.getRectanglePress(button.x, button.y, button.width + button.x, button.height + button.y);
                button.isClicked = press;

                if (press && button.taskOnClick != null) {
                   button.taskOnClick.run();
                }
            }
        }
    }

    private static void updateKeyLogging() {
        if (keyLogging) {
            for (int i = 48; i <= 90; i++) {
                if (getKeyClick(GLFW_KEY_SPACE)) {
                    keyLoggingText += " ";
                }
                if (getKeyClick(GLFW_KEY_BACKSPACE)) {
                    if (keyLoggingText.length() > 0) {
                        keyLoggingText = keyLoggingText.substring(0, keyLoggingText.length() - 1);
                    }
                }
                if (getKeyClick(GLFW_KEY_PERIOD)) {
                    keyLoggingText += ".";
                }

                //a - z, 0 - 9
                if (i <= 57 || i >= 65) {
                    if (getKeyClick(i)) {
                        keyLoggingText += !getKey(GLFW_KEY_LEFT_SHIFT) ? glfwGetKeyName(i, 0) : glfwGetKeyName(i, 0).toUpperCase();
                    }
                }
            }
        }
    }

    private static void updateClicks() {
        if (Settings.createdSettings && !buttons.get(Json.getName("SettingsSave")).isClicked) {
            int count = (int) buttons.values().stream().filter(currentButton -> currentButton.isClicked && currentButton.visible && (currentButton.group.contains("Swap") || currentButton.group.contains("Drop"))).count();

            if (Settings.needUpdateCount) {
                Settings.pressedCount = count;
                Settings.needUpdateCount = false;
            } else if (count != Settings.pressedCount) {
                buttons.get(Json.getName("SettingsSave")).isClickable = true;
            }
        }

        if (sliders.get("worldSize") != null && sliders.get("worldSize").visible) {
            float worldSize = sliders.get("worldSize").max;
            String pic;

            if (getSliderPos("worldSize") >= worldSize / 1.5f) {
                pic = "planetBig.png";
            } else if (getSliderPos("worldSize") >= worldSize / 3) {
                pic = "planetAverage.png";
            } else {
                pic = "planetMini.png";
            }
            panels.get("planet").options = assetsDir("World/WorldGenerator/" + pic);
        }
    }

    private static void updateHotkeys() {
        if (getKeyClick(GLFW_KEY_ESCAPE) && start) {
            if (!Pause.created) {
                Pause.create();
            } else {
                Pause.delete();
            }
            Settings.delete();
        }
    }

    private static void updateDebug() {
        if (Integer.parseInt(Config.getFromConfig("Debug")) > 0 && System.currentTimeMillis() - lastSecond >= 1000) {
            lastSecond = System.currentTimeMillis();

            if (start) {
                CreateElement.createText(5, 980, "PlayerPos", "Player pos: x - " + (int) WorldGenerator.DynamicObjects.get(0).x + "(" + (int) WorldGenerator.DynamicObjects.get(0).x / 16 + ") y - " + (int) WorldGenerator.DynamicObjects.get(0).y + "(" + (int) WorldGenerator.DynamicObjects.get(0).y / 16 + ")", new SimpleColor(25, 25, 25, 255), null);
                CreateElement.createText(5, 1005, "PhysicsFPS", "Physics FPS: " + updates, new SimpleColor(25, 25, 25, 255), null);
            }
            CreateElement.createText(5, 1030, "HandlerFPS", "Handler FPS: " + handlerUpdates, new SimpleColor(25, 25, 25, 255), null);
            CreateElement.createText(5, 1055, "GameFPS", "Game FPS: " + fps, new SimpleColor(25, 25, 25, 255), null);

            handlerUpdates = 0;
            updates = 0;
            fps = 0;
        }
    }

    @Override
    public void run() {
        while (!glfwWindowShouldClose(glfwWindow)) {
            Player.updatePlayerGUILogic();
            updateButtons();
            updateClicks();
            updateKeyLogging();
            updateSliders();
            updateHotkeys();
            updateLine();
            updateDebug();

            handlerUpdates++;
        }
    }
}
