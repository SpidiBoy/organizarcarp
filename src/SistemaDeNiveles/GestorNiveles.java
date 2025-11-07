/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SistemaDeNiveles;
import SistemaDeNiveles.Configuracion.PlataformaConfig;
import SistemaDeNiveles.Configuracion.ConfiguracionNivel;
import SistemaDeSoporte.Handler;
import SistemaSoporte.Spawners.BarrilSpawner;
import SistemaSoporte.Spawners.FuegoSpawner;
import SistemaDeSoporte.EstadoJuego;
import SistemaSoporte.Spawners.ItemSpawner;
import Entidades.NPCs.DiegoKong;
import Entidades.Escenario.PlataformaMovil;
import Entidades.NPCs.Princesa;
import Entidades.Player;
import Entidades.Enemigos.LlamaEstatica;
import Entidades.Items.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import mariotest.Juego;

/**
 * Gestor de Niveles - Patrón FACTORY + STRATEGY
 * Maneja carga, configuración y transiciones entre niveles
 * * @author LENOVO
 */
public class GestorNiveles {
    
    private Juego juego;
    private Handler handler;
    private EstadoNivel estadoActual;
    
    // Configuración de niveles
    private int nivelActual;
    private static final int NIVEL_INICIAL = 1;
    private static final int NIVEL_MAXIMO = 3;
    
    // Configuración específica por nivel
    private ConfiguracionNivel configActual;
    
    // Referencias a sistemas
    private BarrilSpawner barrelSpawner;
    private FuegoSpawner fuegoSpawner;
    private ItemSpawner itemSpawner;
    private TiledTMXParser tmxParser;
    
    // Sprites de animación de victoria
    private BufferedImage spriteCorazon;
    private BufferedImage spriteCorazonRoto;
    private BufferedImage[] spritesDKAgarra;
    
    // Referencias a entidades clave
    private DiegoKong diegoKong;
    private Princesa princesa;
    
    // Estado de animación de victoria
    private boolean animacionVictoriaActiva;
    private int frameAnimacionDK;
    
    // NUEVA CONSTANTE PARA MOVIMIENTO DE VICTORIA
    private static final float VELOCIDAD_ESCAPE_VICTORIA = -1.0f; // Mover hacia arriba
    
    /**
     * Constructor
     */
    public GestorNiveles(Juego juego, Handler handler) {
        this.juego = juego;
        this.handler = handler;
        this.nivelActual = NIVEL_INICIAL;
        this.estadoActual = new EstadoNivel.Jugando(juego, this);
        this.tmxParser = new TiledTMXParser(handler);
        this.animacionVictoriaActiva = false;
        
        cargarSpritesVictoria();
        
        System.out.println("[GESTOR] Inicializando nivel inicial...");
        inicializarNivel(NIVEL_INICIAL);
    }
    
    public void reiniciar() {
    this.nivelActual = 0; // Se fuerza la recarga del nivel 1
    // Reinicia el estado del juego, aunque ya se hace externamente,
    // es bueno forzarlo aquí si hay lógica de nivel dependiente.
    EstadoJuego.getInstance().reiniciar(); 
    
    // Ahora cargamos el primer nivel
    inicializarNivel(1); 
    System.out.println("[GESTOR NIVELES] Reiniciado al Nivel 1.");
    }
    
    /**
     * Carga sprites de animación de victoria
     */
    private void cargarSpritesVictoria() {
        try {
            // TODO: Cargar sprites desde Texturas
            spriteCorazon = Juego.getTextura().getCorazonSprite();
            spriteCorazonRoto = Juego.getTextura().getCorazonRotoSprite();
            spritesDKAgarra = Juego.getTextura().getDKAgarraSprites();
            
            System.out.println("[GESTOR] Sprites de victoria cargados");
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar sprites de victoria: " + e.getMessage());
        }
    }
    
