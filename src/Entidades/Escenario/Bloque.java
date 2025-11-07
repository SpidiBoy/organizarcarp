package Entidades.Escenario;

import Entidades.JuegoObjetos;
import SistemaDeSoporte.ObjetosID;
import mariotest.Juego;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Bloque extends JuegoObjetos {
    
    private int tileID;  // ID del tile en Tiled
    private BufferedImage sprite;  // Sprite a dibujar
    
    public Bloque(int x, int y, int width, int height, int scale) {
        super(x, y, ObjetosID.Bloque, width, height, scale);
        this.tileID = 0;
    }
    
    // Constructor con ID de tile
    public Bloque(int x, int y, int width, int height, int scale, int tileID) {
        super(x, y, ObjetosID.Bloque, width, height, scale);
        this.tileID = tileID;
        cargarSprite();
    }
    
    /**
     * Carga el sprite correspondiente al tile ID
     */
      private void cargarSprite() {
        // Accedemos al gestor de texturas y pedimos el sprite para nuestro ID
        this.sprite = Juego.getTextura().getSpritePorID(this.tileID); 
        
        // (Opcional) Un aviso útil para depurar si un tileID del CSV no tiene imagen
        if (this.sprite == null && this.tileID != -1) { // -1 es vacío
            System.err.println("[ADVERTENCIA] No se encontró un sprite para el tileID: " + this.tileID);
        }
    }
  
    @Override
    public void tick() {
        // Bloques son estáticos
    }

    @Override
    public void render(Graphics g) {
        // Esta lógica ahora funcionará como se espera.
        // Si el sprite se cargó, lo dibuja.
        if (sprite != null) {
            g.drawImage(sprite, (int)getX(), (int)getY(), 
                       (int)getWidth(), (int)getHeight(), null);
        } else {
            // Si no, dibuja los colores de antes como un respaldo visual.
            if (tileID >= 10 && tileID <= 10000) {
                g.setColor(new Color(139, 69, 19)); // Marrón
            } else if (tileID >= 1134 && tileID <= 1141) {
                g.setColor(new Color(255, 0, 0)); // Rojo
            }
            // No dibujamos nada para el ID 0 o -1 para que sea transparente
            if (tileID > 0) {
                 g.fillRect((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
                 g.setColor(Color.BLACK);
                 g.drawRect((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
            }
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    }
    
    public int getTileID() {
        return tileID;
    }
}