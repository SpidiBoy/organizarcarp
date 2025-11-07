package SistemaSoporte.Spawners;

import Entidades.Items.Martillo;
import Entidades.Items.BolsoDama;
import Entidades.Items.Item;
import Entidades.Items.Sombrero;
import Entidades.Items.Paraguas;
import SistemaDeSoporte.Handler;
import java.awt.Point;
import java.util.*;

public class ItemSpawner {
    private Handler handler;
    private List<Point> spawnPoints;
    private Random random;
    
    private int ticksDesdeUltimoSpawn;
    private int ticksEntreSpawns;
    private boolean activo;
    
    private static final int TICKS_MIN = 300;  // 5 segundos
    private static final int TICKS_MAX = 600;  // 10 segundos
    
    // Probabilidades de spawn (0-100)
    private static final int PROB_MARTILLO = 10;
    private static final int PROB_PARAGUAS = 30;
    private static final int PROB_BOLSO = 25;
    private static final int PROB_SOMBRERO = 35;
    
    public ItemSpawner(Handler handler, List<Point> spawnPoints) {
        this.handler = handler;
        this.spawnPoints = new ArrayList<>(spawnPoints);
        this.random = new Random();
        this.ticksDesdeUltimoSpawn = 0;
        this.ticksEntreSpawns = TICKS_MIN;
        this.activo = false;
    }
    
    public void tick() {
        if (!activo || spawnPoints.isEmpty()) return;
        
        ticksDesdeUltimoSpawn++;
        
        if (ticksDesdeUltimoSpawn >= ticksEntreSpawns) {
            spawnItemAleatorio();
            ticksDesdeUltimoSpawn = 0;
            ticksEntreSpawns = TICKS_MIN + random.nextInt(TICKS_MAX - TICKS_MIN);
        }
    }
    
private void spawnItemAleatorio() {
    Point spawn = spawnPoints.get(random.nextInt(spawnPoints.size()));
    int prob = random.nextInt(100);
    
    Item item = null;
    
    if (prob < PROB_MARTILLO) {
        // 🔧 FIX: Descomentado
        item = new Martillo(spawn.x, spawn.y, 2, handler);
    } else if (prob < PROB_MARTILLO + PROB_PARAGUAS) {
        // 🔧 FIX: Descomentado
        item = new Paraguas(spawn.x, spawn.y, 2, handler);
    } else if (prob < PROB_MARTILLO + PROB_PARAGUAS + PROB_BOLSO) {
        item = new BolsoDama(spawn.x, spawn.y, 2, handler);
    } else {
        item = new Sombrero(spawn.x, spawn.y, 2, handler);
    }
    
    if (item != null) {
        handler.addObj(item);
        System.out.println("[ITEM SPAWNER] " + item.getClass().getSimpleName() + 
                         " spawneado en (" + spawn.x + "," + spawn.y + ")");
    }
}
    
    public void activar() {
        activo = true;
        System.out.println("[ITEM SPAWNER] Activado");
    }
    
    public void desactivar() {
        activo = false;
        System.out.println("[ITEM SPAWNER] Desactivado");
    }
    
    public void agregarSpawnPoint(Point punto) {
        spawnPoints.add(punto);
    }
}