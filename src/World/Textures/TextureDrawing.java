package World.Textures;

import com.sun.jdi.ByteValue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glTexCoord2i;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class TextureDrawing {
    public static void draw(String path, int x, int y, ByteBuffer buffer, BufferedImage image) {
        //если при вызове не приходят буфферы, то сам декодирует их исходя из пути
        if(buffer == null){
            System.err.println("buffer is null");
            buffer = TextureLoader.ByteBufferEncoder(path);
        }
        if(image == null) {
            System.err.println("image is null");
            image = TextureLoader.BufferedImageEncoder(path);
        }
        //считывает размеры экрана
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        glOrtho(0, dim.height, dim.width, 0, -1.0, 1.0);

        //параметры, бинд текстур, и прочее
        glActiveTexture(glGenTextures());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        //очистка и рисовка квада на экране
        //РИСОВКА ОБЯЗАТЕЛЬНО ИДЕТ МЕЖДУ glBegin(); и glEnd();
        //введите сюда кодразрешение рисовки/наложения текстур
        glEnable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);

        //верхний левый угол
        glTexCoord2i(0, 0);
        glVertex2i(x, y);
        //нижний левый угол
        glTexCoord2i(0, 1);
        glVertex2i(0 + x, image.getWidth() * 2 + y);
        //нижний правый угол
        glTexCoord2i(1, 1);
        glVertex2i(image.getHeight() + x, image.getWidth() * 2 + y);
        //верхний правый угол
        glTexCoord2i(1, 0);
        glVertex2i(image.getHeight() + x, 0 + y);

        //glVertex2i Задает вершины
        //glTexCoord2i Задает текущие координаты текстуры

        //glEnd();
        glDisable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);
    }
}
