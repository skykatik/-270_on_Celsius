package core.World;

import core.EventHandling.Logging.Json;
import core.UI.GUI.Menu.CreatePlanet;
import core.Window;
import core.World.Creatures.CreaturesGenerate;
import core.World.Creatures.Physics;
import core.World.Textures.DynamicWorldObjects;
import core.World.Textures.ShadowMap;
import core.World.Textures.SimpleColor;
import core.World.Textures.StaticWorldObjects.StaticObjectsConst;
import core.World.Textures.StaticWorldObjects.Structures;
import core.World.Weather.Sun;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.InflaterInputStream;
import static core.EventHandling.Logging.Logger.log;
import static core.UI.GUI.CreateElement.*;
import static core.Window.*;
import static core.World.Textures.StaticWorldObjects.StaticWorldObjects.*;

public class WorldGenerator {
    public static int SizeX, SizeY;
    public static short[] StaticObjects;
    public static ArrayList<DynamicWorldObjects> DynamicObjects = new ArrayList<>();
    private static HashMap<String, Structures> structures = new HashMap<>();

    public static void setObject(int x, int y, short object) {
        StaticObjects[x + SizeX * y] = object;
    }

    public static short getObject(int x, int y) {
        if (x < 0 || x > SizeX || y < 0 || y > SizeY) {
            return -1;
        }
        return StaticObjects[x + SizeX * y];
    }

    public static int findX(int x, int y) {
        return ((x + SizeX * y) % SizeX) * 16;
    }

    public static int findY(int x, int y) {
        return ((x + SizeX * y) / SizeX) * 16;
    }

    public static void generateWorld() {
        int SizeX = getSliderPos("worldSize") + 20;
        int SizeY = getSliderPos("worldSize") + 20;
        boolean simple = buttons.get(Json.getName("GenerateSimpleWorld")).isClicked;
        boolean randomSpawn = buttons.get(Json.getName("RandomSpawn")).isClicked;
        boolean creatures = buttons.get(Json.getName("GenerateCreatures")).isClicked;

        log("\nWorld generator: version: 1.0, written at dev 0.0.0.5" + "\nWorld generator: starting generating world at size: x - " + SizeX + ", y - " + SizeY + " (" + SizeX * SizeY + "); with arguments 'simple: " + simple + ", random spawn: " + randomSpawn + "'");

        StaticObjects = new short[(SizeX + 1) * (SizeY + 1)];
        WorldGenerator.SizeX = SizeX;
        WorldGenerator.SizeY = SizeY;

        generateBlocks(simple);
        generateDynamicsObjects(randomSpawn);

        log("World generator: generating done!\n");
        createText(42, 50, "generatingDone", "Done! Starting world..", new SimpleColor(147, 51, 0, 255), "WorldGeneratorState");

        Sun.createSun();
        start(creatures);
    }

    private static void generateBlocks(boolean simple) {
        StaticObjectsConst.setDestroyed();
        generateFlatWorld();

        if (simple) {
            ShadowMap.generate();
            generateResources();
        } else {
            generateMountains();
            smoothWorld();
            fillHollows();

            ShadowMap.generate();
            generateResources();
            generateEnvironment();
            ShadowMap.generate();
        }
    }

    private static void generateFlatWorld() {
        log("World generator: generating flat world");
        createText(42, 170, "WorldGeneratorState", "Generating flat world", new SimpleColor(210, 210, 210, 255), "WorldGeneratorState");

        for (int x = 0; x < SizeX; x++) {
            for (int y = 0; y < SizeY; y++) {
                if (y > SizeY / 1.5f) {
                    setObject(x, y, createStatic("Gas"));
                } else {
                    setObject(x, y, createStatic("Grass"));
                }
            }
        }
    }

    private static void loadAllStructures() {
        File folder = new File(defPath + "\\src\\assets\\World\\Saves");

        if (folder.exists() && folder.isDirectory()) {
            getAllFilePaths(folder);
        }
    }

