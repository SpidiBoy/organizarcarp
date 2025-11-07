package Entidades;

import SistemaDeSoporte.Handler;
import SistemaDeSoporte.EstadoJuego;
import SistemaDeSoporte.ObjetosID;
import SistemaDeSoporte.PoderMartillo;
import SistemaDeSoporte.EstadoVidaPlayer;
import Entidades.Escenario.Escalera;
import SistemaGFX.Animacion;
import SistemaGFX.Texturas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import mariotest.*;

/**
 * Jugador mejorado con sistema de escaleras y colisiones corregidas
 * FIX: Permite salir de escalera al tocar bloques superiores
 */
public class Player extends JuegoObjetos {
    private static final float WIDTH = 16;
    private static final float HEIGHT = 16;
    private static final float WIDTH_MARTILLO = 32;
    private static final float HEIGHT_MARTILLO = 32;
    // Estados del jugador
    private Handler handler;
    private Texturas textura;
    private BufferedImage[] spriteS;
    
    private PoderMartillo poderMartillo;
    private boolean tieneMartillo;
    private BufferedImage[] spriteMartillo;
    private Animacion playerCaminaMartillo;
    private EstadoVidaPlayer estadoVida;
    private BufferedImage[] spriteMuerte;
    private Point puntoSpawn;
    // Animaciones
    private Animacion playerCaminaS;
    private Animacion playerSubeEscalera;
    private Animacion playerBajaEscalera;
    private Animacion playerCaminaL;
    private BufferedImage[] currSprite;
    private Animacion currAnimacion;
    private Animacion muerteAnimacion;
    
    // Flags de movimiento
    private boolean salto = false;
    private boolean adelante = true;
    private boolean enEscalera = false;
    private boolean puedeMoverseEnEscalera = false;
    private boolean subiendoEscalera = false;
    private boolean bajandoEscalera = false;
    private boolean invulnerable;
    private int ticksInvulnerabilidad;
    private static final int DURACION_INVULNERABILIDAD = 120;
    
    // Física
    private static final float VELOCIDAD_CAMINAR = 2.2f;
    private static final float VELOCIDAD_ESCALERA = 1f;
    private static final float FUERZA_SALTO = -7.5f;
    private static final float GRAVEDAD = 0.5f;
    
    // Escalera actual
    private Escalera escaleraActual = null;
    
    // Control de salida de escalera
    private int ticksEnEscalera = 0;
    private static final int TICKS_MIN_ESCALERA = 5; // Frames mínimos antes de poder salir
    
    public Player(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.Jugador, WIDTH, HEIGHT, scale);
        this.handler = handler;
        estadoVida = new EstadoVidaPlayer.Vivo(this);
        estadoVida.entrar();
        puntoSpawn = new Point((int)x, (int)y);
        invulnerable = false;
        ticksInvulnerabilidad = 0;
        this.poderMartillo = new PoderMartillo(this, handler);
        this.tieneMartillo = false;
        this.textura = Juego.getTextura();
        spriteMuerte = textura.getMarioMuerte();
        spriteMartillo = textura.getMarioMartillo();
        spriteS = textura.getMarioS();
        
        // Inicializar animaciones
        playerCaminaS = new Animacion(5, spriteS[1], spriteS[2], spriteS[3]);
        
        // Animación de subir escalera (sprites 5-11)
        playerSubeEscalera = new Animacion(5, spriteS[5], spriteS[6], spriteS[7], 
                                           spriteS[8], spriteS[9], spriteS[10], spriteS[11]);
        
        // Animación de bajar escalera (misma que subir, se puede invertir o usar otros sprites)
        playerBajaEscalera = new Animacion(5, spriteS[5], spriteS[6], spriteS[7], 
                                           spriteS[8], spriteS[9], spriteS[10], spriteS[11]);
         
        playerCaminaMartillo = new Animacion(5, spriteMartillo[0], spriteMartillo[1],spriteMartillo[2],spriteMartillo[3],
        spriteMartillo[4],
        spriteMartillo[5]);
        muerteAnimacion = new Animacion(50,spriteMuerte[0],spriteMuerte[1],spriteMuerte[2],spriteMuerte[3],spriteMuerte[4]);
        
