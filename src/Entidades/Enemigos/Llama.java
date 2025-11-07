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
 * Clase Llama - Enemigo tipo llama tradicional
 * Animación de fuego clásica estilo Donkey Kong
 * Se mueve horizontalmente y puede saltar ocasionalmente
 * 
 * @author LENOVO
 */
public class Llama extends JuegoObjetos {
    
    // Dimensiones de la llama
    private static final float WIDTH = 16F;
    private static final float HEIGHT = 24F; // Más alta que ancha (forma de llama)
    
    // Componentes
    private Handler handler;
    private BufferedImage[] llamaSprites;
    private Animacion llamaAnimacion;
    
    // Estados
    private boolean enSuelo = false;
    private int direccion = 1; // 1 = derecha, -1 = izquierda
    
    // Física
    private static final float VELOCIDAD = 2.0f;
    private static final float VELOCIDAD_CAIDA_MAX = 12f;
    private static final float GRAVEDAD = 0.5f;
    private static final float FUERZA_SALTO = -6f;
    
    // Comportamiento especial
    private int ticksEnSuelo = 0;
    private boolean puedeSaltar = true;
    private static final int COOLDOWN_SALTO = 120; // 2 segundos entre saltos
    private static final int PROBABILIDAD_SALTO = 5; // 5% por tick cuando puede saltar
    
    // Efectos visuales
    private int ticksAnimacion = 0;
    private boolean brillando = false;
    
    /**
     * Constructor de la llama
     */
    public Llama(float x, float y, int scale, Handler handler, int direccion) {
        super(x, y, ObjetosID.Fuego, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.direccion = direccion;
        
        // Establecer velocidad inicial
        setVelX(VELOCIDAD * direccion);
        
        // Cargar sprites y animaciones
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[LLAMA] Creada en (" + x + ", " + y + ") dirección: " + 
                          (direccion > 0 ? "DERECHA" : "IZQUIERDA"));
    }
    
    /**
     * Constructor simplificado (dirección derecha por defecto)
     */
    public Llama(float x, float y, int scale, Handler handler) {
        this(x, y, scale, handler, 1);
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
                System.out.println("[LLAMA] Sprites cargados: " + llamaSprites.length);
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Excepción al cargar sprites de llama: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicializa las animaciones de la llama
     */
    private void inicializarAnimaciones() {
        if (llamaSprites == null || llamaSprites.length < 2) {
            System.err.println("[ERROR] No hay suficientes sprites para animación de llama");
            return;
        }
        
        // Animación de llama (todos los frames disponibles)
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
        
        System.out.println("[LLAMA] Animación inicializada");
    }

    @Override
    public void tick() {
        ticksAnimacion++;
        
        // Aplicar física
        aplicarGravedad();
        
        // Aplicar movimiento
        setX(getX() + getVelX());
        setY(getY() + getVely());
        
        // Limitar velocidad de caída
        if (getVely() > VELOCIDAD_CAIDA_MAX) {
            setVely(VELOCIDAD_CAIDA_MAX);
        }
        
        // Comportamiento en suelo
        if (enSuelo) {
            ticksEnSuelo++;
            
            // Recuperar capacidad de salto
            if (ticksEnSuelo >= COOLDOWN_SALTO) {
                puedeSaltar = true;
            }
            
            // Intentar saltar ocasionalmente
            if (puedeSaltar && Math.random() * 100 < PROBABILIDAD_SALTO) {
                saltar();
            }
            
            // Mantener velocidad horizontal
            if (Math.abs(getVelX()) < VELOCIDAD) {
                setVelX(VELOCIDAD * direccion);
            }
        }
        
        // Colisiones
        manejarColisiones();
        
        // Actualizar animación
        if (llamaAnimacion != null) {
            llamaAnimacion.runAnimacion();
        }
        /*
        // Efecto de brillo ocasional
        if (ticksAnimacion % 60 == 0) { // Cada segundo
            brillando = Math.random() < 0.3; // 30% probabilidad
        }
        */
        // Eliminar si cae fuera del mapa
        if (getY() > 1000) {
            destruir();
        }
    }
    
    /**
     * Hace que la llama salte
     */
    private void saltar() {
        if (enSuelo && puedeSaltar) {
            setVely(FUERZA_SALTO);
            enSuelo = false;
            puedeSaltar = false;
            ticksEnSuelo = 0;
            System.out.println("[LLAMA] ¡Saltó!");
        }
    }
    
    /**
     * Maneja las colisiones con bloques y plataformas
     */
    private void manejarColisiones() {
        enSuelo = false;
        
        for (JuegoObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Bloque || obj.getId() == ObjetosID.Pipe) {
                
                // Colisión inferior (aterrizar en plataforma)
                if (getBounds().intersects(obj.getBounds())) {
                    setY(obj.getY() - getHeight());
                    setVely(0);
                    enSuelo = true;
                    
                    // Asegurar velocidad horizontal
                    if (Math.abs(getVelX()) < VELOCIDAD) {
                        setVelX(VELOCIDAD * direccion);
                    }
                }
                
                // Colisión superior
                if (getBoundsTop().intersects(obj.getBounds())) {
                    setY(obj.getY() + obj.getHeight());
                    setVely(0);
                }
                
                // Colisión derecha (girar)
                if (getBoundsRight().intersects(obj.getBounds())) {
                    setX(obj.getX() - getWidth());
                    direccion = -1;
                    setVelX(VELOCIDAD * direccion);
                }
                
                // Colisión izquierda (girar)
                if (getBoundsLeft().intersects(obj.getBounds())) {
                    setX(obj.getX() + obj.getWidth());
                    direccion = 1;
                    setVelX(VELOCIDAD * direccion);
                }
            }
        }
    }
    