    private static void getAllFilePaths(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    loadStructure(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    getAllFilePaths(file);
                }
            }
        }
    }

    private static void loadStructure(String path) {
        try (FileInputStream fis = new FileInputStream(path);
             InflaterInputStream iis = new InflaterInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(iis)) {

            structures.put(path, (Structures) ois.readObject());
        } catch (Exception e) {
            log("Error when load structure, path: '" + path + "', error: " + e);
        }
    }

    private static void generateMountains() {
        log("World generator: generating mountains");
        createText(42, 140, "generateMountainsText", "Generating mountains", new SimpleColor(210, 210, 210, 255), "WorldGeneratorState");

        float randGrass = 2f;           //шанс появления неровности, выше число - ниже шанс
        float randAir = 3.5f;           //шанс появления воздуха вместо блока, выше число - ниже шанс
        float iterations = 3f;          //количество итераций генерации
        float mountainHeight = 24000f;  //шанс появления высоких гор, выше число - выше шанс

        for (int i = 0; i < iterations; i++) {
            for (int x = 1; x < SizeX - 1; x++) {
                for (int y = SizeY / 3; y < SizeY - 1; y++) {
                    randGrass += y / (mountainHeight * SizeY);

                    if ((getType(getObject(x + 1, y)) == StaticObjectsConst.Types.SOLID || getType(getObject(x - 1, y)) == StaticObjectsConst.Types.SOLID || getType(getObject(x, y + 1)) == StaticObjectsConst.Types.SOLID || getType(getObject(x, y - 1)) == StaticObjectsConst.Types.SOLID) && Math.random() * randGrass < 1) {
                        setObject(x, y, createStatic("Grass"));
                    } else if (Math.random() * randAir < 1) {
                        setObject(x, y, destroyObject(getObject(x, y)));
                    }
                }
            }
        }
    }

    private static void fillHollows() {
        log("World generator: filling hollows");
        createText(42, 80, "fillHollowsText", "Filling hollows", new SimpleColor(210, 210, 210, 255), "WorldGeneratorState");

        boolean[][] visited = new boolean[SizeX][SizeY];

        for (int x = 1; x < SizeX - 1; x++) {
            for (int y = 1; y < SizeY - 1; y++) {
                if (getType(getObject(x, y)) == StaticObjectsConst.Types.GAS && !visited[x][y]) {
                    List<int[]> area = new ArrayList<>();
                    boolean isClosed = search(x, y, visited, area);
                    if (isClosed) {
                        fillAreaWithGrass(area);
                    }
                }
            }
        }
    }

    private static void smoothWorld() {
        log("World generator: smoothing world");
        createText(42, 110, "smoothWorldText", "Smoothing world", new SimpleColor(210, 210, 210, 255), "WorldGeneratorState");

        float smoothingChance = 3f; //шанс сглаживания, выше число - меньше шанс

        for (int x = 1; x < SizeX - 1; x++) {
            for (int y = 1; y < SizeY - 1; y++) {
                if ((getType(getObject(x, y)) != StaticObjectsConst.Types.GAS && getType(getObject(x + 1, y)) == StaticObjectsConst.Types.SOLID && getType(getObject(x - 1, y)) == StaticObjectsConst.Types.SOLID && getType(getObject(x, y - 1)) == StaticObjectsConst.Types.SOLID) || (getType(getObject(x + 1, y + 1)) == StaticObjectsConst.Types.SOLID && getType(getObject(x - 1, y - 1)) == StaticObjectsConst.Types.SOLID) || (getType(getObject(x - 1, y + 1)) == StaticObjectsConst.Types.SOLID && getType(getObject(x + 1, y - 1)) == StaticObjectsConst.Types.SOLID && Math.random() * smoothingChance < 1)) {
                    setObject(x, y, createStatic("Grass"));
                } else if ((getType(getObject(x, y + 1)) == StaticObjectsConst.Types.SOLID && getType(getObject(x, y - 1)) == StaticObjectsConst.Types.SOLID) || (getType(getObject(x + 1, y)) == StaticObjectsConst.Types.SOLID && getType(getObject(x - 1, y)) == StaticObjectsConst.Types.SOLID) && Math.random() * smoothingChance < 1) {
                    setObject(x, y, createStatic("Grass"));
                }
            }
        }
    }

    private static boolean search(int x, int y, boolean[][] visited, List<int[]> area) {
        java.util.Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{x, y});
        visited[x][y] = true;

        boolean isClosed = true;
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];
            area.add(current);

            if (cx == 0 || cx == SizeX - 1 || cy == 0 || cy == SizeY - 1) {
                isClosed = false;
            }

            if (cx > 0 && getType(getObject(cx - 1, cy)) == StaticObjectsConst.Types.GAS && !visited[cx - 1][cy]) {
                queue.offer(new int[]{cx - 1, cy});
                visited[cx - 1][cy] = true;
            }
            if (cx < SizeX - 1 &&  getType(getObject(cx + 1, cy)) == StaticObjectsConst.Types.GAS && !visited[cx + 1][cy]) {
                queue.offer(new int[]{cx + 1, cy});
                visited[cx + 1][cy] = true;
            }
            if (cy > 0 &&  getType(getObject(cx, cy - 1)) == StaticObjectsConst.Types.GAS && !visited[cx][cy - 1]) {
                queue.offer(new int[]{cx, cy - 1});
                visited[cx][cy - 1] = true;
            }
            if (cy < SizeY - 1 && getType( getObject(cx, cy + 1)) == StaticObjectsConst.Types.GAS && !visited[cx][cy + 1]) {
                queue.offer(new int[]{cx, cy + 1});
                visited[cx][cy + 1] = true;
            }
        }

        return isClosed;
    }

    public static void createStructure(int cellX, int cellY, String path) {
        if (structures.get(path) != null) {
            short[][] objects = structures.get(path).blocks;

            for (int x = 0; x < objects.length; x++) {
                for (int y = 0; y < objects[x].length; y++) {
                    if (cellX + x < SizeX && cellY + y < SizeY && cellX + x > 0 && cellY + y > 0 && getId(objects[x][y]) != 0 && getType(objects[x][y]) != StaticObjectsConst.Types.GAS) {
                        setObject(cellX + x, cellY + y, createStatic(getFileName(objects[x][y])));
                    }
                }
            }
        } else {
            loadStructure(path);
            createStructure(cellX, cellY, path);
        }
    }

    private static void fillAreaWithGrass(List<int[]> area) {
        for (int[] coord : area) {
            int x = coord[0];
            int y = coord[1];
            setObject(x, y, createStatic("Grass"));
        }
    }

    private static void generateCanyons() {

    }

    private static void generateEnvironment() {
        loadAllStructures();
        generateTrees();
        generateDecorStones();
        structures.clear();
    }

    private static void generateTrees() {
        byte[] forests = new byte[SizeX];
        float lastForest = 0;
        float lastForestSize = 0;

        //(максимальный размер + минимальный) не должны превышать 127
        float chance = 80;
        float maxForestSize = 20;
        float minForestSize = 2;

        //первый этап - сажает семечки для лесов и задает размер
        for (int x = 0; x < SizeX; x++) {
            if (Math.random() * chance < 1 && lastForest != x && lastForest + lastForestSize < x) {
                forests[x] = (byte) ((Math.random() * maxForestSize) + minForestSize);
                lastForest = x;
                lastForestSize = (float) ((forests[x] * Math.random() * 8) + 4);
            }
        }

        //второй этап - сажает деревья по семечкам
        for (int x = 0; x < forests.length; x++) {
            if (forests[x] > 0) {
                for (int i = 0; i < forests[x]; i++) {
                    final String path = defPath + "\\src\\assets\\World\\Saves\\tree" + (int) (Math.random() * 2) + ".ser";
                    int distance = (int) ((Math.random() * 8) + 4);
                    int xTree = x + (i * distance);
                    int yTree = findFreeVerticalCell(x + (i * distance));

                    if (xTree > 0 && yTree > 0 && xTree < forests.length && !checkInterInsideSolid(xTree, yTree, path) && xTree + structures.get(path).lowestSolidBlock < SizeX && yTree - 1 < SizeY && getType(getObject(xTree + structures.get(path).lowestSolidBlock, yTree - 1)) == StaticObjectsConst.Types.SOLID) {
                        createStructure(xTree, yTree, path);
                    }
                }
            }
        }
    }

    private static void generateDecorStones() {
        float chance = 40;

        for (int x = 0; x < SizeX; x++) {
            if (Math.random() * chance < 1) {
                int y = findFreeVerticalCell(x);

                if (y - 1 > 0 && getType(getObject(x, y - 1)) == StaticObjectsConst.Types.SOLID && getResistance(getObject(x, y - 1)) == 100) {
                    createStructure(x, y, defPath + "\\src\\assets\\World\\Saves\\decorStone.ser");
                }
            }
        }
    }

    private static boolean checkInterInsideSolid(int xCell, int yCell, String path) {
        int width = structures.get(path).blocks.length;
        int height = structures.get(path).blocks[0].length;
        short[][] objects = structures.get(path).blocks;

        for (int x = xCell; x < xCell + width; x++) {
            for (int y = yCell; y < yCell + height; y++) {
                if (x > 0 && y > 0 && x < SizeX && y < SizeY && getType(getObject(x, y)) == StaticObjectsConst.Types.SOLID && getType(objects[x - xCell][y - yCell]) == StaticObjectsConst.Types.SOLID) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int findFreeVerticalCell(int x) {
        if (x > 0 && x < SizeX) {
            for (int y = 0; y < SizeY; y++) {
                if (getType(getObject(x, y)) == StaticObjectsConst.Types.GAS) {
                    return y;
                }
            }
        }
        return -1;
    }

    private static void generateResources() {
        log("World generator: generating resources");

        PerlinNoiseGenerator.main(SizeX, SizeY, 1, 15, 1, 0.8f, 4);

        for (int x = 0; x < SizeX; x++) {
            for (int y = 0; y < SizeY; y++) {
                if (getType(getObject(x, y + 1)) != StaticObjectsConst.Types.GAS) { // Генерация земли под блоками травы
                    setObject(x, y, createStatic("Dirt"));
                }

                if (ShadowMap.getDegree(x, y) >= 3) { // Генерация камня
                    setObject(x, y, createStatic("Stone"));
                }

                if (PerlinNoiseGenerator.noise[x][y] && ShadowMap.getDegree(x, y) >= 3) { // Генерация руды
                    setObject(x, y, createStatic("IronOre"));
                }

                if (ShadowMap.getDegree(x, y) == 2) { // Генерация перехода между землёй и камнем
                    if (!getFileName(getObject(x, y + 1)).equals("DirtStone")) {
                        setObject(x, y, createStatic("DirtStone"));
                    }
                }
            }
        }
    }

    public static void generateDynamicsObjects(boolean randomSpawn) {
        DynamicObjects.add(new DynamicWorldObjects(true, false, 0.003f, 4, 0, randomSpawn ? (int) (Math.random() * (SizeX * 16)) : SizeX * 8f, 100, defPath + "\\src\\assets\\World\\creatures\\playerRight\\player"));
        DynamicObjects.set(0, new DynamicWorldObjects(true, false, 0.003f, 4, 0, randomSpawn ? (int) (Math.random() * (SizeX * 16)) : SizeX * 8f, 100, defPath + "\\src\\assets\\World\\creatures\\playerRight\\player"));}

    public static void start(boolean generateCreatures) {
        CreatePlanet.delete();

        new Thread(new Physics()).start();
        if (generateCreatures) {
            new Thread(new CreaturesGenerate()).start();
        }
        Window.start = true;
    }
}