package SistemaSoporte.Spawners;

import Entidades.Enemigos.Fuego;
import SistemaDeSoporte.Handler;
import SistemaDeSoporte.ObjetosID;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FuegoSpawner - Sistema de generación de enemigos tipo fuego
 * Similar a BarrilSpawner pero para enemigos de llama
 * 
 * @author LENOVO
 */
public class FuegoSpawner {
    private Handler handler;
    private List<Point> spawnPoints;
    private Random random;
    
    // Configuración de spawn
    private int ticksDesdeUltimoSpawn;
    private int ticksEntreSpawns;
    private boolean activo;
    private int maxFuegos; // Límite de fuegos simultáneos
    
    // Constantes
    private static final int TICKS_MIN_SPAWN = 180;  // 3 segundos
    private static final int TICKS_MAX_SPAWN = 420;  // 7 segundos
    private static final int MAX_FUEGOS_DEFAULT = 4; // Máximo 4 fuegos a la vez
    
    /**
     * Constructor del spawner
     */
    public FuegoSpawner(Handler handler, List<Point> spawnPoints) {
        this.handler = handler;
        this.spawnPoints = new ArrayList<>(spawnPoints);
        this.random = new Random();
        
        this.ticksDesdeUltimoSpawn = 0;
        this.ticksEntreSpawns = generarTiempoAleatorio();
        this.activo = false;
        this.maxFuegos = MAX_FUEGOS_DEFAULT;
        
        System.out.println("[FUEGO SPAWNER] Inicializado con " + spawnPoints.size() + " spawn points");
    }
    
    /**
     * Actualiza el spawner cada tick
     */
    public void tick() {
        if (!activo || spawnPoints.isEmpty()) {
            return;
        }
        
        ticksDesdeUltimoSpawn++;
        
        // Verificar si es momento de spawnar un fuego
        if (ticksDesdeUltimoSpawn >= ticksEntreSpawns) {
            // Solo spawner si no se ha alcanzado el límite
            if (contarFuegosActivos() < maxFuegos) {
                spawnFuego();
            }
            
            ticksDesdeUltimoSpawn = 0;
            ticksEntreSpawns = generarTiempoAleatorio();
        }
    }
    
    /**
     * Genera un fuego en un spawn point aleatorio
     */
    private void spawnFuego() {
        if (spawnPoints.isEmpty()) {
            return;
        }
        
        // Seleccionar spawn point aleatorio
        Point spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
        
        // Dirección aleatoria
        int direccion = random.nextBoolean() ? 1 : -1;
        
        // Tipo aleatorio (70% normal, 20% rápido, 10% perseguidor)
        Fuego.TipoFuego tipo;
        int tipoRandom = random.nextInt(100);
        if (tipoRandom < 70) {
            tipo = Fuego.TipoFuego.NORMAL;
        } else if (tipoRandom < 90) {
            tipo = Fuego.TipoFuego.RAPIDO;
        } else {
            tipo = Fuego.TipoFuego.PERSEGUIDOR;
        }
        
        // Crear y agregar el fuego
        Fuego fuego = new Fuego(
            spawnPoint.x, 
            spawnPoint.y, 
            2,  // scale
            handler,
            tipo,
            direccion
        );
        
        handler.addObj(fuego);
        
        System.out.println("[FUEGO SPAWNER] Fuego generado: tipo=" + tipo + 
                          " pos=(" + spawnPoint.x + "," + spawnPoint.y + ") " +
                          "dir=" + (direccion > 0 ? "DERECHA" : "IZQUIERDA"));
    }
    
    /**
     * Cuenta cuántos fuegos hay activos
     */
    private int contarFuegosActivos() {
        return handler.contarObjetosPorTipo(ObjetosID.Fuego);
    }
    
    /**
     * Genera un tiempo aleatorio entre spawns
     */
    private int generarTiempoAleatorio() {
        return TICKS_MIN_SPAWN + random.nextInt(TICKS_MAX_SPAWN - TICKS_MIN_SPAWN);
    }
    
