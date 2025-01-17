package core.World.Creatures.Player;

import core.EventHandling.Logging.Config;
import core.Global;
import core.Utils.SimpleColor;
import core.World.Creatures.DynamicWorldObjects;
import core.World.Creatures.Player.BuildMenu.BuildMenu;
import core.World.Creatures.Player.Inventory.Inventory;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.Creatures.Player.Inventory.Items.Tools;
import core.World.Creatures.Player.Inventory.Items.Weapons.Ammo.Bullets;
import core.World.StaticWorldObjects.TemperatureMap;
import core.World.Textures.*;
import core.World.StaticWorldObjects.StaticObjectsConst;
import java.awt.Point;

import static core.Window.assetsDir;
import static core.Window.start;
import static core.World.Creatures.Player.Inventory.Inventory.*;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;
import static core.World.WorldGenerator.*;
import static core.World.WorldUtils.getBlockUnderMousePoint;
import static core.World.WorldUtils.getDistanceToMouse;
import static org.lwjgl.glfw.GLFW.*;

public class Player {
    public static Thread currentInteraction;
    public static boolean noClip = false;
    private static int transparencyHPline = Config.getFromConfig("AlwaysOnPlayerHPLine").equals("true") ? 220 : 0;
    public static final int playerSize = 72;
    public static int lastDamage = 0;
    public static long lastDamageTime = System.currentTimeMillis();
    private static long lastChangeTransparency = System.currentTimeMillis(), lastChangeLengthDamage = System.currentTimeMillis();

    public static void createPlayer(boolean randomSpawn) {
        DynamicObjects.addFirst(DynamicWorldObjects.createDynamic("player", randomSpawn ? (int) (Math.random() * (SizeX * TextureDrawing.blockSize)) : SizeX * 8f));
    }

    public static void updatePlayerJump() {
        if (Global.input.pressed(GLFW_KEY_SPACE)) {
            DynamicObjects.getFirst().jump(1.05f);
        }
    }

    public static void updatePlayerMove() {
        float increment = noClip ? 1.6f : 0.4f;

//        if (EventHandler.getKeyClick(GLFW_KEY_Q) && DynamicObjects.getFirst().getAnimationSpeed() == 0) {
//            DynamicObjects.getFirst().setPath(assetsDir("World/Creatures/playerLeft/player"));
//            DynamicObjects.getFirst().setAnimationSpeed(30);
//            setObject((int) ((DynamicObjects.getFirst().getX() - 1) / TextureDrawing.blockSize), (int) (DynamicObjects.getFirst().getY() / TextureDrawing.blockSize + 1), StaticWorldObjects.decrementHp(getObject((int) ((DynamicObjects.getFirst().getX() - 1) / TextureDrawing.blockSize), (int) (DynamicObjects.getFirst().getY() / TextureDrawing.blockSize + 1)), 10));
//        }
//        if (EventHandler.getKeyClick(GLFW_KEY_E) && DynamicObjects.getFirst().getAnimationSpeed() == 0) {
//            DynamicObjects.getFirst().setPath(assetsDir("World/Creatures/playerRight/player"));
//            DynamicObjects.getFirst().setAnimationSpeed(30);
//            setObject((int) (DynamicObjects.getFirst().getX() / TextureDrawing.blockSize + 2), (int) (DynamicObjects.getFirst().getY() / TextureDrawing.blockSize + 1), StaticWorldObjects.decrementHp(getObject((int) (DynamicObjects.getFirst().getX() / TextureDrawing.blockSize + 2), (int) (DynamicObjects.getFirst().getY() / TextureDrawing.blockSize + 1)), 10));
//        }

        if (Global.input.pressed(GLFW_KEY_D)) {
            DynamicObjects.getFirst().setMotionVectorX(increment);
        }
        if (Global.input.pressed(GLFW_KEY_A)) {
            DynamicObjects.getFirst().setMotionVectorX(-increment);
        }
        if (noClip && Global.input.pressed(GLFW_KEY_S)) {
            DynamicObjects.getFirst().setMotionVectorY(-increment);
        }
        if (noClip && Global.input.pressed(GLFW_KEY_W)) {
            DynamicObjects.getFirst().setMotionVectorY(increment);
        }
    }

