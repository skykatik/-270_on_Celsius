package core.UI.API;

public abstract non-sealed class BaseElement<E extends BaseElement<E>> implements Element {
    public final Group parent;

    protected int x, y;
    protected int width, height;
    protected boolean visible = true;

    protected BaseElement(Group parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    protected E as() {
        return (E) this;
    }

    @Override
    public final Group parent() {
        return parent;
    }

    @Override
    public final int x() {
        return x;
    }

    @Override
    public final int y() {
        return y;
    }

    @Override
    public final int width() {
        return width;
    }

    @Override
    public final int height() {
        return height;
    }

    @Override
    public final boolean visible() {
        return visible;
    }

    @Override
    public E setSize(int size) {
        this.width = size;
        this.height = size;
        return as();
    }

    @Override
    public E setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return as();
    }

    @Override
    public E set(int x, int y, int width, int height) {
        setPosition(x, y);
        setSize(width, height);
        return as();
    }

    @Override
    public E setX(int x) {
        this.x = x;
        return as();
    }

    @Override
    public E setY(int y) {
        this.y = y;
        return as();
    }

    @Override
    public E setPosition(int x, int y) {
        setX(x);
        setY(y);
        return as();
    }

    @Override
    public E setVisible(boolean visible) {
        this.visible = visible;
        return as();
    }
}