        currSprite = spriteS;
        currAnimacion = playerCaminaS;
    }

    @Override
    public void tick() {
        // Detectar si está en una escalera
        if (estadoVida != null) {
            estadoVida.tick();
        } else {
            System.err.println("[ERROR] estadoVida es NULL! Reinicializando a VIVO...");
            estadoVida = new EstadoVidaPlayer.Vivo(this);
            estadoVida.entrar();
        }
        
        //  Si está muerto o muriendo, NO ejecutar lógica normal
        if (estadoVida instanceof EstadoVidaPlayer.Muerto || 
            estadoVida instanceof EstadoVidaPlayer.Muriendo) {
            return;
        }
        verificarEscalera();
        
        // Aplicar física según el estado
        if (enEscalera) {
            // En escalera: no hay gravedad automática
            ticksEnEscalera++;
            
            // Verificar si debe salir de la escalera al tocar un bloque arriba
            if (subiendoEscalera && ticksEnEscalera > TICKS_MIN_ESCALERA) {
                verificarSalidaSuperiorEscalera();
            }
            
            // NUEVO: Verificar si debe salir al llegar al final de la escalera bajando
            if (bajandoEscalera && ticksEnEscalera > TICKS_MIN_ESCALERA) {
                verificarSalidaInferiorEscalera();
            }
        } else {
            // Fuera de escalera: aplicar gravedad normal
            aplicarGravedad();
            ticksEnEscalera = 0;
        }
        
        if (poderMartillo != null) {
        poderMartillo.tick();
        tieneMartillo = poderMartillo.isActivo();
    }

        
        // Aplicar movimiento
        setX(getVelX() + getX());
        setY(getVely() + getY());
         //  LÍMITE DE SEGURIDAD: Si cae demasiado, forzar muerte
        if (getY() > 2000) { // Muy lejos del mapa
            System.err.println("[EMERGENCIA] Player cayó a Y=" + getY() + " - Forzando muerte");
            if (estadoVida instanceof EstadoVidaPlayer.Vivo) {
                recibirDanio(null); // Muerte forzada
            }
            return; // Detener tick
        }
        
        if (estadoVida.tieneColision()) {
        colisiones();
    }
    
    // Verificar colisiones con enemigos
       verificarColisionesEnemigos();
        
        // Actualizar animación según el estado
        actualizarAnimacion();
    }
    
private void verificarColisionesEnemigos() {
    // No recibir daño si es invulnerable o no puede recibir daño
    if (!estadoVida.puedeRecibirDanio() || invulnerable) {
        return;
    }
    
    // Si tiene martillo activo, no recibe daño (el martillo destruye enemigos)
    if (tieneMartillo && poderMartillo.isActivo()) {
        return;
    }
    
    try {
        for (JuegoObjetos obj : handler.getGameObjs()) {
            if (obj == null) continue; // SKIP null objects
            
            ObjetosID id = obj.getId();
            
            // Verificar colisión con enemigos
            boolean esEnemigo = (id == ObjetosID.Barril || 
                                id == ObjetosID.Fuego || 
                                id == ObjetosID.DiegoKong);
            
            if (esEnemigo && getBounds().intersects(obj.getBounds())) {
                recibirDanio(obj);
                break; // Solo procesar una colisión por tick
            }
        }
    } catch (Exception e) {
        System.err.println("[ERROR] Excepción en verificarColisionesEnemigos: " + e.getMessage());
        e.printStackTrace();
    }
}
 
