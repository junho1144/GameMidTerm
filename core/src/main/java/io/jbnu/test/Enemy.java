package io.jbnu.test;


import com.badlogic.gdx.math.Rectangle;


public class Enemy {
    public int hp;

    public final Rectangle col;

    public Enemy(float x, float y, float w, float h, int hp) {
        this.col = new Rectangle(x, y, w, h);
        this.hp = hp;
    }
}
