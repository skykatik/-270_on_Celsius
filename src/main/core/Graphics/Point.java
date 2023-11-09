package core.Graphics;

public final class Point {
    public int x, y;

    public Point() {}

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point setX(int x) {
        this.x = x;
        return this;
    }

    public Point setY(int y) {
        this.y = y;
        return this;
    }

    public Point set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Point set(Point other) {
        return set(other.x, other.y);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Point point && x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        int h = 5381;
        h += (h << 5) + x;
        h += (h << 5) + y;
        return h;
    }

    @Override
    public String toString() {
        return '(' + x + ", " + y + ')';
    }
}