    /**
     * Inicializa el nivel actual
     */
/**
 * Inicializa el nivel actual
 */
public void inicializarNivel(int nivel) {
    System.out.println("\n========================================");
    System.out.println("  INICIANDO NIVEL " + nivel);
    System.out.println("========================================");
    
    this.nivelActual = nivel;
    
    // SOLUCIÓN AL CONTADOR DE NIVEL
    // Actualizar el estado global del juego
    EstadoJuego.getInstance().setNivelActual(nivel);
    
    //CAMBIAR TEXTURAS AL NIVEL CORRESPONDIENTE
    Juego.getTextura().cambiarNivel(nivel);
    
    // Limpiar nivel anterior
    limpiarNivel();
    
    // Cargar configuración del nivel
    configActual = ConfiguracionNivel.crear(nivel);
    
    // Cargar mapa TMX
    cargarMapa(configActual.getRutaTMX());
    
    // Configurar elementos específicos del nivel
    configurarNivel(configActual);
    
    // Cambiar a estado JUGANDO
    cambiarEstado(new EstadoNivel.Jugando(juego, this));
    
    System.out.println("========================================");
    System.out.println("  NIVEL " + nivel + " CARGADO");
    System.out.println("========================================\n");
}
    
    /**
     * Limpia todos los objetos del nivel anterior
     */
    private void limpiarNivel() {
        System.out.println("[GESTOR] Limpiando nivel anterior...");
        
        // Detener spawners
        if (barrelSpawner != null) barrelSpawner.desactivar();
        if (fuegoSpawner != null) fuegoSpawner.desactivar();
        if (itemSpawner != null) itemSpawner.desactivar();
        
        // Limpiar todos los objetos excepto el jugador
        Player player = handler.getPlayer();
        handler.getGameObjs().clear();
        
        // Re-agregar jugador
        if (player != null) {
            handler.addObj(player);
        }
        
        // Resetear referencias
        diegoKong = null;
        princesa = null;
    }
    
    /**
     * Carga el mapa TMX del nivel
     */
    private void cargarMapa(String rutaTMX) {
        System.out.println("[GESTOR] Cargando mapa: " + rutaTMX);
        tmxParser.cargarMapa(rutaTMX);
    }
    
    /**
     * Configura elementos específicos del nivel
     */
    private void configurarNivel(ConfiguracionNivel config) {
        // Crear Diego Kong
        if (config.tieneDiegoKong()) {
            Point posDK = config.getPosicionDK();
            diegoKong = new DiegoKong(posDK.x, posDK.y, 2, handler);
            handler.addObj(diegoKong);
        }
        
        // Crear Princesa
        if (config.tienePrincesa()) {
            Point posPrincesa = config.getPosicionPrincesa();
            princesa = new Princesa(posPrincesa.x, posPrincesa.y, 2, handler);
            handler.addObj(princesa);
        }
        
        // Configurar spawner de barriles
        if (config.tieneBarriles()) {
            List<Point> spawnPoints = config.getBarrilSpawnPoints();
            barrelSpawner = new BarrilSpawner(handler, spawnPoints);
            if (config.isBarrilesActivos()) {
                barrelSpawner.activar();
            }
        }
        
        // Configurar spawner de fuegos
        if (config.tieneFuegos()) {
            List<Point> spawnPoints = config.getFuegoSpawnPoints();
            fuegoSpawner = new FuegoSpawner(handler, spawnPoints);
            fuegoSpawner.setMaxFuegos(config.getMaxFuegos());
            if (config.isFuegosActivos()) {
                fuegoSpawner.activar();
            }
        }
        
        if (nivelActual == 3) {
        // Martillo al inicio del nivel (garantizado)
        Martillo martillo = new Martillo(200, 280, 2, handler);
        handler.addObj(martillo);
        }
        
        // Configurar spawner de items
        if (config.tieneItems()) {
            List<Point> spawnPoints = config.getItemSpawnPoints();
            itemSpawner = new ItemSpawner(handler, spawnPoints);
            if (config.isItemsActivos()) {
                itemSpawner.activar();
            }
        }
        
        // Crear plataformas móviles específicas del nivel
        if (config.tienePlataformasMoviles()) {
            crearPlataformasMoviles(config);
        }
        
        // Crear llamas estáticas
        if (config.tieneLlamasEstaticas()) {
            crearLlamasEstaticas(config);
        }
    }
    
    /**
     * Crea plataformas móviles según configuración
     */
    private void crearPlataformasMoviles(ConfiguracionNivel config) {
        for (PlataformaConfig pConfig : config.getPlataformasMoviles()) {
            PlataformaMovil plataforma = new PlataformaMovil(
                pConfig.x, pConfig.y,
                pConfig.width, pConfig.height,
                pConfig.scale, pConfig.tileID,
                pConfig.tipo, pConfig.velocidad,
                pConfig.limiteMin, pConfig.limiteMax,
                pConfig.duracionVisible, pConfig.duracionInvisible
            );
            handler.addObj(plataforma);
        }
    }
    
