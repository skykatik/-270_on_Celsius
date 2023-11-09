package core.UI.API;

import core.Utils.SimpleColor;

import java.util.ArrayList;

import static core.World.Textures.TextureDrawing.drawRectangle;
import static core.World.Textures.TextureDrawing.drawRectangleBorder;

public class Panel extends BaseGroup<Panel> {

    public boolean simple;
    public SimpleColor color = SimpleColor.DEFAULT_PANEL;

    public Panel(Group parent) {
        super(parent);
    }

    public Panel setSimple(boolean simple) {
        this.simple = simple;
        return this;
    }

    public Panel setColor(SimpleColor color) {
        this.color = color;
        return this;
    }

    @Override
    public void draw() {
        if (!simple) {
            drawRectangle(x, y, width, height, color);
            drawRectangleBorder(x, y, width, height, 20, color);
        } else {
            drawRectangle(x, y, width, height, color);
        }

        if (children != null) {
            for (Element child : children) {
                child.draw();
            }
        }
    }
}
