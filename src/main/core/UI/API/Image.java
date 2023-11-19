package core.UI.API;

import core.Window;
import core.World.Textures.TextureLoader;

import static core.World.Textures.TextureDrawing.drawTexture;

public class Image extends BaseElement<Image> {
    public String asset;

    protected Image(Group parent) {
        super(parent);
    }

    public Image setImage(String path) {
        this.asset = Window.assetsDir(path);
        var size = TextureLoader.getSize(asset);
        setSize(size.width(), size.height());
        return this;
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