public void recibirDanio(JuegoObjetos enemigo) {
    // MANEJAR MUERTE POR CAÍDA (enemigo = null)
    if (enemigo == null) {
        System.out.println("[PLAYER] ¡Recibió daño por CAÍDA AL VACÍO!");
    } else {
        System.out.println("[PLAYER] ¡Recibió daño de: " + enemigo.getId() + "!");
    }
 
    // Obtener estado del juego
    EstadoJuego estado = EstadoJuego.getInstance();
    
    // Perder una vida
    estado.perderVida();
    
    // Resetear racha de combos
    estado.resetearRacha();
    
    // Desactivar martillo si lo tenía
    if (tieneMartillo) {
        poderMartillo.desactivar();
        tieneMartillo = false;
    }
    
    // Cambiar a estado MURIENDO
    cambiarEstadoVida(new EstadoVidaPlayer.Muriendo(this));
}

// ==================== CAMBIAR ESTADO DE VIDA ====================
/**
 * Cambia el estado de vida del jugador (Patrón STATE)
 */
public void cambiarEstadoVida(EstadoVidaPlayer nuevoEstado) {
    if (estadoVida != null) {
        estadoVida.salir();
    }
    
    estadoVida = nuevoEstado;
    estadoVida.entrar();
}

// ==================== RESPAWNEAR ====================
/**
 * Respawnea al jugador en el punto de spawn
 */
public void respawnear() {
    if (puntoSpawn == null) {
        System.err.println("[ERROR] Punto de spawn no establecido! Usando posición por defecto.");
        puntoSpawn = new Point(100, 400); // Fallback
    }
    
    System.out.println("[PLAYER] Respawneando en (" + puntoSpawn.x + ", " + puntoSpawn.y + ")");
    
    //  RESETEAR POSICIÓN
    setX(puntoSpawn.x);
    setY(puntoSpawn.y);
    
    //  RESETEAR FÍSICA COMPLETAMENTE
    setVelX(0);
    setVely(0);
    setSalto(false);
    
    //  SALIR DE ESCALERA SI ESTABA EN UNA
    if (enEscalera) {
        salirEscalera();
    }
    
    //  DESACTIVAR MARTILLO SI LO TENÍA
    if (tieneMartillo && poderMartillo != null) {
        poderMartillo.desactivar();
        tieneMartillo = false;
    }
    
    //  RESETEAR ANIMACIÓN A REPOSO
    currAnimacion = playerCaminaS;
    adelante = true;
    
    //  ACTIVAR INVULNERABILIDAD TEMPORAL (se maneja en estado Respawneando)
    // No es necesario hacerlo aquí, el estado lo maneja
    
    System.out.println("[PLAYER] Respawn completado");
}

// ==================== ESTABLECER PUNTO DE SPAWN ====================
/**
 * Establece un nuevo punto de spawn
 */
