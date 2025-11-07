/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SistemaDeSoporte;

/**
 *
 * @author LENOVO
 */
public enum ObjetosID {
    Jugador,
    // Elementos del escenario
    Bloque, // Plataformas y bloques sólidos
    TileVisual,   //Tiles decorativos (SIN colisión)
    Pipe,             // Tuberías (heredado de Mario, puede usarse para otros elementos)
    Escalera,// Escaleras normales
    Item,
    EscaleraRota,     // Escaleras rotas o dañadas
    
    // Enemigos y NPCs
    DiegoKong,       // Diego Kong
    Barril,           // Barriles que lanza DK
    Fuego,            // Fuego/llama enemiga
    Princesa,         // Princesa a rescatar
    
    // Elementos interactivos
    Martillo,         // Martillo power-up
    Puntos,           // Objetos que dan puntos
    
    // Elementos del sistema
    SpawnPoint,       // Puntos de aparición
    Trigger,          // Disparadores de eventos
    
    // Efectos visuales
    Explosion,        // Explosiones
    Particula  
}
