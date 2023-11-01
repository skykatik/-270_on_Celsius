package core.World.Creatures.Player;

import core.EventHandling.EventHandler;
import core.Utils.SimpleColor;
import core.World.Creatures.Player.BuildMenu.BuildMenu;
import core.World.Creatures.Player.Inventory.Inventory;
import core.World.Creatures.Player.Inventory.Items.Items;
import core.World.Creatures.Player.Inventory.Items.Tools;
import core.World.Creatures.Player.Inventory.Items.Weapons.Ammo.Bullets;
import core.World.Textures.*;
import core.World.StaticWorldObjects.StaticObjectsConst;
import core.World.StaticWorldObjects.StaticWorldObjects;
import java.awt.*;
import java.awt.geom.Point2D;
import static core.EventHandling.EventHandler.getMousePos;
import static core.Window.assetsDir;
import static core.Window.start;
import static core.World.Creatures.Player.Inventory.Inventory.*;
import static core.World.HitboxMap.*;
import static core.World.StaticWorldObjects.StaticWorldObjects.*;
import static core.World.WorldGenerator.*;
import static org.lwjgl.glfw.GLFW.*;

public class Player {
    public static boolean noClip = false;

    public static void updatePlayerJump() {
        if (EventHandler.getKey(GLFW_KEY_SPACE)) {
            DynamicObjects.get(0).jump(0.45f);
        }
    }

    public static void updatePlayerMove() {
        float increment = noClip ? 0.5f : 0.1f;

        if (EventHandler.getKeyClick(GLFW_KEY_Q) && DynamicObjects.get(0).animSpeed == 0) {
            DynamicObjects.get(0).path = assetsDir("World/Creatures/playerLeft/player");
            DynamicObjects.get(0).animSpeed = 0.03f;
            setObject((int) ((DynamicObjects.get(0).x - 1) / 16), (int) (DynamicObjects.get(0).y / 16 + 1), StaticWorldObjects.decrementHp(getObject((int) ((DynamicObjects.get(0).x - 1) / 16), (int) (DynamicObjects.get(0).y / 16 + 1)), 10));
        }
        if (EventHandler.getKeyClick(GLFW_KEY_E) && DynamicObjects.get(0).animSpeed == 0) {
            DynamicObjects.get(0).path = assetsDir("World/Creatures/playerRight/player");
            DynamicObjects.get(0).animSpeed = 0.03f;
            setObject((int) (DynamicObjects.get(0).x / 16 + 2), (int) (DynamicObjects.get(0).y / 16 + 1), StaticWorldObjects.decrementHp(getObject((int) (DynamicObjects.get(0).x / 16 + 2), (int) (DynamicObjects.get(0).y / 16 + 1)), 10));
        }

        if (EventHandler.getKey(GLFW_KEY_D) && DynamicObjects.get(0).x + 24 < SizeX * 16 && (noClip || !checkIntersStaticR(DynamicObjects.get(0).x + 0.1f, DynamicObjects.get(0).y, 24, 24))) {
            DynamicObjects.get(0).motionVector.x = increment;
        }
        if (EventHandler.getKey(GLFW_KEY_A) && DynamicObjects.get(0).x > 0 && (noClip || !checkIntersStaticL(DynamicObjects.get(0).x - 0.1f, DynamicObjects.get(0).y, 24))) {
            DynamicObjects.get(0).motionVector.x = -increment;
        }
        if (noClip && EventHandler.getKey(GLFW_KEY_S)) {
            DynamicObjects.get(0).motionVector.y = -increment;
        }
        if (noClip && EventHandler.getKey(GLFW_KEY_W)) {
            DynamicObjects.get(0).motionVector.y = increment;
        }
        DynamicObjects.get(0).notForDrawing = noClip;
    }

    public static void updateInventoryInteraction() {
        if (currentObject != null) {
            updatePlaceableInteraction();
        }
    }