    /**
     * Agrega un nuevo spawn point
     */
    public void agregarSpawnPoint(Point punto) {
        if (!spawnPoints.contains(punto)) {
            spawnPoints.add(punto);
            System.out.println("[FUEGO SPAWNER] Spawn point agregado: (" + punto.x + ", " + punto.y + ")");
        }
    }
    
    /**
     * Agrega múltiples spawn points
     */
    public void agregarSpawnPoints(List<Point> puntos) {
        for (Point punto : puntos) {
            agregarSpawnPoint(punto);
        }
    }
    
    /**
     * Elimina un spawn point
     */
    public void eliminarSpawnPoint(Point punto) {
        spawnPoints.remove(punto);
        System.out.println("[FUEGO SPAWNER] Spawn point eliminado");
    }
    
    /**
     * Limpia todos los spawn points
     */
    public void limpiarSpawnPoints() {
        spawnPoints.clear();
        System.out.println("[FUEGO SPAWNER] Todos los spawn points eliminados");
    }
    
    /**
     * Activa el spawner
     */
    public void activar() {
        activo = true;
        ticksDesdeUltimoSpawn = 0;
        System.out.println("[FUEGO SPAWNER] Activado");
    }
    
    /**
     * Desactiva el spawner
     */
    public void desactivar() {
        activo = false;
        System.out.println("[FUEGO SPAWNER] Desactivado");
    }
    
    /**
     * Elimina todos los fuegos activos
     */
    public void eliminarTodosFuegos() {
        handler.eliminarObjetosPorTipo(ObjetosID.Fuego);
        System.out.println("[FUEGO SPAWNER] Todos los fuegos eliminados");
    }
    
    /**
     * Fuerza el spawn inmediato de un fuego
     */
    public void spawnInmediato() {
        if (contarFuegosActivos() < maxFuegos) {
            spawnFuego();
        }
        ticksDesdeUltimoSpawn = 0;
        ticksEntreSpawns = generarTiempoAleatorio();
    }
    
    /**
     * Spawner un fuego específico en una posición
     */
    public void spawnFuegoEn(float x, float y, Fuego.TipoFuego tipo, int direccion) {
        Fuego fuego = new Fuego(x, y, 2, handler, tipo, direccion);
        handler.addObj(fuego);
        System.out.println("[FUEGO SPAWNER] Fuego manual spawneado en (" + x + "," + y + ")");
    }
    
    /**
     * Cambia el límite de fuegos simultáneos
     */
    public void setMaxFuegos(int max) {
        if (max > 0) {
            this.maxFuegos = max;
            System.out.println("[FUEGO SPAWNER] Límite de fuegos actualizado: " + max);
        }
    }
    
    /**
     * Cambia el rango de tiempo entre spawns
     */
    public void setRangoTiempoSpawn(int minTicks, int maxTicks) {
        if (minTicks > 0 && maxTicks > minTicks) {
            System.out.println("[FUEGO SPAWNER] Rango de spawn actualizado: " + 
                             minTicks + " - " + maxTicks + " ticks");
        }
    }
    
    // ==================== GETTERS ====================
    
    public boolean isActivo() {
        return activo;
    }
    
    public int getCantidadSpawnPoints() {
        return spawnPoints.size();
    }
    
    public List<Point> getSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }
    
    public int getTicksParaProximoSpawn() {
        return ticksEntreSpawns - ticksDesdeUltimoSpawn;
    }
    
    public int getMaxFuegos() {
        return maxFuegos;
    }
    
    public int getFuegosActivos() {
        return contarFuegosActivos();
    }
    
    /**
     * Información de debug
     */
    public String getInfo() {
        return String.format(
            "FuegoSpawner [Activo: %s, Spawn Points: %d, Fuegos: %d/%d, Próximo: %d ticks]",
            activo ? "SÍ" : "NO",
            spawnPoints.size(),
            contarFuegosActivos(),
            maxFuegos,
            getTicksParaProximoSpawn()
        );
    }
}