    public static void updateInventoryInteraction() {
        if (currentObject != null) {
            updatePlaceableInteraction();
        }
    }

    private static void updatePlaceableInteraction() {
        if (currentObjectType == Items.Types.PLACEABLE && Global.input.justClicked(GLFW_MOUSE_BUTTON_LEFT)) {
            if (Global.input.mousePos().x > (Inventory.inventoryOpen ? 1488 : 1866)) {
                if (Global.input.mousePos().y > 756) {
                    return;
                }
            }
            Point blockUMB = getBlockUnderMousePoint();

            if (getType(getObject(blockUMB.x, blockUMB.y)) == StaticObjectsConst.Types.GAS && getDistanceToMouse() < 9) {
                Items item = Inventory.getCurrent();
                int blockX = blockUMB.x;
                int blockY = blockUMB.y;

                if (item != null && item.placeable != 0) {
                    updatePlaceableBlock(item.placeable, blockX, blockY);
                }
            }
        }
    }

    private static void updatePlaceableBlock(short placeable, int blockX, int blockY) {
        if (canPlace(placeable, blockX, blockY)) {
            decrementItem(currentObject.x, currentObject.y);
            setObject(blockX, blockY, placeable);
            ShadowMap.update();
        }
    }

    public static boolean canPlace(short placeable, int blockX, int blockY) {
        if (underMouseItem == null) {
            if (StaticObjectsConst.getConst(getId(placeable)).optionalTiles == null && getType(getObject(blockX, blockY)) == StaticObjectsConst.Types.GAS && (getType(getObject(blockX, blockY + 1)) == StaticObjectsConst.Types.SOLID || getType(getObject(blockX, blockY - 1)) == StaticObjectsConst.Types.SOLID || getType(getObject(blockX + 1, blockY)) == StaticObjectsConst.Types.SOLID || getType(getObject(blockX - 1, blockY)) == StaticObjectsConst.Types.SOLID)) {
                return true;
            } else if (StaticObjectsConst.getConst(getId(placeable)).optionalTiles != null && getType(getObject(blockX, blockY - 1)) == StaticObjectsConst.Types.SOLID && getType(getObject(blockX, blockY)) == StaticObjectsConst.Types.GAS) {
                short[][] tiles = StaticObjectsConst.getConst(getId(placeable)).optionalTiles;

                for (int x = 0; x < tiles.length; x++) {
                    for (int y = 0; y < tiles[0].length; y++) {
                        if (getType(getObject(x + blockX, y + blockY)) == StaticObjectsConst.Types.SOLID && getType(tiles[x][y]) == StaticObjectsConst.Types.SOLID) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static void updateToolInteraction() {
        Items item = Inventory.getCurrent();
        if (item != null && item.tool != null) {

            Tools tool = item.tool;
            Point blockUMB = getBlockUnderMousePoint();
            int blockX = blockUMB.x;
            int blockY = blockUMB.y;
            short object = getObject(blockX, blockY);

            if (object != 0 && getPath(object) != null && !StaticObjectsConst.getConst(getId(object)).hasMotherBlock && StaticObjectsConst.getConst(getId(object)).optionalTiles == null) {
                updateNonStructure(blockX, blockY, object, tool);
            } else if (StaticObjectsConst.getConst(getId(object)).hasMotherBlock || StaticObjectsConst.getConst(getId(object)).optionalTiles != null) {
                updateStructure(blockX, blockY, object, tool);
            }
        }
    }

    private static void updateNonStructure(int blockX, int blockY, short object, Tools tool) {
        if (getDistanceToMouse() <= tool.maxInteractionRange && ShadowMap.getDegree(blockX, blockY) == 0) {
            drawBlock(blockX, blockY, object, true);

            if (Global.input.justClicked(GLFW_MOUSE_BUTTON_LEFT) && getId(object) != 0 && System.currentTimeMillis() - tool.lastHitTime >= tool.secBetweenHits && getHp(object) > 0) {
                tool.lastHitTime = System.currentTimeMillis();

                if (getHp(decrementHp(object, (int) tool.damage)) <= 0) {
                    createElementPlaceable(object);
                    destroyObject(blockX, blockY);
                } else {
                    setObject(blockX, blockY, decrementHp(object, (int) tool.damage));
                }
            }
        } else {
            drawBlock(blockX, blockY, object, false);
        }
    }

    private static void updateStructure(int blockX, int blockY, short object, Tools tool) {
        Point root = findRoot(blockX, blockY);

        if (root != null) {
            blockX = root.x;
            blockY = root.y;

            if (getDistanceToMouse() <= tool.maxInteractionRange && ShadowMap.getDegree(blockX, blockY) == 0) {
                drawBlock(blockX, blockY, getObject(root.x, root.y), true);

                if (Global.input.justClicked(GLFW_MOUSE_BUTTON_LEFT) && getId(object) != 0 && System.currentTimeMillis() - tool.lastHitTime >= tool.secBetweenHits && getHp(object) > 0) {
                    tool.lastHitTime = System.currentTimeMillis();
                    decrementHpMulti(blockX, blockY, (int) tool.damage, root);
                }
            } else {
                drawBlock(blockX, blockY, getObject(root.x, root.y), false);
            }
        }
    }

    public static Point findRoot(int cellX, int cellY) {
        if (!StaticObjectsConst.getConst(getId(getObject(cellX, cellY))).hasMotherBlock && StaticObjectsConst.getConst(getId(getObject(cellX, cellY))).optionalTiles == null) {
            return null;
        }
        int maxCellsX = 4;
        int maxCellsY = 4;

        for (int blockX = 0; blockX < maxCellsX; blockX++) {
            for (int blockY = 0; blockY < maxCellsY; blockY++) {
                StaticObjectsConst objConst = StaticObjectsConst.getConst(getId(getObject(cellX - blockX, cellY - blockY)));
                if (objConst != null && objConst.optionalTiles != null) {
                    return new Point(cellX - blockX, cellY - blockY);
                }
            }
        }
        return null;
    }

    private static void decrementHpMulti(int cellX, int cellY, int hp, Point root) {
        if (root != null && getObject(root.x, root.y) != 0) {
            short rootObj = getObject(root.x, root.y);

            for (int x = -(cellX - root.x); x < StaticObjectsConst.getConst(getId(rootObj)).optionalTiles.length - (cellX - root.x); x++) {
                for (int y = -(cellY - root.y); y < StaticObjectsConst.getConst(getId(rootObj)).optionalTiles[0].length - (cellY - root.y); y++) {
                    short object = getObject(x + cellX, y + cellY);

                    if (getHp(decrementHp(object, hp)) <= 0 && getType(object) != StaticObjectsConst.Types.GAS) {
                        createElementPlaceable(rootObj);
                        destroyObject(cellX, cellY);
                        break;
                    } else if (getType(object) != StaticObjectsConst.Types.GAS) {
                        StaticObjects[(x + cellX) + SizeX * (y + cellY)] = decrementHp(object, hp);
                    }
                }
            }
        }
    }

    public static void drawBlock(int cellX, int cellY, short obj, boolean breakable) {
        SimpleColor color = ShadowMap.getColor(cellX, cellY);
        int a = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        SimpleColor blockColor = breakable ? SimpleColor.fromRGBA(Math.max(0, a - 150), Math.max(0, a - 150), a, 255) : SimpleColor.fromRGBA(a, Math.max(0, a - 150), Math.max(0, a - 150), 255);
        int xBlock = cellX * TextureDrawing.blockSize;
        int yBlock = cellY * TextureDrawing.blockSize;

        if (getHp(obj) > getMaxHp(obj) / 1.5f) {
            TextureDrawing.drawTexture(xBlock, yBlock, 1f, false, false, getPath(obj), blockColor);

        } else if (getHp(obj) < getMaxHp(obj) / 3) {
            TextureDrawing.drawMultiTexture(xBlock, yBlock, 1f, false, false, getPath(obj), assetsDir("World/Blocks/damaged1.png"), blockColor);

        } else {
            TextureDrawing.drawMultiTexture(xBlock, yBlock, 1f, false, false, getPath(obj), assetsDir("World/Blocks/damaged0.png"), blockColor);
        }
    }

    public static void updatePlayerGUI() {
        if (start) {
            Bullets.drawBullets();
            updateTemperatureEffect();
            Inventory.update();
            BuildMenu.draw();
            updateToolInteraction();
            drawCurrentHP();
        }
    }

    public static void updatePlayerGUILogic() {
        if (start) {
            BuildMenu.updateLogic();
        }
    }

    private static void updateTemperatureEffect() {
        DynamicWorldObjects player = DynamicObjects.getFirst();
        int temp = (int) TemperatureMap.getAverageTempAroundDynamic(player.getX(), player.getY(), player.getPath());
        int upperLimit = 100;
        int lowestLimit = -20;
        int maxColor = 90;

        int a = 0;
        if (temp > upperLimit) {
            a = Math.min(maxColor, Math.abs((temp - upperLimit) / 3));
        } else if (temp < lowestLimit) {
            a = Math.min(maxColor, Math.abs((temp + lowestLimit) / 3));
        }

        int r = temp > 0 ? a : 0;
        int b = temp > 0 ? 0 : a;

        TextureDrawing.drawTexture(0, 0, 1, true, false, assetsDir("\\UI\\GUI\\modifiedTemperature.png"), SimpleColor.fromRGBA(r, (int) (b / 2f), b, a));
    }

    public static void playerMaxHP() {
        DynamicObjects.getFirst().setCurrentHp(DynamicObjects.getFirst().getMaxHp());
    }

    public static void playerKill() {
        DynamicObjects.getFirst().setCurrentHp(0);
    }

    private static void drawCurrentHP() {
        int currentHp = (int) DynamicObjects.getFirst().getCurrentHP();
        int maxHp = (int) DynamicObjects.getFirst().getMaxHp();

        if (currentHp == maxHp && transparencyHPline > 0 && System.currentTimeMillis() - lastChangeTransparency >= 10 && !Config.getFromConfig("AlwaysOnPlayerHPLine").equals("true")) {
            lastChangeTransparency = System.currentTimeMillis();
            transparencyHPline--;
        } else if (currentHp != maxHp) {
            transparencyHPline = 220;
        }
        if (lastDamage > 0 && System.currentTimeMillis() - lastChangeLengthDamage >= 15 && System.currentTimeMillis() - lastDamageTime >= 300) {
            lastChangeLengthDamage = System.currentTimeMillis();
            lastDamage--;
        }

        if (transparencyHPline > 0) {
            TextureDrawing.drawRectangleBorder(30, 30, 200, 35, 1, SimpleColor.fromRGBA(10, 10, 10, transparencyHPline));
            TextureDrawing.drawRectangle(31, 31, currentHp * 2 - 2, 33, SimpleColor.fromRGBA(150, 0, 20, transparencyHPline));

            if (lastDamage > 0) {
                TextureDrawing.drawRectangle(29 + currentHp * 2, 31, Math.min(lastDamage * 2, 200), 33, SimpleColor.fromRGBA(252, 161, 3, transparencyHPline));
            }
        }
    }
}
