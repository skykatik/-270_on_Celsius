package core.UI.API;

import java.util.ArrayList;
import java.util.List;

import static core.World.Textures.TextureDrawing.drawRectangle;
import static core.World.Textures.TextureDrawing.drawRectangleBorder;

public abstract non-sealed class BaseGroup<G extends BaseElement<G> & Group> extends BaseElement<G> implements Group {
    // TODO специальный лист с защитой от ConcurrentModificationException
    protected ArrayList<Element> children;

    protected BaseGroup(Group parent) {
        super(parent);
    }

    private ArrayList<Element> initChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    @Override
    public List<Element> children() {
        return children != null ? children : List.of();
    }

    @Override
    public <E extends Element> E add(E element) {
        initChildren().add(element);
        return element;
    }

    @Override
    public void remove(Element element) {
        if (children != null) {
            children.remove(element);
        }
    }

    @Override
    public void draw() {
        if (!visible) {
            return;
        }

        if (children != null) {
            for (Element child : children) {
                child.draw();
            }
        }
    }

    @Override
    public void update() {
        if (children != null) {
            for (Element child : new ArrayList<>(children)) {
                child.update();
            }
        }
    }
}
