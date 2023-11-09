package core.UI.API;

import core.Global;

public abstract class Dialog extends BaseGroup<Dialog> {

    protected Dialog(Group parent) {
        super(parent);
    }

    public void show() {
        Global.scene.add(this);
    }

    public void hide() {
        Global.scene.remove(this);
    }
}
