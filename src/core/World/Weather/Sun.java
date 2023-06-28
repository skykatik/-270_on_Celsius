package core.World.Weather;

import java.awt.*;

import static core.EventHandling.Logging.Config.getFromConfig;
import static core.Window.*;
import static core.World.Textures.TextureDrawing.*;
import static core.World.WorldGenerator.*;
import static core.World.WorldGenerator.DynamicObjects;

public class Sun {
    public static float currentTime = (float) (Math.random() * 2400), x, y = -800 * (1 -  (currentTime - 2400) / (1 - 2400)) + 1500 * (currentTime - 2400) / (1 - 2400) + (SizeY / 2f * 16 - 700);
    public static boolean visible = false;
    private static final int startSunset = 800, endSunset = 1600;
    private static int aGradient = 0;
    private static long lastTime = System.currentTimeMillis();

    public static void createSun() {
        visible = true;
    }

    public static void updateSun() {
        if (visible) {
            if (System.currentTimeMillis() - lastTime >= 750) {
                lastTime = System.currentTimeMillis();
                currentTime++;

                if (currentTime > 2400 || currentTime < 0) { // 2400 - 23:59
                    currentTime = 0;
                }
                if (DynamicObjects[0].isPlayer) {
                    x = DynamicObjects[0].x;

                    if (currentTime >= 2400 || currentTime < 1) {
                        y = -800;
                    } else {
                        double t = (currentTime - 2400) / (1 - 2400);
                        y = (float) (-800 * (1 - t) + 1500 * t) + (SizeY / 2f * 16 - 700);
                    }
                }
            }
            final int minGreen = 85;
            final int maxGreen = 255;

            double ratio = (double) (maxGreen - minGreen) / (2400 - minGreen);
            int green = (int) (maxGreen - (currentTime * ratio));

            updateGradient();
            drawTexture(defPath + "\\src\\assets\\World\\other\\sun.png", 580, y, 1, new Color(255, green, 40, 220), true);
        }
    }

    private static void updateGradient() {
        double alpha = 0;

        if (currentTime >= startSunset && currentTime <= endSunset) {
            alpha = Lerp(0, 1, (currentTime - startSunset) / (endSunset - startSunset));
        } else if (currentTime > endSunset) {
            alpha = Lerp(1, 0, (currentTime - endSunset) / (endSunset - startSunset));
        }
        aGradient = (int) (255 * alpha);
        aGradient = Math.max(0, Math.min(255, aGradient));

        drawTexture(defPath + "\\src\\assets\\World\\other\\" + (getFromConfig("InterpolateSunset").equals("true") ? "" : "non") + "InterpolatedSunset.png", 0, 0, 1, new Color(aGradient, 0, 20, aGradient), true);
    }

    private static double Lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
