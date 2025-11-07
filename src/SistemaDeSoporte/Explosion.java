/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SistemaDeSoporte;

import Entidades.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

/**
 *
 * @author LENOVO
 */
class Explosion extends JuegoObjetos {
    
    private Handler handler;
    private int ticksVida;
    private static final int DURACION = 30; // 0.5 segundos
    private float radio;
    private float radioMax;
    private Color[] colores;
    private Random random;
    private Particula[] particulas;
    
    public Explosion(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.Explosion, 32, 32, scale);
        this.handler = handler;
        this.ticksVida = 0;
        this.radio = 5;
        this.radioMax = 20;
        this.random = new Random();
        
        // Colores de la explosión
        this.colores = new Color[] {
            new Color(255, 69, 0),   // Rojo-naranja
            new Color(255, 140, 0),  // Naranja
            new Color(255, 215, 0),  // Amarillo
            new Color(255, 255, 255) // Blanco
        };
        
        // Crear partículas
        crearParticulas();
    }
    
    private void crearParticulas() {
        int numParticulas = 12;
        particulas = new Particula[numParticulas];
        
        for (int i = 0; i < numParticulas; i++) {
            double angulo = Math.toRadians(i * (360.0 / numParticulas));
            float velX = (float)(Math.cos(angulo) * 3);
            float velY = (float)(Math.sin(angulo) * 3);
            
            particulas[i] = new Particula(
                getX(), 
                getY(), 
                velX, 
                velY,
                colores[random.nextInt(colores.length)]
            );
        }
    }
    
    @Override
    public void tick() {
        ticksVida++;
        
        // Expandir explosión
        radio += (radioMax - radio) * 0.2f;
        
        // Actualizar partículas
        for (Particula p : particulas) {
            p.tick();
        }
        
        // Eliminar cuando termine
        if (ticksVida >= DURACION) {
            handler.removeObj(this);
        }
    }
    
    @Override
    public void render(Graphics g) {
        float progreso = (float)ticksVida / DURACION;
        float alpha = 1.0f - progreso;
        
        // Círculo central de explosión
        for (int i = 0; i < colores.length; i++) {
            float r = radio * (1 - i * 0.2f);
            int alphaInt = (int)(alpha * 255);
            
            Color colorTransparente = new Color(
                colores[i].getRed(),
                colores[i].getGreen(),
                colores[i].getBlue(),
                alphaInt
            );
            
            g.setColor(colorTransparente);
            g.fillOval(
                (int)(getX() - r),
                (int)(getY() - r),
                (int)(r * 2),
                (int)(r * 2)
            );
        }
        
        // Renderizar partículas
        for (Particula p : particulas) {
            p.render(g, alpha);
        }
    }
    
    @Override
    public void aplicarGravedad() {
        // No aplicar gravedad
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, 0, 0, 0);
    }
    
    /**
     * Clase interna: Partícula de explosión
     */
    private class Particula {
        float x, y;
        float velX, velY;
        Color color;
        float vida;
        
        Particula(float x, float y, float velX, float velY, Color color) {
            this.x = x;
            this.y = y;
            this.velX = velX;
            this.velY = velY;
            this.color = color;
            this.vida = 1.0f;
        }
        
        void tick() {
            x += velX;
            y += velY;
            
            // Desacelerar
            velX *= 0.95f;
            velY *= 0.95f;
            
            // Reducir vida
            vida -= 0.033f;
        }
        
        void render(Graphics g, float alphaGlobal) {
            if (vida <= 0) return;
            
            int alpha = (int)(vida * alphaGlobal * 255);
            Color colorTransparente = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alpha
            );
         
            g.setColor(colorTransparente);
            int size = (int)(4 * vida);
            g.fillOval((int)x - size/2, (int)y - size/2, size, size);
        }
    }
}
