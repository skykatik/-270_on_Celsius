package core.UI.API;

import core.EventHandling.EventHandler;
import core.Window;
import core.World.Textures.TextureLoader;

import static core.World.Textures.TextureDrawing.drawTexture;

public class ImageButton extends BaseElement<ImageButton> {

    public boolean isClickable = true, isClicked;
    public Runnable clickAction;
    public String asset;

    protected ImageButton(Group parent) {
        super(parent);
    }

    public ImageButton setImage(String path) {
        this.asset = Window.assetsDir(path);
        var size = TextureLoader.getSize(asset);
        setSize(size.width(), size.height());
        return this;
    }

    public ImageButton onClick(Runnable clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public ImageButton setClickable(boolean isClickable) {
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
        if (visible) {
            if (asset != null) {
                drawTexture(asset, x, y, 1, true);
            }
        }
    }
}
