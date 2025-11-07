package Entidades.Enemigos;

import Entidades.JuegoObjetos;
import Entidades.EfectosVisuales.ParticulaFuego;
import Entidades.Player;
import SistemaGFX.Animacion;
import SistemaDeSoporte.Handler;
import SistemaDeSoporte.ObjetosID;
import mariotest.Juego;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Clase Fuego - Enemigo tipo flama
 * Se mueve horizontalmente por las plataformas
 * Similar a las llamas del Donkey Kong original
 * 
 * @author LENOVO
 */
public class Fuego extends JuegoObjetos {
    
    // Dimensiones del fuego
    private static final float WIDTH = 16F;
    private static final float HEIGHT = 16F;
    
    // Componentes
    private Handler handler;
    private BufferedImage[] fuegoSprites;
    private Animacion fuegoAnimacion;
    
    // Estados del fuego
    private boolean enSuelo = false;
    private int direccion = 1; // 1 = derecha, -1 = izquierda
    
    // Física
    private static final float VELOCIDAD_BASE = 2.0f;
    private static final float VELOCIDAD_RAPIDA = 3.5f;
    private float velocidadActual;
    private static final float VELOCIDAD_CAIDA_MAX = 12f;
    private static final float GRAVEDAD = 0.5f;
    
    // Comportamiento
    private TipoFuego tipo;
    private int ticksVivo = 0;
    private boolean puedeGirar = true;
    
    // Efectos visuales
    private Random random;
    private int ticksParticula = 0;
    private static final int FRECUENCIA_PARTICULA = 3; // Cada 3 ticks
    
    /**
     * Tipos de comportamiento del fuego
     */
    public enum TipoFuego {
        NORMAL,      // Velocidad normal, gira en bordes
        RAPIDO,      // Velocidad alta, más agresivo
        ESTATICO,    // No se mueve, solo anima (decorativo)
        PERSEGUIDOR  // Persigue al jugador (más difícil)
    }
    
    /**
     * Constructor del fuego
     */
    public Fuego(float x, float y, int scale, Handler handler, TipoFuego tipo, int direccion) {
        super(x, y, ObjetosID.Fuego, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.tipo = tipo;
        this.direccion = direccion;
        this.random = new Random();
        
        // Configurar velocidad según el tipo
        switch (tipo) {
            case RAPIDO:
                this.velocidadActual = VELOCIDAD_RAPIDA;
                break;
            case ESTATICO:
                this.velocidadActual = 0;
                break;
            default:
                this.velocidadActual = VELOCIDAD_BASE;
                break;
        }
        
        // Establecer velocidad inicial
        setVelX(velocidadActual * direccion);
        
        // Cargar sprites y animaciones
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[FUEGO] Creado en (" + x + ", " + y + ") tipo: " + tipo + 
                          " dirección: " + (direccion > 0 ? "DERECHA" : "IZQUIERDA"));
    }
    
    /**
     * Constructor simplificado (tipo normal, dirección derecha)
     */
    public Fuego(float x, float y, int scale, Handler handler) {
        this(x, y, scale, handler, TipoFuego.NORMAL, 1);
    }
    
