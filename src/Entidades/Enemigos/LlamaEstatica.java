package Entidades.Enemigos;

import Entidades.JuegoObjetos;
import Entidades.Player;
import SistemaGFX.Animacion;
import SistemaDeSoporte.Handler;
import SistemaDeSoporte.ObjetosID;
import mariotest.Juego;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Clase LlamaEstatica - Llama que NO se mueve
 * Solo animación visual, ideal para trampas fijas o decoración peligrosa
 * 
 * @author LENOVO
 */
public class LlamaEstatica extends JuegoObjetos {
    
    // Dimensiones de la llama
    private static final float WIDTH = 16F;
    private static final float HEIGHT = 24F;
    
    // Componentes
    private Handler handler;
    private BufferedImage[] llamaSprites;
    private Animacion llamaAnimacion;
    
    // Efectos visuales
    private int ticksAnimacion = 0;
    private boolean brillando = false;
    
    /**
     * Constructor de la llama estática
     */
    public LlamaEstatica(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.Fuego, WIDTH, HEIGHT, scale);
        this.handler = handler;
        
        // NO establecer velocidad (permanece en 0)
        setVelX(0);
        setVely(0);
        
        // Cargar sprites y animaciones
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[LLAMA ESTATICA] Creada en (" + x + ", " + y + ")");
    }
    
    /**
     * Carga los sprites de la llama desde Texturas
     */
    private void cargarSprites() {
        try {
            llamaSprites = Juego.getTextura().getLlamaSprites();
            
            if (llamaSprites == null || llamaSprites.length == 0) {
                System.err.println("[ERROR] No se pudieron cargar sprites de llama");
            } else {
                System.out.println("[LLAMA ESTATICA] Sprites cargados: " + llamaSprites.length);
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Excepción al cargar sprites: " + e.getMessage());
        }
    }
    
    /**
     * Inicializa las animaciones de la llama
     */
    private void inicializarAnimaciones() {
        if (llamaSprites == null || llamaSprites.length < 2) {
            System.err.println("[ERROR] No hay suficientes sprites para animación");
            return;
        }
        
        // Animación de llama
        if (llamaSprites.length >= 3) {
            llamaAnimacion = new Animacion(3, 
                llamaSprites[0], 
                llamaSprites[1], 
                llamaSprites[2]
            );
        } else {
            llamaAnimacion = new Animacion(4, 
                llamaSprites[0], 
                llamaSprites[1]
            );
        }
        
        System.out.println("[LLAMA ESTATICA] Animación inicializada");
    }

    @Override
    public void tick() {
        ticksAnimacion++;
        
        // IMPORTANTE: NO aplicar física ni movimiento
        // La llama permanece completamente estática
        
        // Solo actualizar animación
        if (llamaAnimacion != null) {
            llamaAnimacion.runAnimacion();
        }
    }
    
    @Override
    public void aplicarGravedad() {
        // NO aplicar gravedad - permanece fija
    }

    @Override
    public void render(Graphics g) {
        if (llamaSprites != null && llamaSprites[0] != null && llamaAnimacion != null) {
            // Renderizar animación
            llamaAnimacion.drawAnimacion(g, 
                (int) getX(), (int) getY(), 
                (int) getWidth(), (int) getHeight()
            );
            
        } else {
            // Placeholder visual
            g.setColor(new Color(255, 69, 0));
            int[] xPoints = {(int)(getX() + getWidth()/2), 
                            (int)getX(), 
                            (int)getX(), 
                            (int)(getX() + getWidth()/2),
                            (int)(getX() + getWidth()),
                            (int)(getX() + getWidth())};
            int[] yPoints = {(int)getY(), 
                            (int)(getY() + getHeight()/3), 
                            (int)(getY() + getHeight()),
                            (int)(getY() + 2*getHeight()/3),
                            (int)(getY() + getHeight()),
                            (int)(getY() + getHeight()/3)};
            g.fillPolygon(xPoints, yPoints, 6);
            
            // Centro amarillo
            g.setColor(Color.YELLOW);
            g.fillOval((int)(getX() + getWidth()/4), 
                      (int)(getY() + getHeight()/3), 
                      (int)(getWidth()/2), 
                      (int)(getHeight()/3));
        }
    }

    @Override
    public Rectangle getBounds() {
        // Hitbox completa (es una trampa fija)
        return new Rectangle(
            (int)(getX() + 4),
            (int)(getY() + 4),
            (int)(getWidth() - 8),
            (int)(getHeight() - 8)
        );
    }
    
    /**
     * Verifica colisión con el jugador
     */
    public boolean colisionaConJugador(Player player) {
        return getBounds().intersects(player.getBounds());
    }
    
    /**
     * Destruye la llama
     */
    
    public void destruir() {
        handler.removeObj(this);
        System.out.println("[LLAMA ESTATICA] Destruida");
    }
}