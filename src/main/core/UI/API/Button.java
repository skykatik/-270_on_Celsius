package core.UI.API;

import core.EventHandling.EventHandler;
import core.Utils.SimpleColor;

import static core.World.Textures.TextureDrawing.*;

public class Button extends BaseElement<Button> {
    public boolean simple, isClickable = true, isClicked;
    public SimpleColor color = SimpleColor.DEFAULT_ACCENT_BUTTON;
    public String name, prompt;
    public Runnable clickAction;

    protected Button(Group panel) {
        super(panel);
    }

    public Button setClicked(boolean clicked) {
        this.isClicked = clicked;
        return this;
    }

    public Button setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public Button setName(String name) {
        this.name = name;
        return this;
    }

    public Button onClick(Runnable clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public Button setColor(SimpleColor color) {
        this.color = color;
        return this;
    }

    public Button setSimple(boolean simple) {
        this.simple = simple;
        return this;
    }

    public Button setClickable(boolean isClickable) {
        this.isClickable = isClickable;
        return this;
    }

    @Override
    public void update() {
        if (!isClickable) {
            return;
        }
        boolean press = EventHandler.isMousePressed(this);
        isClicked = press;
        if (press && clickAction != null) {
            clickAction.run();
        }
    }

    @Override
    public void draw() {
        if (!visible) {
            return;
        }
        if (simple) {
            drawRectangle(x, y, width, height, color);
        } else {
            drawRectangleBorder(x, y, width, height, 6, color);
        }
        if (!isClickable) {
            drawRectangle(x, y, width, height, SimpleColor.fromRGBA(0, 0, 0, 123));
        }
        if (name != null) {
            drawText(x + 20, (int) (y + height / 2.8f), name);
        }
        drawPrompt(this);
    }
}