    /**
     * Carga los sprites del fuego desde Texturas
     */
    private void cargarSprites() {
        try {
            fuegoSprites = Juego.getTextura().getFuegoSprites();
            
            if (fuegoSprites == null || fuegoSprites.length == 0) {
                System.err.println("[ERROR] No se pudieron cargar sprites de fuego");
            } else {
                System.out.println("[FUEGO] Sprites cargados: " + fuegoSprites.length);
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Excepción al cargar sprites de fuego: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Inicializa las animaciones del fuego
     */
    private void inicializarAnimaciones() {
        if (fuegoSprites == null || fuegoSprites.length < 2) {
            System.err.println("[ERROR] No hay suficientes sprites para animación de fuego");
            return;
        }
        
        // Animación de fuego (todos los frames disponibles)
        if (fuegoSprites.length >= 4) {
            fuegoAnimacion = new Animacion(4, 
                fuegoSprites[0], 
                fuegoSprites[1]

            );
        } else {
            fuegoAnimacion = new Animacion(5, 
                fuegoSprites[0], 
                fuegoSprites[1],
                fuegoSprites[2], 
                fuegoSprites[3]
            );
        }
        
        System.out.println("[FUEGO] Animación inicializada");
    }

    @Override
    public void tick() {
        ticksVivo++;
        
        // Aplicar física
        if (tipo != TipoFuego.ESTATICO) {
            aplicarGravedad();
            
            // Aplicar movimiento
            setX(getX() + getVelX());
            setY(getY() + getVely());
            
            // Limitar velocidad de caída
            if (getVely() > VELOCIDAD_CAIDA_MAX) {
                setVely(VELOCIDAD_CAIDA_MAX);
            }
        }
        
        // Comportamiento según el tipo
        switch (tipo) {
            case NORMAL:
            case RAPIDO:
                comportamientoNormal();
                break;
                
            case PERSEGUIDOR:
                comportamientoPerseguidor();
                break;
                
            case ESTATICO:
                // No hacer nada, solo animar
                break;
        }
        
        // Colisiones
        manejarColisiones();
        
        // Generar partículas de fuego
        generarParticulas();
        
        // Actualizar animación
        if (fuegoAnimacion != null) {
            fuegoAnimacion.runAnimacion();
        }
        
        // Eliminar si cae fuera del mapa
        if (getY() > 1000) {
            destruir();
        }
    }
    
    /**
     * Comportamiento normal: patrullar horizontalmente
     */
    private void comportamientoNormal() {
        // Mantener velocidad constante
        if (Math.abs(getVelX()) < velocidadActual && enSuelo) {
            setVelX(velocidadActual * direccion);
        }
    }
    
    /**
     * Comportamiento perseguidor: seguir al jugador
     */
    private void comportamientoPerseguidor() {
        Player player = handler.getPlayer();
        if (player == null || !enSuelo) return;
        
        // Solo perseguir si el jugador está cerca
        float distanciaX = player.getX() - getX();
        float distanciaY = Math.abs(player.getY() - getY());
        
        // Si está en la misma altura aproximada y cerca
        if (distanciaY < 50 && Math.abs(distanciaX) < 300) {
            // Cambiar dirección hacia el jugador
            int nuevaDireccion = distanciaX > 0 ? 1 : -1;
            
            if (nuevaDireccion != direccion && puedeGirar) {
                direccion = nuevaDireccion;
                setVelX(VELOCIDAD_RAPIDA * direccion);
            }
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
                    
                    // Asegurar que mantiene velocidad horizontal
                    if (Math.abs(getVelX()) < velocidadActual && tipo != TipoFuego.ESTATICO) {
                        setVelX(velocidadActual * direccion);
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
                    if (puedeGirar) {
                        direccion = -1;
                        setVelX(velocidadActual * direccion);
                    }
                }
                
                // Colisión izquierda (girar)
                if (getBoundsLeft().intersects(obj.getBounds())) {
                    setX(obj.getX() + obj.getWidth());
                    if (puedeGirar) {
                        direccion = 1;
                        setVelX(velocidadActual * direccion);
                    }
                }
            }
        }
    }
    
    /**
     * Genera partículas de fuego para efecto visual
     */
    private void generarParticulas() {
        ticksParticula++;
        
        if (ticksParticula >= FRECUENCIA_PARTICULA) {
            ticksParticula = 0;
            
            // Crear partícula de fuego
            float particulaX = getX() + getWidth() / 2 + (random.nextFloat() - 0.5f) * 8;
            float particulaY = getY() + getHeight() / 2;
            
            ParticulaFuego particula = new ParticulaFuego(
                particulaX, 
                particulaY, 
                1, 
                handler
            );
            
            handler.addObj(particula);
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
        if (fuegoSprites != null && fuegoSprites[0] != null && fuegoAnimacion != null) {
            // Renderizar animación
            fuegoAnimacion.drawAnimacion(g, 
                (int) getX(), (int) getY(), 
                (int) getWidth(), (int) getHeight()
            );
            
            // Brillo adicional para tipo rápido
            if (tipo == TipoFuego.RAPIDO) {
                g.setColor(new Color(255, 255, 0, 50));
                g.fillOval((int)getX() - 2, (int)getY() - 2, 
                          (int)getWidth() + 4, (int)getHeight() + 4);
            }
            
        } else {
            // Placeholder visual
            g.setColor(new Color(255, 69, 0));
            int[] xPoints = {(int)(getX() + getWidth()/2), 
                            (int)getX(), 
                            (int)getX(), 
                            (int)(getX() + getWidth())};
            int[] yPoints = {(int)getY(), 
                            (int)(getY() + getHeight()/3), 
                            (int)(getY() + getHeight()), 
                            (int)(getY() + getHeight())};
            g.fillPolygon(xPoints, yPoints, 4);
            
            // Centro brillante
            g.setColor(Color.YELLOW);
            g.fillOval((int)(getX() + getWidth()/4), 
                      (int)(getY() + getHeight()/3), 
                      (int)(getWidth()/2), 
                      (int)(getHeight()/2));
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            (int)(getX() + 3),
            (int)(getY() + 3),
            (int)(getWidth() - 6),
            (int)(getHeight() - 6)
        );
    }
    
    public Rectangle getBoundsTop() {
        return new Rectangle(
            (int)(getX() + 4),
            (int)getY(),
            (int)(getWidth() - 8),
            (int)(getHeight() / 2)
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
     * Destruye el fuego
     */
    public void destruir() {
        handler.removeObj(this);
        System.out.println("[FUEGO] Destruido en (" + (int)getX() + ", " + (int)getY() + ")");
    }
    
    /**
     * Verifica colisión con el jugador
     */
    public boolean colisionaConJugador(Player player) {
        return getBounds().intersects(player.getBounds());
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public TipoFuego getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoFuego tipo) {
        this.tipo = tipo;
        
        // Actualizar velocidad según el tipo
        switch (tipo) {
            case RAPIDO:
                this.velocidadActual = VELOCIDAD_RAPIDA;
                break;
            case ESTATICO:
                this.velocidadActual = 0;
                break;
            default:
                this.velocidadActual = VELOCIDAD_BASE;
                break;
        }
    }
    
    public int getDireccion() {
        return direccion;
    }
    
    public void setDireccion(int direccion) {
        this.direccion = direccion;
        if (tipo != TipoFuego.ESTATICO) {
            setVelX(velocidadActual * direccion);
        }
    }
    
    public boolean isEnSuelo() {
        return enSuelo;
    }
    
    public void setPuedeGirar(boolean puedeGirar) {
        this.puedeGirar = puedeGirar;
    }
    
    public int getTicksVivo() {
        return ticksVivo;
    }
}