public void setPuntoSpawn(int x, int y) {
    puntoSpawn = new Point(x, y);
    System.out.println("[PLAYER] Nuevo punto de spawn: (" + x + ", " + y + ")");
}
    
    /**
     * Verifica si el jugador debe salir de la escalera al llegar abajo
     */
    private void verificarSalidaInferiorEscalera() {
        if (!enEscalera || !bajandoEscalera) return;
        
        // Verificar si hay un bloque sólido justo debajo
        boolean hayBloqueDebajo = false;
        boolean hayEscaleraDebajo = false;
        
        Rectangle areaDebajo = new Rectangle(
            (int)(getX() + getWidth() / 4),
            (int)(getY() + getHeight()),
            (int)(getWidth() / 2),
            10
        );
        
        for (JuegoObjetos obj : handler.getGameObjs()) {
            // Verificar bloques sólidos
            if (obj.getId() == ObjetosID.Bloque || obj.getId() == ObjetosID.Pipe) {
                if (areaDebajo.intersects(obj.getBounds())) {
                    hayBloqueDebajo = true;
                    
                    // Posicionar sobre el bloque
                    if (Math.abs(getY() + getHeight() - obj.getY()) < 15) {
                        setY(obj.getY() - getHeight());
                    }
                }
            }
            
            // Verificar si hay escalera para continuar
            if (obj.getId() == ObjetosID.Escalera) {
                if (areaDebajo.intersects(obj.getBounds())) {
                    hayEscaleraDebajo = true;
                }
            }
        }
        
        // Si hay bloque pero NO hay escalera, salir
        if (hayBloqueDebajo && !hayEscaleraDebajo) {
            salirEscalera();
            setVely(0);
            salto = false;
            System.out.println("[PLAYER] Salió de escalera al llegar abajo");
        }
    }
    
    /**
     * Verifica si el jugador debe salir de la escalera al llegar arriba
     * Detecta bloques sólidos justo encima de la cabeza del jugador
     */
    private void verificarSalidaSuperiorEscalera() {
        if (!enEscalera || !subiendoEscalera) return;
        
        // Verificar si hay un bloque sólido cerca de la parte superior
        for (JuegoObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Bloque) {
                Rectangle bloqueArea = obj.getBounds();
                
                // Crear área de detección en la parte superior del jugador
                Rectangle areaDeteccion = new Rectangle(
                    (int)(getX() + getWidth() / 4),
                    (int)(getY() - 10), // 10 píxeles por encima de la cabeza
                    (int)(getWidth() / 2),
                    15 // Altura de detección
                );
                
                // Si el área de detección toca el bloque
                if (areaDeteccion.intersects(bloqueArea)) {
                    // Posicionar al jugador SOBRE el bloque
                    float nuevaY = bloqueArea.y - getHeight();
                    
                    // Solo si está cerca de la superficie del bloque
                    if (Math.abs(getY() - nuevaY) < 20) {
                        setY(nuevaY);
                        salirEscalera();
                        setVely(0);
                        salto = false;
                        
                        System.out.println("[PLAYER] Salió de escalera al tocar bloque superior");
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Actualiza y ejecuta la animación apropiada según el estado del jugador
     */
private void actualizarAnimacion() {
    if (enEscalera) {
        // ====== EN ESCALERA (siempre usa animación normal) ======
        if (subiendoEscalera) {
            currAnimacion = playerSubeEscalera;
            currAnimacion.runAnimacion();
        } else if (bajandoEscalera) {
            currAnimacion = playerBajaEscalera;
            currAnimacion.runAnimacion();
        }
        // Si está quieto en escalera, no correr animación
        
    } else {
        // ====== FUERA DE ESCALERA ======
        
        //  SELECCIONAR ANIMACIÓN SEGÚN SI TIENE MARTILLO
        if (tieneMartillo) {
            // Con martillo: usar animación especial
            currAnimacion = playerCaminaMartillo;
        } else {
            // Sin martillo: usar animación normal
            currAnimacion = playerCaminaS;
        }
        
        // Ejecutar animación solo si se está moviendo horizontalmente
        if (getVelX() != 0 && !salto) {
            currAnimacion.runAnimacion();
        }
    }
}
    
    /**
     * Verifica si el jugador está tocando una escalera
     * Detecta escaleras tanto en contacto directo como debajo
     */
    private void verificarEscalera() {
        escaleraActual = null;
        puedeMoverseEnEscalera = false;
        
        // Revisar todas las escaleras
        for (JuegoObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Escalera || obj.getId() == ObjetosID.EscaleraRota) {
                Escalera escalera = (Escalera) obj;
                
                // ÁREA 1: Contacto directo (para subir/estar en escalera)
                Rectangle areaEscalera = escalera.getAreaInteraccion();
                Rectangle jugadorBounds = getBounds();
                
                if (areaEscalera.intersects(jugadorBounds)) {
                    if (escalera.esUsable()) {
                        escaleraActual = escalera;
                        puedeMoverseEnEscalera = true;
                        break;
                    }
                }
                
                // ÁREA 2: Escalera debajo (para bajar desde plataforma)
                Rectangle areaDebajo = new Rectangle(
                    (int)(getX() + getWidth() / 4),
                    (int)(getY() + getHeight() - 5), // Desde los pies
                    (int)(getWidth() / 2),
                    25 // Buscar hasta 25 píxeles hacia abajo
                );
                
                if (areaDebajo.intersects(escalera.getBounds()) && escalera.esUsable()) {
                    escaleraActual = escalera;
                    puedeMoverseEnEscalera = true;
                    break;
                }
            }
        }
        
        // Si no está tocando una escalera Y no está bajando activamente, salir del modo escalera
        if (!puedeMoverseEnEscalera && enEscalera && !bajandoEscalera) {
            salirEscalera();
        }
    }
    
    /**
     * Sistema de colisiones 
     * Permite atravesar bloques al subir Y bajar escaleras
     */
    private void colisiones() {
        for (int i = 0; i < handler.getGameObjs().size(); i++) {
            JuegoObjetos temp = handler.getGameObjs().get(i);
            
            // Ignorar colisiones con TileVisual (son decorativos)
            if (temp.getId() == ObjetosID.TileVisual) {
                continue;
            }
            
            // Colisión con bloques y tuberías
            if (temp.getId() == ObjetosID.Bloque || temp.getId() == ObjetosID.Pipe) {
                
                // ============================================
                // PRIORIDAD 1: Si está bajando por escalera, NO COLISIONAR
                // ============================================
                if (enEscalera && bajandoEscalera) {
                    continue; // Ignorar TODAS las colisiones al bajar
                }
                
                // ============================================
                // PRIORIDAD 2: Si está subiendo por escalera, NO COLISIONAR
                // ============================================
                if (enEscalera && subiendoEscalera) {
                    continue; // Ignorar TODAS las colisiones al subir
                }
                
                // ============================================
                // COLISIONES NORMALES (solo si NO está en escalera)
                // ============================================
                
                // COLISIÓN INFERIOR (aterrizar en plataforma)
                if (getBounds().intersects(temp.getBounds())) {
                    setY(temp.getY() - getHeight());
                    setVely(0);
                    salto = false;
                }
                
                // COLISIÓN SUPERIOR (golpear la cabeza)
                if (getBoundsTop().intersects(temp.getBounds())) {
                    setY(temp.getY() + temp.getHeight());
                    setVely(0);
                }
                
                // COLISIÓN DERECHA
                if (getBoundsRight().intersects(temp.getBounds())) {
                    setX(temp.getX() - getWidth());
                }
                
                // COLISIÓN IZQUIERDA
                if (getBoundsLeft().intersects(temp.getBounds())) {
                    setX(temp.getX() + temp.getWidth());
                }
            }
        }
    }
    
    @Override
    public void aplicarGravedad() {
        if (!enEscalera) {
            setVely(getVely() + GRAVEDAD);
        }
    }
@Override
public void render(Graphics g) {
    // Si está en estado MUERTO, no renderizar
    if (estadoVida instanceof EstadoVidaPlayer.Muerto) {
        return;
    }
    
    // Si está RESPAWNEANDO y parpadeando, alternar visibilidad
    if (estadoVida instanceof EstadoVidaPlayer.Respawneando) {
        EstadoVidaPlayer.Respawneando respawn = (EstadoVidaPlayer.Respawneando) estadoVida;
        if (respawn.isParpadeando()) {
            return; // No renderizar durante parpadeo
        }
    }
    
    // ====================  ANIMACIÓN DE MUERTE ====================
    if (estadoVida instanceof EstadoVidaPlayer.Muriendo) {
        renderAnimacionMuerte(g);
        return;
    }
   
    // ====== RENDERIZADO NORMAL (código existente) ======
    BufferedImage[] spritesActuales;
    Animacion animacionCaminar;
    int anchoRender, altoRender;
    int xRender, yRender;
    
    if (tieneMartillo && !enEscalera) {
        spritesActuales = spriteMartillo;
        animacionCaminar = playerCaminaMartillo;
        anchoRender = (int) getWidth() * 2;
        altoRender = (int) getHeight() * 2;
        xRender = (int) getX() - (int) getWidth() / 2;
        yRender = (int) getY() - (int) getHeight();
    } else {
        spritesActuales = currSprite;
        animacionCaminar = currAnimacion;
        anchoRender = (int) getWidth();
        altoRender = (int) getHeight();
        xRender = (int) getX();
        yRender = (int) getY();
    }
    
    if (spritesActuales == null || spritesActuales.length == 0) {
        spritesActuales = currSprite;
        animacionCaminar = currAnimacion;
        anchoRender = (int) getWidth();
        altoRender = (int) getHeight();
        xRender = (int) getX();
        yRender = (int) getY();
    }
    
    // Renderizar según estado
    if (enEscalera) {
        if (subiendoEscalera || bajandoEscalera) {
            currAnimacion.drawAnimacion(g, (int) getX(), (int) getY(), 
                                       (int) getWidth(), (int) getHeight());
        } else {
            g.drawImage(spriteS[5], (int) getX(), (int) getY(), 
                       (int) getWidth(), (int) getHeight(), null);
        }
    } else if (salto) {
        BufferedImage spriteJump = spritesActuales[3];
        if (adelante) {
            g.drawImage(spriteJump, xRender, yRender, anchoRender, altoRender, null);
        } else {
            g.drawImage(spriteJump, xRender + anchoRender, yRender, 
                       -anchoRender, altoRender, null);
        }
    } else if (getVelX() > 0) {
        animacionCaminar.drawAnimacion(g, xRender, yRender, anchoRender, altoRender);
        adelante = true;
    } else if (getVelX() < 0) {
        animacionCaminar.drawAnimacion(g, xRender + anchoRender, yRender, 
                                       -anchoRender, altoRender);
        adelante = false;
    } else {
        BufferedImage spriteReposo = spritesActuales[0];
        if (adelante) {
            g.drawImage(spriteReposo, xRender, yRender, anchoRender, altoRender, null);
        } else {
            g.drawImage(spriteReposo, xRender + anchoRender, yRender, 
                       -anchoRender, altoRender, null);
        }
    }
    
    // Renderizar efecto del martillo
    if (poderMartillo != null && tieneMartillo) {
        poderMartillo.render(g);
    }
    
    // Indicador visual de invulnerabilidad
    if (invulnerable && !(estadoVida instanceof EstadoVidaPlayer.Respawneando)) {
        g.setColor(new Color(255, 255, 0, 100));
        g.fillOval((int)getX() - 5, (int)getY() - 5, 
                  (int)getWidth() + 10, (int)getHeight() + 10);
    }
    
}
private void renderAnimacionMuerte(Graphics g) { 
    // USAR LA ANIMACIÓN CORRECTAMENTE
    // Ejecutar la animación (esto actualiza el frame interno)
    muerteAnimacion.runAnimacion();
    
    // Renderizar el frame actual de la animación
    if (adelante) {
        muerteAnimacion.drawAnimacion(g, 
            (int)getX(), (int)getY(), 
            (int)getWidth(), (int)getHeight()
        );
    } else {
        // Voltear horizontalmente si miraba a la izquierda
        muerteAnimacion.drawAnimacion(g, 
            (int)(getX() + getWidth()), (int)getY(), 
            (int)-getWidth(), (int)getHeight()
        );
    }
    
    // ==================== SOLO EFECTOS VISUALES (SIN CÍRCULOS) ====================
    
    EstadoVidaPlayer.Muriendo muriendo = (EstadoVidaPlayer.Muriendo) estadoVida;
    int frame = muriendo.getFrameActual();
    
    // Frame 0: Flash blanco de impacto (MUY SUTIL)
    if (frame == 1) {
        g.setColor(new Color(255, 255, 255, 80));
        g.fillRect((int)getX() - 2, (int)getY() - 2, 
                  (int)getWidth() + 4, (int)getHeight() + 4);
    }
}

public void activarMartillo() {
        if (poderMartillo != null) {
            poderMartillo.activar();
            tieneMartillo = true;
            System.out.println("[PLAYER] ¡Martillo activado!");
        }
    }

    public void activarMartillo(int duracionSegundos) {
        if (poderMartillo != null) {
            poderMartillo.activar(duracionSegundos * 60); // Convertir a ticks
            tieneMartillo = true;
        }
    }
    
    /**
     * Ejecuta un golpe con el martillo
     * Llamar cuando el jugador presione el botón de ataque
     */
    public void golpearConMartillo() {
        if (poderMartillo != null && tieneMartillo) {
            poderMartillo.golpear();
        }
    }
    
    /**
     * Verifica si el jugador tiene el martillo activo
     */
    public boolean tieneMartillo() {
        return tieneMartillo;
    }
    
    /**
     * Obtiene el poder del martillo
     */
    public PoderMartillo getPoderMartillo() {
        return poderMartillo;
    }

    @Override
    public Rectangle getBounds() {
        // Pie del jugador para colisiones con el suelo
        return new Rectangle(
            (int)(getX() + getWidth() / 4),
            (int)(getY() + getHeight() / 2),
            (int) getWidth() / 2,
            (int) getHeight() / 2
        );
    }
    
    public Rectangle getBoundsTop() {
        // Cabeza del jugador
        return new Rectangle(
            (int) (getX() + getWidth() / 4),
            (int) getY(),
            (int) getWidth() / 2,
            (int) getHeight() / 2
        );
    }
    
    public Rectangle getBoundsRight() {
        // Lado derecho
        return new Rectangle(
            (int) (getX() + getWidth() - 5),
            (int) getY() + 5,
            5,
            (int) getHeight() - 10
        );
    }
    
    public Rectangle getBoundsLeft() {
        // Lado izquierdo
        return new Rectangle(
            (int) getX(),
            (int) (getY() + 5),
            5,
            (int) (getHeight() - 10)
        );
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Inicia el salto del jugador
     */
    public void iniciarSalto() {
        if (!salto && !enEscalera) {
            setVely(FUERZA_SALTO);
            salto = true;
           
        }
    }
    
    /**
     * Mueve al jugador hacia arriba en una escalera
     */
    public void subirEscalera() {
        if (tieneMartillo) {
        return;
        }
        if (puedeMoverseEnEscalera && escaleraActual != null) {
            if (!enEscalera) {
                // Centrar al jugador en la escalera al entrar
                float centroEscalera = escaleraActual.getX() + escaleraActual.getWidth() / 2;
                setX(centroEscalera - getWidth() / 2);
            }
            
            enEscalera = true;
            subiendoEscalera = true;
            bajandoEscalera = false;
            setVely(-VELOCIDAD_ESCALERA);
            setVelX(0); // Detener movimiento horizontal
            salto = false; // Resetear salto
        }
    }
    
    /**
     * Mueve al jugador hacia abajo en una escalera
     * MEJORADO: FUERZA la entrada desde plataformas sólidas
     */
    public void bajarEscalera() {
        if (tieneMartillo) {
        return;
    }
        // CASO 1: Ya está en una escalera, continuar bajando
        if (enEscalera && escaleraActual != null) {
            bajandoEscalera = true;
            subiendoEscalera = false;
            setVely(VELOCIDAD_ESCALERA);
            setVelX(0);
            return;
        }
        
        // CASO 2: NO está en escalera, pero hay una cerca
        if (puedeMoverseEnEscalera && escaleraActual != null) {
            // Centrar al jugador en la escalera
            float centroEscalera = escaleraActual.getX() + escaleraActual.getWidth() / 2;
            setX(centroEscalera - getWidth() / 2);
            
            // FORZAR entrada a modo escalera
            enEscalera = true;
            bajandoEscalera = true;
            subiendoEscalera = false;
            setVely(VELOCIDAD_ESCALERA);
            setVelX(0);
            salto = false;
            
            System.out.println("[PLAYER] Forzó entrada a escalera desde plataforma");
            return;
        }
        
        // CASO 3: No hay escalera detectada, buscar manualmente debajo
        buscarYEntrarEscaleraDebajo();
    }
    
    /**
     * Busca activamente una escalera debajo y FUERZA la entrada
     * Este método es más agresivo que verificarEscalera()
     */
    private void buscarYEntrarEscaleraDebajo() {
        // Área de búsqueda AMPLIA: hasta 30 píxeles hacia abajo
        Rectangle areaBusqueda = new Rectangle(
            (int)(getX() + getWidth() / 4),
            (int)(getY() + getHeight() - 5),
            (int)(getWidth() / 2),
            30 // Búsqueda extendida
        );
        
        for (JuegoObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Escalera) {
                Escalera escalera = (Escalera) obj;
                
                if (escalera.esUsable() && areaBusqueda.intersects(escalera.getBounds())) {
                    // ¡FORZAR ENTRADA!
                    escaleraActual = escalera;
                    puedeMoverseEnEscalera = true;
                    
                    // Centrar en la escalera
                    float centroEscalera = escalera.getX() + escalera.getWidth() / 2;
                    setX(centroEscalera - getWidth() / 2);
                    
                    // Activar modo escalera INMEDIATAMENTE
                    enEscalera = true;
                    bajandoEscalera = true;
                    subiendoEscalera = false;
                    setVely(VELOCIDAD_ESCALERA);
                    setVelX(0);
                    salto = false;
                    
                    System.out.println("[PLAYER] Forzó entrada a escalera (búsqueda activa)");
                    return;
                }
            }
        }
        
        System.out.println("[PLAYER] No se encontró escalera debajo (buscó hasta 30px)");
    }
    
    /**
     * Detiene el movimiento vertical en la escalera
     */
    public void detenerMovimientoVertical() {
        if (enEscalera) {
            subiendoEscalera = false;
            bajandoEscalera = false;
            setVely(0);
        }
    }
    
    /**
     * Sale de la escalera y retoma el movimiento normal
     */
    public void salirEscalera() {
        if (enEscalera) {
            enEscalera = false;
            subiendoEscalera = false;
            bajandoEscalera = false;
            setVely(0);
            ticksEnEscalera = 0;
        }
    }
    
    /**
     * Mueve al jugador a la izquierda
     */
    public void moverIzquierda() {
        if (!enEscalera) {
            setVelX(-VELOCIDAD_CAMINAR);
        } else {
            // Si está en escalera y presiona A/D, salir de la escalera
            salirEscalera();
            setVelX(-VELOCIDAD_CAMINAR);
        }
    }
    
    /**
     * Mueve al jugador a la derecha
     */
    public void moverDerecha() {
        if (!enEscalera) {
            setVelX(VELOCIDAD_CAMINAR);
        } else {
            // Si está en escalera y presiona A/D, salir de la escalera
            salirEscalera();
            setVelX(VELOCIDAD_CAMINAR);
        }
    }
    
    /**
     * Detiene el movimiento horizontal
     */
    public void detenerMovimiento() {
        if (!enEscalera) {
            setVelX(0);
        }
    }
    
    public boolean hasSalto() {
        return salto;
    }
    
    public void setSalto(boolean hasSalto) {
        this.salto = hasSalto;
    }
    
    public boolean isEnEscalera() {
        return enEscalera;
    }
    
    public boolean isPuedeMoverseEnEscalera() {
        return puedeMoverseEnEscalera;
    }
    
    public boolean isSubiendoEscalera() {
        return subiendoEscalera;
    }
    
    public boolean isBajandoEscalera() {
        return bajandoEscalera;
    }
    
    public Escalera getEscaleraActual() {
        return escaleraActual;
    }
    
    public boolean isInvulnerable() {
    return invulnerable;
}

public void setInvulnerable(boolean invulnerable) {
    this.invulnerable = invulnerable;
    this.ticksInvulnerabilidad = 0;
}

public EstadoVidaPlayer getEstadoVida() {
    return estadoVida;
}

public boolean estaVivo() {
    return estadoVida instanceof EstadoVidaPlayer.Vivo || 
           estadoVida instanceof EstadoVidaPlayer.Respawneando;
}

public boolean estaMuriendo() {
    return estadoVida instanceof EstadoVidaPlayer.Muriendo;
}

public boolean estaMuerto() {
    return estadoVida instanceof EstadoVidaPlayer.Muerto;
}

public Point getPuntoSpawn() {
    return puntoSpawn;
}
}