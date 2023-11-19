package core.UI.API;

import core.EventHandling.EventHandler;
import core.Utils.SimpleColor;

import static core.World.Textures.TextureDrawing.drawCircle;
import static core.World.Textures.TextureDrawing.drawRectangle;

public class Slider extends BaseElement<Slider> {
    private int sliderPos;
    public int max;
    public SimpleColor sliderColor, dotColor;
    public MoveListener updater;

    public interface MoveListener {
        void update(int pos, int max);
    }

    protected Slider(Group parent) {
        super(parent);
    }

    public int getSliderPos() {
        float relativePos = (float) (sliderPos - x) / width;
        return Math.round(relativePos * max);
    }

    public Slider onMove(MoveListener updater) {
        this.updater = updater;
        return this;
    }

    public Slider setMax(int max) {
        this.max = max;
        return this;
    }

    public Slider setSliderColor(SimpleColor color) {
        this.sliderColor = color;
        return this;
    }

    public Slider setDotColor(SimpleColor color) {
        this.dotColor = color;
        return this;
    }

    @Override
    public void draw() {
        if (visible) {
            drawRectangle(x, y, width, height, sliderColor);
            drawCircle(sliderPos, y + height / 2, height / 1.1f, dotColor);
        }
    }

    @Override
    public void update() {
        if (EventHandler.isMousePressed(this)) {
            sliderPos = EventHandler.getMousePos().x;
            if (updater != null) {
                updater.update(getSliderPos(), max);
            }
        }
    }

    @Override
    public Slider setX(int x) {
        this.sliderPos = x + 1;
        return super.setX(x);
    }
}
