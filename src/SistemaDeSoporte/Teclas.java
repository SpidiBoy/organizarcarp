package SistemaDeSoporte;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import mariotest.GestorEstados;

/**
 * Sistema de control de teclas mejorado
 * Soporta movimiento en escaleras con animaciones completas
 * 
 * @author LENOVO
 */
public class Teclas extends KeyAdapter {
    private boolean[] keyAbajo = new boolean[5];
    private Handler handler;
    private GestorEstados gestorEstados;
    // indices de las teclas
    private static final int KEY_SPACE = 0;
    private static final int KEY_W = 1;
    private static final int KEY_S = 2;
    private static final int KEY_A = 3;
    private static final int KEY_D = 4;
    
    public Teclas(Handler handler, GestorEstados gestorEstados) {
        this.handler = handler;
        this.gestorEstados = gestorEstados;
    }
    
@Override
    public void keyPressed(KeyEvent e) {
        
        // DELEGAR AL GESTOR DE ESTADOS PRIMERO
        if (gestorEstados != null) {
            gestorEstados.keyPressed(e);
        }
        
        // Si no estamos jugando (estamos en un menú), no procesar teclas de jugador
        if (gestorEstados != null && !gestorEstados.estaJugando()) {
            return;
        }
        
        int key = e.getKeyCode();
        
        // Verificar que el jugador exista
        if (handler.getPlayer() == null) {
            return;
        }
        
        // ESPACIO - Saltar
        if (key == KeyEvent.VK_SPACE) {
            if (!keyAbajo[KEY_SPACE]) {
                handler.getPlayer().iniciarSalto();
                keyAbajo[KEY_SPACE] = true;
            }
        }
        
        // W - Subir escalera
        if (key == KeyEvent.VK_W) {
            if (!keyAbajo[KEY_W]) {
                if (handler.getPlayer().isPuedeMoverseEnEscalera()) {
                    handler.getPlayer().subirEscalera();
                }
                keyAbajo[KEY_W] = true;
            }
        }
        
        // S - Bajar escalera
        if (key == KeyEvent.VK_S) {
            if (!keyAbajo[KEY_S]) {
                if (handler.getPlayer().isPuedeMoverseEnEscalera()) {
                    handler.getPlayer().bajarEscalera();
                }
                keyAbajo[KEY_S] = true;
            }
        }
        
        // A - Mover a la izquierda
        if (key == KeyEvent.VK_A) {
            if (!keyAbajo[KEY_A]) {
                handler.getPlayer().moverIzquierda();
                keyAbajo[KEY_A] = true;
            }
        }
        
        // D - Mover a la derecha
        if (key == KeyEvent.VK_D) {
            if (!keyAbajo[KEY_D]) {
                handler.getPlayer().moverDerecha();
                keyAbajo[KEY_D] = true;
            }
        }
    }
    
@Override
    public void keyReleased(KeyEvent e) {
        
        // 4. DELEGAR AL GESTOR DE ESTADOS
        if (gestorEstados != null) {
            gestorEstados.keyReleased(e);
        }

        // Si no estamos jugando, no procesar
        if (gestorEstados != null && !gestorEstados.estaJugando()) {
            return;
        }

        int key = e.getKeyCode();
        
        if (handler.getPlayer() == null) {
            return;
        }
        
        if (key == KeyEvent.VK_SPACE) {
            keyAbajo[KEY_SPACE] = false;
        }
        
        if (key == KeyEvent.VK_W) {
            keyAbajo[KEY_W] = false;
            if (handler.getPlayer().isEnEscalera() && !keyAbajo[KEY_S]) {
                handler.getPlayer().detenerMovimientoVertical();
            }
        }
        
        if (key == KeyEvent.VK_S) {
            keyAbajo[KEY_S] = false;
            if (handler.getPlayer().isEnEscalera() && !keyAbajo[KEY_W]) {
                handler.getPlayer().detenerMovimientoVertical();
            }
        }
        
        if (key == KeyEvent.VK_A) {
            keyAbajo[KEY_A] = false;
        }
        
        if (key == KeyEvent.VK_D) {
            keyAbajo[KEY_D] = false;
        }
        
        if (!keyAbajo[KEY_A] && !keyAbajo[KEY_D]) {
            handler.getPlayer().detenerMovimiento();
        }
    }
    
    /**
     * Verifica si una tecla específica está presionada
     */
    public boolean isKeyDown(int keyIndex) {
        if (keyIndex >= 0 && keyIndex < keyAbajo.length) {
            return keyAbajo[keyIndex];
        }
        return false;
    }
    
    /**
     * Verifica si W está presionada (útil para debug)
     */
    public boolean isWPressed() {
        return keyAbajo[KEY_W];
    }
    
    /**
     * Verifica si S está presionada (útil para debug)
     */
    public boolean isSPressed() {
        return keyAbajo[KEY_S];
    }
    
    /**
     * Verifica si A está presionada
     */
    public boolean isAPressed() {
        return keyAbajo[KEY_A];
    }
    
    /**
     * Verifica si D está presionada
     */
    public boolean isDPressed() {
        return keyAbajo[KEY_D];
    }
    
    /**
     * Resetea todas las teclas (útil para pausas o cambios de estado)
     */
    public void resetKeys() {
        for (int i = 0; i < keyAbajo.length; i++) {
            keyAbajo[i] = false;
        }
        
        if (handler.getPlayer() != null) {
            handler.getPlayer().detenerMovimiento();
            handler.getPlayer().detenerMovimientoVertical();
        }
    }
}