    @Override
    public void aplicarGravedad() {
        if (!enSuelo) {
            setVely(getVely() + GRAVEDAD);
        }
    }

    @Override
    public void render(Graphics g) {
        if (llamaSprites != null && llamaSprites[0] != null && llamaAnimacion != null) {
            // Renderizar animación
            llamaAnimacion.drawAnimacion(g, 
                (int) getX(), (int) getY(), 
                (int) getWidth(), (int) getHeight()
            );
            /*
            // Efecto de brillo ocasional
            if (brillando) {
                g.setColor(new Color(255, 255, 0, 80));
                g.fillOval((int)getX() - 4, (int)getY() - 4, 
                          (int)getWidth() + 8, (int)getHeight() + 8);
            }
            */
        } else {
            // Placeholder visual
            // Base roja
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
            
            // Punta blanca
            g.setColor(Color.WHITE);
            int[] xPunta = {(int)(getX() + getWidth()/2), 
                           (int)(getX() + getWidth()/3),
                           (int)(getX() + 2*getWidth()/3)};
            int[] yPunta = {(int)getY(),
                           (int)(getY() + getHeight()/4),
                           (int)(getY() + getHeight()/4)};
            g.fillPolygon(xPunta, yPunta, 3);
        }
    }

    @Override
    public Rectangle getBounds() {
        // Hitbox más pequeña (solo la parte inferior de la llama)
        return new Rectangle(
            (int)(getX() + 4),
            (int)(getY() + getHeight()/2),
            (int)(getWidth() - 8),
            (int)(getHeight()/2)
        );
    }
    
    public Rectangle getBoundsTop() {
        return new Rectangle(
            (int)(getX() + 4),
            (int)getY(),
            (int)(getWidth() - 8),
            (int)(getHeight() / 3)
        );
    }
    
    public Rectangle getBoundsRight() {
        return new Rectangle(
            (int)(getX() + getWidth() - 5),
            (int)(getY() + 5),
            5,
            (int)(getHeight() - 10)
        );
    }
    
    public Rectangle getBoundsLeft() {
        return new Rectangle(
            (int)getX(),
            (int)(getY() + 5),
            5,
            (int)(getHeight() - 10)
        );
    }
    
    /**
     * Destruye la llama
     */
    public void destruir() {
        handler.removeObj(this);
        System.out.println("[LLAMA] Destruida en (" + (int)getX() + ", " + (int)getY() + ")");
    }
    
    /**
     * Verifica colisión con el jugador
     */
    public boolean colisionaConJugador(Player player) {
        return getBounds().intersects(player.getBounds());
    }
    
    /**
     * Fuerza un salto (útil para control externo)
     */
    public void forzarSalto() {
        if (enSuelo) {
            saltar();
        }
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public int getDireccion() {
        return direccion;
    }
    
    public void setDireccion(int direccion) {
        this.direccion = direccion;
        setVelX(VELOCIDAD * direccion);
    }
    
    public boolean isEnSuelo() {
        return enSuelo;
    }
    
    public boolean isPuedeSaltar() {
        return puedeSaltar;
    }
    
    public void setPuedeSaltar(boolean puedeSaltar) {
        this.puedeSaltar = puedeSaltar;
    }
}