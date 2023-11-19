package core.UI.API;

import core.EventHandling.EventHandler;

import static core.Window.assetsDir;
import static core.Window.glfwWindow;
import static core.World.Textures.TextureDrawing.*;
import static org.lwjgl.glfw.GLFW.*;

public class ToggleButton extends BaseButton<ToggleButton> {

    protected ToggleButton(Group panel) {
        super(panel);
    }

    private boolean withCheckMark = true;
    private boolean canUse = true;

    @Override
    public void update() {
        if (!isClickable) {
            return;
        }

        if (EventHandler.mouseMoved) {
            canUse = false;
        } else {
            switch (glfwGetMouseButton(glfwWindow, GLFW_MOUSE_BUTTON_LEFT)) {
                case GLFW_PRESS -> {
                    if (canUse && EventHandler.isMousePressed(this)) {
                        canUse = false;

                        isClicked = !isClicked;
                        if (clickAction != null) {
                            clickAction.run();
                        }
                    }
                }
                case GLFW_RELEASE -> canUse = true;
            }
        }

    }

    @Override
    public void draw() {
        if (!visible) {
            return;
        }

        if (simple) {
            drawRectangle(x, y, width, height, color);
            if (withCheckMark) {
                if (isClicked) {
                    drawTexture(assetsDir("UI/GUI/checkMarkTrue.png"), x + width / 1.3f, y + height / 3f, 1, true);
                }
            }
            drawText((int) (x * 1.1f), y + height / 3, name);
        } else {
            drawRectangleBorder(x - 6, y - 6, width, height, 6, color);
            if (withCheckMark) {
                String markTexture;
                if (isClicked) {
                    markTexture = assetsDir("UI/GUI/checkMarkTrue.png");
                } else {
                    markTexture = assetsDir("UI/GUI/checkMarkFalse.png");
                }
                drawTexture(markTexture, x, y, 1, true);
            }

            drawText(width + x + 24, y, name);
        }

        drawPrompt(this);
    }
}