    /**
     * Crea llamas estáticas según configuración
     */
    private void crearLlamasEstaticas(ConfiguracionNivel config) {
        for (Point pos : config.getPosicionesLlamasEstaticas()) {
            LlamaEstatica llama = new LlamaEstatica(pos.x, pos.y, 2, handler);
            handler.addObj(llama);
        }
    }
    
    /**
     * Verifica si el jugador llegó a la princesa (victoria)
     */
    public boolean verificarVictoria() {
        if (princesa == null || princesa.isRescatada()) {
            return false;
        }
        
        Player player = handler.getPlayer();
        if (player == null) return false;
        
        // Verificar distancia
        float distX = Math.abs(player.getX() - princesa.getX());
        float distY = Math.abs(player.getY() - princesa.getY());
        
        if (distX < 30 && distY < 30) {
            princesa.setRescatada(true);
            return true;
        }
        
        return false;
    }
    
    /**
     * Inicia la animación de victoria
     */
    public void iniciarAnimacionVictoria() {
        animacionVictoriaActiva = true;
        frameAnimacionDK = 0;
        
        System.out.println("[VICTORIA] Iniciando animación de victoria");
    }
    
    /**
     * Muestra sprite de corazón
     */
    public void mostrarCorazon() {
        System.out.println("[VICTORIA] Mostrando corazón");
        // El sprite se renderiza en renderOverlayVictoria()
    }
    
    /**
     * Anima a DK agarrando a la princesa
     */
public void animarDKAgarraPrincesa() {
    System.out.println("[VICTORIA]  DK agarra a la princesa");
    
    if (diegoKong != null && princesa != null) {
        // ✅ Activar animación especial de DK
        diegoKong.activarAnimacionAgarrar();
        
        // ✅ Mover princesa hacia DK
        float destinoX = diegoKong.getX() + 10; // Justo al lado de DK
        float destinoY = diegoKong.getY() + 5;
        princesa.moverHacia(destinoX, destinoY);
        
        System.out.println("[VICTORIA] Princesa moviéndose hacia DK");
    } else {
        System.err.println("[ERROR] DK o Princesa no encontrados para animación");
    }
}
    
    /**
     *  Mueve a DK y la princesa hacia arriba
     */
    public void moverDKYPrincesaHaciaArriba() {
        if (diegoKong != null) {
            // Mover DK hacia arriba
            diegoKong.setY(diegoKong.getY() + VELOCIDAD_ESCAPE_VICTORIA);
            
            // Forzar a la princesa a seguir a DK
            if (princesa != null) {
                // Detener cualquier movimiento automático restante
                if (princesa.isMoviendose()) {
                     princesa.detenerMovimiento();
                }
                
                // Anclar la posición de la princesa a DK
                float anclaX = diegoKong.getX() + 10;
                float anclaY = diegoKong.getY() + 5;
                princesa.setX(anclaX);
                princesa.setY(anclaY);
            }
        }
    }
    
    /**
     * Muestra sprite de corazón roto
     */
    public void mostrarCorazonRoto() {
        System.out.println("[VICTORIA] Mostrando corazón roto");
        // El sprite se renderiza en renderOverlayVictoria()
    }
    
    /**
     * Renderiza overlay de victoria
     */
 public void renderOverlayVictoria(Graphics g, int fase, int ticks) {
    if (!animacionVictoriaActiva) return;
    
    // Overlay semi-transparente
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(0, 0, Juego.getVentanaWidth(), Juego.getVentanaHeight());
    
    int centerX = Juego.getVentanaWidth() / 2;
    int centerY = Juego.getVentanaHeight() / 2;
    
    switch (fase) {
        case 0: // FASE_CORAZON
            renderCorazon(g, centerX, centerY, false);
            break;
            
        case 1: // FASE_DK_AGARRA
            renderTextoAnimacion(g, centerX, centerY, "¡DIEGO KONG AGARRA A LA PRINCESA!", Color.RED);
            break;
            
        case 2: // FASE_MOVIMIENTO
            renderTextoAnimacion(g, centerX, centerY, "¡LA ESTÁ LLEVANDO!", Color.ORANGE);
            break;
            
        case 3: // FASE_CORAZON_ROTO
            renderCorazon(g, centerX, centerY, true);
            renderTextoAnimacion(g, centerX, centerY + 60, "¡Ella se escapa otra vez!", Color.YELLOW);
            break;
    }
    
    // MOSTRAR PROGRESO AL SIGUIENTE NIVEL
    if (ticks > 180) { // Últimos 60 ticks (1 segundo)
        g.setColor(Color.CYAN);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        String textoSiguiente = "SIGUIENTE NIVEL EN " + ((240 - ticks) / 60 + 1) + "...";
        int w = g.getFontMetrics().stringWidth(textoSiguiente);
        g.drawString(textoSiguiente, centerX - w/2, Juego.getVentanaHeight() - 50);
    }
}
 
