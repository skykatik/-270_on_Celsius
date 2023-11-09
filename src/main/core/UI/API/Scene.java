package core.UI.API;

import core.EventHandling.Logging.Logger;

import java.util.ArrayList;

public final class Scene {

    private final ArrayList<Element> elements = new ArrayList<>();

    public void add(Element element) {
        elements.add(element);
    }

    public void remove(Element element) {
        elements.remove(element);
    }

    public void clean() {
        elements.clear();
    }

    public void processUpdate() {
        for (Element element : new ArrayList<>(elements)) {
            try {
                element.update();
            } catch (Exception e) {
                Logger.printException("Failed to update '" + element + "'", e);
            }

            try {
                element.draw();
            } catch (Exception e) {
                Logger.printException("Failed to draw '" + element + "'", e);
            }
        }
    }
}
