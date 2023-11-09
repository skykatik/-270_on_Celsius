package core.UI.API;

import java.util.List;

public sealed interface Group extends Element permits BaseGroup {
    List<Element> children();

    <E extends Element> E add(E element);

    void remove(Element element);

    @Override
    Group setSize(int size);

    @Override
    Group setSize(int width, int height);

    @Override
    Group setX(int x);

    @Override
    Group setY(int y);

    @Override
    Group setPosition(int x, int y);

    @Override
    Group setVisible(boolean state);

    // region Дополнительные методы

    default Panel addPanel() {
        return add(new Panel(this));
    }

    default Button addButton(Runnable onClick) {
        return add(new Button(this))
                .onClick(onClick);
    }

    default ImageButton addImageButton(Runnable onClick) {
        return add(new ImageButton(this))
                .onClick(onClick);
    }

    // endregion
}
