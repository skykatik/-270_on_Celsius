package World;

//объекты мира и возможные переменные у них
public class WorldObjects {
    boolean gas, liquid, solid, plasma, sleeping, onCamera, destroyed;
    String options, path;
    int y, x, id;

    public WorldObjects(boolean destroyed, boolean onCamera, boolean gas, boolean liquid, boolean solid, boolean plasma, boolean sleeping, String options, String path, int x, int y) {
        this.destroyed = destroyed;    //уничтожен ли -> после следующего прохода будет удален из переменных
        this.onCamera = onCamera;      //находится ли в фокусе, для оптимизации
        this.gas = gas;                //является ли газом
        this.liquid = liquid;          //является ли жидкостью
        this.solid = solid;            //является ли твердым
        this.plasma = plasma;          //является ли плазмой
        this.sleeping = sleeping;      //спит (находится вне фокуса и не рисуется/неактивен)
        this.options = options;        //описание и прочее
        this.path = path;              //путь до текстуры
        this.x = x;                    //мировая координата x
        this.y = y;                    //мировая координата у
    }
}

