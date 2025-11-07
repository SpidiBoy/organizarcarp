package Entidades.Escenario;

import Entidades.JuegoObjetos;
import SistemaDeSoporte.ObjetosID;
import mariotest.Juego;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Clase TileVisual - Tiles decorativos SIN colisión
 * 
 * Se renderizan en pantalla pero Mario puede atravesarlos libremente.
 * 
 * SISTEMA DE CAPAS:
 * - esFondo = true  → Se dibuja DETRÁS de todo (fondo lejano)
 * - esFondo = false → Se dibuja DETRÁS del jugador pero ENCIMA del fondo
 * 
 * Útil para:
 * - Decoraciones de fondo (nubes, montañas)
 * - Detalles visuales (césped, flores)
 * - Elementos no interactivos
 * 
 * @author LENOVO
 */
public class TileVisual extends JuegoObjetos {
    
    private int tileID;           // ID del tile en Tiled
    private BufferedImage sprite; // Sprite a dibujar
    private boolean esFondo;      // Si es fondo (se dibuja primero)
    
    /**
     * Constructor para tile visual sin colisión
     * 
     * @param x Posición X en el mundo
     * @param y Posición Y en el mundo
     * @param width Ancho del tile
     * @param height Alto del tile
     * @param scale Escala de renderizado
     * @param tileID ID del tile en Tiled
     * @param esFondo true = fondo lejano, false = decoración de primer plano (debajo del jugador)
     */
    public TileVisual(int x, int y, int width, int height, int scale, int tileID, boolean esFondo) {
        super(x, y, ObjetosID.TileVisual, width, height, scale);
        this.tileID = tileID;
        this.esFondo = esFondo;
        cargarSprite();
    }
    
    /**
     * Constructor simplificado (por defecto NO es fondo = debajo del jugador)
     */
    public TileVisual(int x, int y, int width, int height, int scale, int tileID) {
        this(x, y, width, height, scale, tileID, false);
    }
    
    /**
     * Carga el sprite correspondiente al tile ID
     */
    private void cargarSprite() {
        this.sprite = Juego.getTextura().getSpritePorID(this.tileID);
        
        if (this.sprite == null && this.tileID > 0) {
            System.err.println("[TileVisual] Advertencia: No se encontró sprite para tileID: " + this.tileID);
        }
    }
    
    @Override
    public void tick() {
        // Los tiles visuales son estáticos, no necesitan actualización
    }

    @Override
    public void render(Graphics g) {
        // Renderizar el sprite si existe
        if (sprite != null) {
            g.drawImage(sprite, (int)getX(), (int)getY(), 
                       (int)getWidth(), (int)getHeight(), null);
        }
    }

    @Override
    public Rectangle getBounds() {
        // Retornar un rectángulo vacío porque NO tiene colisión
        // Esto hace que las colisiones con este objeto siempre fallen
        return new Rectangle(0, 0, 0, 0);
    }
    
    /**
     * Método alternativo que retorna el área visual (sin colisión)
     * Útil para efectos visuales o detección de hover
     */
    public Rectangle getAreaVisual() {
        return new Rectangle((int)getX(), (int)getY(), 
                           (int)getWidth(), (int)getHeight());
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public int getTileID() {
        return tileID;
    }
    
    public boolean isEsFondo() {
        return esFondo;
    }
    
    public void setEsFondo(boolean esFondo) {
        this.esFondo = esFondo;
    }
    /**
     * Verifica si este tile es puramente decorativo
     */
    public boolean esDecorativo() {
        return true; // Siempre es decorativo
    }
}