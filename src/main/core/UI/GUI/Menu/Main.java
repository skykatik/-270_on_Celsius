package core.UI.GUI.Menu;

import core.EventHandling.Logging.Logger;
import core.UI.API.Dialog;
import core.UI.API.Group;
import core.UI.API.Panel;
import core.Utils.SimpleColor;
import java.awt.Desktop;
import java.net.URI;
import static core.EventHandling.Logging.Json.getName;
import static core.EventHandling.Logging.Logger.printException;
import static core.UI.GUI.CreateElement.*;
import static core.Window.*;

public class Main extends Dialog {

    public static final Main instance = new Main();

    private Main() {
        super(null);

        addPanel()
                .setPosition(0, 965)
                .setSize(1920, 115)
                .setSimple(true);

        addImageButton(Main::discordBtn)
                .setPosition(1830, 990)
                .setImage("UI/discordIcon.png");

        addButton(Main::exitBtn)
                .setPosition(822, 990)
                .setSize(240, 65)
                .setName(getName("Exit"))
                .setColor(SimpleColor.DEFAULT_CLICK_BUTTON);

        addButton(this::settingsBtn)
                .setPosition(548, 990)
                .setSize(240, 65)
                .setName(getName("Settings"))
                .setColor(SimpleColor.DEFAULT_CLICK_BUTTON);

        addButton(this::playBtn)
                .setPosition(46, 990)
                .setSize(240, 65)
                .setName(getName("Play"))
                .setColor(SimpleColor.DEFAULT_ACCENT_BUTTON);
    }

    private static void discordBtn() {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI("https://discord.gg/gUS9X6exAQ"));
        } catch (Exception e) {
            printException("Error when open discord server", e);
        }
    }

    private static void exitBtn() {
        Logger.logExit(0);
    }

    private void settingsBtn() {
        Settings.create();
        if (!start) {
            hide();
        } else {
            Pause.delete();
        }
    }

    private void playBtn() {
        hide();
        CreatePlanet.create();
    }
}