    private static void updatePlaceableInteraction() {
        if (currentObjectType == Items.Types.PLACEABLE && EventHandler.getMousePress()) {
            if (getMousePos().x > (Inventory.inventoryOpen ? 1488 : 1866) && getMousePos().y > 756) {
                return;
            }
            Point blockUMB = getBlockUnderMousePoint();

            if (getType(getObject(blockUMB.x, blockUMB.y)) == StaticObjectsConst.Types.GAS && Player.getDistanceUMB() < 9) {
                int blockX = blockUMB.x;
                int blockY = blockUMB.y;

                if (currentObject != null) {
                    short placeable = inventoryObjects[currentObject.x][currentObject.y].placeable;
                    updatePlaceableBlock(placeable, blockX, blockY);
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
        return false;
    }

    private static void updateToolInteraction() {
        if (currentObjectType == Items.Types.TOOL && currentObject != null) {
            Tools tool = inventoryObjects[currentObject.x][currentObject.y].tool;
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
        if (getDistanceUMB() <= tool.maxInteractionRange && ShadowMap.getDegree(blockX, blockY) == 0) {
            drawBlock(blockX, blockY, object, true);

            if (EventHandler.getMousePress() && getId(object) != 0 && System.currentTimeMillis() - tool.lastHitTime >= tool.secBetweenHits && getHp(object) > 0) {
                tool.lastHitTime = System.currentTimeMillis();

                if (getHp(decrementHp(object, (int) tool.damage)) <= 0) {
                    createElementPlaceable(object, "none");
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

            if (getDistanceUMB() <= tool.maxInteractionRange && ShadowMap.getDegree(blockX, blockY) == 0) {
                drawStructure(blockX, blockY, true, getObject(root.x, root.y), StaticObjectsConst.getConst(StaticWorldObjects.getId(getObject(blockX, blockY))).optionalTiles);

                if (EventHandler.getMousePress() && getId(object) != 0 && System.currentTimeMillis() - tool.lastHitTime >= tool.secBetweenHits && getHp(object) > 0) {
                    tool.lastHitTime = System.currentTimeMillis();
                    decrementHpMulti(blockX, blockY, (int) tool.damage);
                }
            } else {
                drawStructure(blockX, blockY, false, getObject(root.x, root.y), StaticObjectsConst.getConst(StaticWorldObjects.getId(getObject(blockX, blockY))).optionalTiles);
            }
        }
    }

    public static void drawStructure(int blockX, int blockY, boolean breakable, short root, short[][] tiles) {
        drawBlock(blockX, blockY, root, breakable);
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {

                if (tiles[x][y] != 0) {
                    drawBlock(x + blockX, y + blockY, tiles[x][y], breakable);
                }
            }
        }
    }

    public static Point findRoot(int cellX, int cellY) {
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

    private static void decrementHpMulti(int cellX, int cellY, int hp) {
        Point root = findRoot(cellX, cellY);

        if (root != null && getObject(root.x, root.y) != 0) {
            short rootObj = getObject(root.x, root.y);

            for (int x = -(cellX - root.x); x < StaticObjectsConst.getConst(getId(rootObj)).optionalTiles.length - (cellX - root.x); x++) {
                for (int y = -(cellY - root.y); y < StaticObjectsConst.getConst(getId(rootObj)).optionalTiles[0].length - (cellY - root.y); y++) {
                    short object = getObject(x + cellX, y + cellY);

                    if (getHp(decrementHp(object, hp)) <= 0 && getType(object) != StaticObjectsConst.Types.GAS) {
                        createElementPlaceable(rootObj, "");
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
        SimpleColor blockColor = breakable ? new SimpleColor(Math.max(0, a - 150), Math.max(0, a - 150), a, 255) : new SimpleColor(a, Math.max(0, a - 150), Math.max(0, a - 150), 255);
        int xBlock = cellX * 16;
        int yBlock = cellY * 16;

        if (getHp(obj) > getMaxHp(obj) / 1.5f) {
            TextureDrawing.drawTexture(getPath(obj), xBlock, yBlock, 3f, blockColor, false, false);

        } else if (getHp(obj) < getMaxHp(obj) / 3) {
            TextureDrawing.drawMultiTexture(getPath(obj), assetsDir("World/Blocks/damaged1.png"), xBlock, yBlock, 3f, blockColor, false, false);

        } else {
            TextureDrawing.drawMultiTexture(getPath(obj), assetsDir("World/Blocks/damaged0.png"), xBlock, yBlock, 3f, blockColor, false, false);
        }
    }

    public static void drawBlock(int cellX, int cellY, String path, boolean breakable) {
        SimpleColor color = ShadowMap.getColor(cellX, cellY);
        int a = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        SimpleColor blockColor = breakable ? new SimpleColor(Math.max(0, a - 150), Math.max(0, a - 150), a, 255) : new SimpleColor(a, Math.max(0, a - 150), Math.max(0, a - 150), 255);
        int xBlock = cellX * 16;
        int yBlock = cellY * 16;

        TextureDrawing.drawTexture(path, xBlock, yBlock, 3f, blockColor, false, false);
    }

    public static Point getBlockUnderMousePoint() {
        int blockX = (int) Math.max(0, Math.min(getWorldMousePoint().x / 16, SizeX));
        int blockY = (int) Math.max(0, Math.min(getWorldMousePoint().y / 16, SizeY));

        return new Point(blockX, blockY);
    }

    public static Point2D.Float getWorldMousePoint() {
        float blockX = ((getMousePos().x - 960) / 3f + 16) + DynamicObjects.get(0).x;
        float blockY = ((getMousePos().y - 540) / 3f + 64) + DynamicObjects.get(0).y;

        return new Point2D.Float(blockX, blockY);
    }

    public static int getDistanceUMB() {
        return (int) Math.abs((DynamicObjects.get(0).x / 16 - getBlockUnderMousePoint().x) + (DynamicObjects.get(0).y / 16 - getBlockUnderMousePoint().y));
    }

    public static void updatePlayerGUI() {
        if (start) {
            Inventory.update();
            Bullets.drawBullets();
            BuildMenu.draw();
            updateToolInteraction();
        }
    }

    public static void updatePlayerGUILogic() {
        if (start) {
            BuildMenu.updateLogic();
        }
    }
}
