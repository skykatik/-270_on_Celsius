package core.UI.API;

import core.EventHandling.EventHandler;
import core.Utils.SimpleColor;

import static core.World.Textures.TextureDrawing.*;

public class BaseButton<B extends BaseButton<B>> extends BaseElement<B> {
    public boolean simple, isClickable = true, isClicked;
    public SimpleColor color = SimpleColor.DEFAULT_ACCENT_BUTTON;
    public String name, prompt;
    public Runnable clickAction;

    protected BaseButton(Group panel) {
        super(panel);
    }

    public B setClicked(boolean clicked) {
        this.isClicked = clicked;
        return as();
    }

    public B setPrompt(String prompt) {
        this.prompt = prompt;
        return as();
    }

    public B setName(String name) {
        this.name = name;
        return as();
    }

    public B onClick(Runnable clickAction) {
        this.clickAction = clickAction;
        return as();
    }

    public B setColor(SimpleColor color) {
        this.color = color;
        return as();
    }

    public B setSimple(boolean simple) {
        this.simple = simple;
        return as();
    }

    public B setClickable(boolean isClickable) {
        this.isClickable = isClickable;
        return as();
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
