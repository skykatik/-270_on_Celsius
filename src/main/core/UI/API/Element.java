package core.UI.API;

public sealed interface Element permits BaseElement, Group {

    // @Nullable
    Group parent();

    int x();

    int y();

    int width();

    int height();

    boolean visible();

    default void draw() {}

    default void update() {}

    Element setSize(int size);

    Element setSize(int width, int height);

    Element setX(int x);
    Element setY(int y);
    Element setPosition(int x, int y);

    Element setVisible(boolean visible);
}