 private void renderCorazon(Graphics g, int centerX, int centerY, boolean roto) {
    BufferedImage sprite = roto ? spriteCorazonRoto : spriteCorazon;
    
    if (sprite != null) {
        // Escala 4x para que sea grande y visible
        int size = 64;
        g.drawImage(sprite, centerX - size/2, centerY - size/2, size, size, null);
    } else {
        // Placeholder si no hay sprites
        g.setColor(roto ? new Color(139, 0, 0) : Color.RED);
        g.fillOval(centerX - 30, centerY - 30, 60, 60);
        
        if (roto) {
            g.setColor(Color.BLACK);
            g.drawLine(centerX - 30, centerY - 30, centerX + 30, centerY + 30);
            g.drawLine(centerX + 30, centerY - 30, centerX - 30, centerY + 30);
        }
    }
}
 
 /**
 * Renderiza texto de animación
 */
private void renderTextoAnimacion(Graphics g, int centerX, int centerY, String texto, Color color) {
    g.setColor(color);
    g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
    
    // Centrar texto
    int textWidth = g.getFontMetrics().stringWidth(texto);
    g.drawString(texto, centerX - textWidth/2, centerY);
    
    // Sombra
    g.setColor(Color.BLACK);
    g.drawString(texto, centerX - textWidth/2 + 2, centerY + 2);
}
    
    /**
     * Carga el siguiente nivel
     */
    public void cargarSiguienteNivel() {
        int siguienteNivel = nivelActual + 1;
        
        if (siguienteNivel > NIVEL_MAXIMO) {
            System.out.println("[GESTOR] ¡Juego completado!");
            // TODO: Mostrar pantalla de victoria final
            siguienteNivel = NIVEL_INICIAL; // Reiniciar al nivel 1
        }
        
        inicializarNivel(siguienteNivel);
    }
    
    /**
     * Detiene todos los spawners
     */
    public void detenerSpawners() {
        if (barrelSpawner != null) barrelSpawner.desactivar();
        if (fuegoSpawner != null) fuegoSpawner.desactivar();
        if (itemSpawner != null) itemSpawner.desactivar();
    }
    
    /**
     * Cambia el estado del nivel
     */
    public void cambiarEstado(EstadoNivel nuevoEstado) {
        if (estadoActual != null) {
            estadoActual.salir();
        }
        
        estadoActual = nuevoEstado;
        estadoActual.entrar();
    }
    
    /**
     * Actualiza el gestor de niveles
     */
    public void tick() {
        if (estadoActual != null) {
            estadoActual.tick();
        }
        
        // Actualizar spawners solo si el estado lo permite
        if (estadoActual.permitirSpawnEnemigos()) {
            if (barrelSpawner != null) barrelSpawner.tick();
            if (fuegoSpawner != null) fuegoSpawner.tick();
            if (itemSpawner != null) itemSpawner.tick();
        }
    }
    
    /**
     * Renderiza el overlay del estado actual
     */
    public void render(Graphics g) {
        if (estadoActual != null) {
            estadoActual.render(g);
        }
    }
    
    // ==================== GETTERS ====================
    
    public int getNivelActual() {
        return nivelActual;
    }
    
    public EstadoNivel getEstadoActual() {
        return estadoActual;
    }
    
    public boolean permitirMovimientoJugador() {
        return estadoActual != null && estadoActual.permitirMovimientoJugador();
    }
    
    public BarrilSpawner getBarrelSpawner() {
        return barrelSpawner;
    }
    
    public FuegoSpawner getFuegoSpawner() {
        return fuegoSpawner;
    }
    
    public ItemSpawner getItemSpawner() {
        return itemSpawner;
    }
}