package core.UI.GUI.Menu;

import core.UI.API.Dialog;
import core.UI.API.Image;
import core.UI.API.Slider;
import core.Utils.SimpleColor;
import core.World.WorldGenerator;

import static core.EventHandling.Logging.Json.getName;
import static core.Utils.SimpleColor.fromRGBA;

public class CreatePlanet extends Dialog {

    // TODO ресетать бульки?

    public static final CreatePlanet instance = new CreatePlanet();

    public boolean simpleGeneration, randomSpawn, generateCreatures = true;

    private final Image planetImage;
    private final Slider slider;

    private CreatePlanet() {
        addPanel().set(20, 20, 1880, 200);
        addPanel().set(20, 240, 1400, 820);
        addPanel().set(1440, 240, 460, 820);

        addToggleButton(() -> simpleGeneration = !simpleGeneration)
                .set(70, 980, 32, 32)
                .setName(getName("GenerateSimpleWorld"))
                .setPrompt(getName("GenerateSimpleWorldPrompt"))
                .setColor(SimpleColor.DEFAULT_CLICK_BUTTON);
        addToggleButton(() -> randomSpawn = !randomSpawn)
                .set(70, 840, 32, 32)
                .setName(getName("RandomSpawn"))
                .setPrompt(getName("RandomSpawnPrompt"))
                .setColor(SimpleColor.DEFAULT_CLICK_BUTTON);
        addToggleButton(() -> generateCreatures = !generateCreatures)
                .set(70, 910, 32, 32)
                .setName(getName("GenerateCreatures"))
                .setPrompt(getName("GenerateCreaturesPrompt"))
                .setColor(SimpleColor.DEFAULT_CLICK_BUTTON);

        addButton(WorldGenerator::generateWorld)
                .set(1460, 260, 420, 67)
                .setName(getName("GenerateWorld"))
                .setSimple(true);

        addImage(1460, 620, "World/WorldGenerator/skyBackgroundPlanet.png");

        planetImage = addImage(1510, 670, "World/WorldGenerator/planetMini.png");

        slider = addSlider((pos, worldSize) -> {
            String pic;
            if (pos >= worldSize / 1.5f) {
                pic = "planetBig.png";
            } else if (pos >= worldSize / 3) {
                pic = "planetAverage.png";
            } else {
                pic = "planetMini.png";
            }
            planetImage.setImage("World/WorldGenerator/" + pic);
        })
        .set(1460, 340, 420, 20)
        .setMax(2500)
        .setSliderColor(fromRGBA(40, 40, 40, 240))
        .setDotColor(fromRGBA(255, 80, 0, 119));
    }

    public int getWorldSize() {
        return slider.getSliderPos() + 20;
    }
}
