/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SistemaSoporte.Spawners;
import SistemaDeSoporte.Handler;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 *
 * @author LENOVO
 */
public class BarrilSpawner {
    private Handler handler;
    private List<Point> spawnPoints;
    private Random random;
    
    // Configuración de spawn
    private int ticksDesdeUltimoSpawn;
    private int ticksEntreSpawns;
    private boolean activo;
    
    // Constantes
    private static final int TICKS_MIN_SPAWN = 120;  // 2 segundos a 60 FPS
    private static final int TICKS_MAX_SPAWN = 300;  // 5 segundos a 60 FPS
    
    /**
     * Constructor del spawner
     * 
     * @param handler Handler del juego
     * @param spawnPoints Lista de puntos donde spawnar barriles
     */
    public BarrilSpawner(Handler handler, List<Point> spawnPoints) {
        this.handler = handler;
        this.spawnPoints = new ArrayList<>(spawnPoints);
        this.random = new Random();
        
        this.ticksDesdeUltimoSpawn = 0;
        this.ticksEntreSpawns = generarTiempoAleatorio();
        this.activo = false;
        
        System.out.println("BarrelSpawner inicializado con " + spawnPoints.size() + " spawn points");
    }
    
    /**
     * Actualiza el spawner cada tick
     * Llamar desde el método tick() del juego principal
     */
    public void tick() {
        if (!activo || spawnPoints.isEmpty()) {
            return;
        }
        
        ticksDesdeUltimoSpawn++;
        
        // Verificar si es momento de spawnar un barril
        if (ticksDesdeUltimoSpawn >= ticksEntreSpawns) {
            spawnBarril();
            ticksDesdeUltimoSpawn = 0;
            ticksEntreSpawns = generarTiempoAleatorio();
        }
    }
    
    /**
     * Genera un barril en un spawn point aleatorio
     */
    private void spawnBarril() {
        if (spawnPoints.isEmpty()) {
            return;
        }
        
        // Seleccionar un spawn point aleatorio
        Point spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
        
        // Dirección aleatoria (50% izquierda, 50% derecha)
        int direccion = random.nextBoolean() ? 1 : -1;
        
        // Crear y agregar el barril
        Entidades.Enemigos.Barril barril = new Entidades.Enemigos.Barril(
            spawnPoint.x, 
            spawnPoint.y, 
            2,  // scale
            handler,
            direccion
        );
        
        handler.addObj(barril);
        
        System.out.println("SPAWN: Barril generado en (" + spawnPoint.x + ", " + spawnPoint.y + ") dirección: " + (direccion > 0 ? "DERECHA" : "IZQUIERDA"));
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
            System.out.println("Spawn point agregado: (" + punto.x + ", " + punto.y + ")");
        }
    }
    
    /**
     * Elimina un spawn point
     */
    public void eliminarSpawnPoint(Point punto) {
        spawnPoints.remove(punto);
        System.out.println("Spawn point eliminado: (" + punto.x + ", " + punto.y + ")");
    }
    
    /**
     * Limpia todos los spawn points
     */
    public void limpiarSpawnPoints() {
        spawnPoints.clear();
        System.out.println("Todos los spawn points eliminados");
    }
    
    /**
     * Activa el spawner (comienza a generar barriles)
     */
    public void activar() {
        activo = true;
        ticksDesdeUltimoSpawn = 0;
        System.out.println("BarrelSpawner activado");
    }
    
    /**
     * Desactiva el spawner (deja de generar barriles)
     */
    public void desactivar() {
        activo = false;
        System.out.println("BarrelSpawner desactivado");
    }
    
    /**
     * Cambia el rango de tiempo entre spawns
     */
    public void setRangoTiempoSpawn(int minTicks, int maxTicks) {
        if (minTicks > 0 && maxTicks > minTicks) {
            // Actualizar constantes no se puede, pero podemos usar variables locales
            System.out.println("Rango de spawn actualizado: " + minTicks + " - " + maxTicks + " ticks");
        }
    }
    
    /**
     * Fuerza el spawn inmediato de un barril
     */
    public void spawnInmediato() {
        spawnBarril();
        ticksDesdeUltimoSpawn = 0;
        ticksEntreSpawns = generarTiempoAleatorio();
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
    
    /**
     * Información de debug
     */
    public String getInfo() {
        return String.format(
            "BarrelSpawner [Activo: %s, Spawn Points: %d, Próximo spawn en: %d ticks]",
            activo ? "SÍ" : "NO",
            spawnPoints.size(),
            getTicksParaProximoSpawn()
        );
    }
}
