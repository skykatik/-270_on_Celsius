package core.World.StaticWorldObjects;

import core.EventHandling.Logging.Config;
import core.Window;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import static core.Window.*;

public class StaticObjectsConst implements Cloneable {
    private static final ConcurrentHashMap<Byte, StaticObjectsConst> constants = new ConcurrentHashMap<>();
    public short[][] optionalTiles;
    public float maxHp, density, resistance;
    public int lightTransmission;
    public boolean hasMotherBlock;
    //original file name - filename, object name - name at file
    public String path, originalFileName, objectName;
    public Runnable onInteraction;
    public Types type;

    public enum Types {
        GAS,
        LIQUID,
        SOLID,
        PLASMA
    }

    @Override
    public StaticObjectsConst clone() {

        try {
            return (StaticObjectsConst) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private StaticObjectsConst(boolean hasMotherBlock, float maxHp, float density, float resistance, int lightTransmission, String path, String objectName, String originalFileName, Types type) {
        this.hasMotherBlock = hasMotherBlock;
        this.maxHp = maxHp;
        this.density = density;
        this.path = path;
        this.objectName = objectName;
        this.originalFileName = originalFileName;
        this.type = type;
        this.lightTransmission = lightTransmission;
        this.resistance = resistance;
        this.optionalTiles = null;
    }

    private StaticObjectsConst(boolean hasMotherBlock, float maxHp, float density, float resistance, int lightTransmission, String path, String objectName, String originalFileName, short[][] optionalTiles, Types type) {
        this.hasMotherBlock = hasMotherBlock;
        this.maxHp = maxHp;
        this.density = density;
        this.path = path;
        this.objectName = objectName;
        this.originalFileName = originalFileName;
        this.type = type;
        this.lightTransmission = lightTransmission;
        this.optionalTiles = optionalTiles;
        this.resistance = resistance;
    }

    private StaticObjectsConst(boolean hasMotherBlock, float maxHp, float density, float resistance, int lightTransmission, String path, String objectName, String originalFileName, short[][] optionalTiles, Types type, Runnable onInteraction) {
        this.hasMotherBlock = hasMotherBlock;
        this.maxHp = maxHp;
        this.density = density;
        this.path = path;
        this.objectName = objectName;
        this.originalFileName = originalFileName;
        this.type = type;
        this.lightTransmission = lightTransmission;
        this.optionalTiles = optionalTiles;
        this.resistance = resistance;
        this.onInteraction = new File(assetsDir("\\World\\ItemsCharacteristics\\BlocksInteractions" + objectName + ".java")).exists() ? generateRunnable(assetsDir("\\World\\ItemsCharacteristics\\BlocksInteractions" + objectName + ".java")) : onInteraction;
    }

    //todo генерация раннабле интеракции для возможности переопределить поведение блока
    private static Runnable generateRunnable(String path) {
        return null;
    }

    public static void setConst(StaticObjectsConst staticConst, byte id) {
        constants.put(id, staticConst);
    }

    public static void setConst(String name, byte id, short[][] optionalTiles) {
        if (constants.get(id) == null) {
            StaticObjectsConst staticConst = getConst(assetsDir("World/ItemsCharacteristics/" + name + ".properties"));
            staticConst.optionalTiles = optionalTiles;
            staticConst.originalFileName = name;

            constants.put(id, staticConst);
        }
    }

    public static StaticObjectsConst getConst(String path) {
        Properties props = Config.getProperties(path);
        boolean hasMotherBlock = Boolean.parseBoolean((String) props.getOrDefault("HasMotherBlock", "false"));
        float density = Float.parseFloat((String) props.getOrDefault("Density", "1"));
        float resistance = Float.parseFloat((String) props.getOrDefault("Resistance", "100"));
        int lightTransmission = Integer.parseInt((String) props.getOrDefault("LightTransmission", "100"));
        int maxHp = Integer.parseInt((String) props.getOrDefault("MaxHp", "100"));
        String texturePath = Window.assetsDir((String) props.getOrDefault("Path", "\\World\\textureNotFound.png"));
        String enumType = (String) props.getOrDefault("Type", Types.SOLID.toString());
        String objectName = (String) props.getOrDefault("Name", "notFound");

        return new StaticObjectsConst(hasMotherBlock, maxHp, density, resistance, lightTransmission, texturePath, objectName, null, null, Types.valueOf(enumType.toUpperCase()));
    }

    private static String getStorageFolder(String path) {
        return path.substring(path.lastIndexOf("/", path.lastIndexOf("/") - 1) + 1, path.lastIndexOf("/"));
    }

    public static void setConst(String name, byte id) {
        setConst(name, id, null);
    }

    public static void setDestroyed() {
        constants.put((byte) 0, new StaticObjectsConst(false, 0, 0, 0, 100, null, "Destroyed", null, Types.GAS));
    }

    public static StaticObjectsConst getConst(byte id) {
        return constants.get(id);
    }

    public static boolean checkIsHere(byte id) {
        return constants.get(id) != null;
    }
}
