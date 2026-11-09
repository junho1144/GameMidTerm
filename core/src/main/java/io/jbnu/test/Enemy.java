package io.jbnu.test;


import com.badlogic.gdx.math.Rectangle;


public class Enemy {
    public int hp;

    public final Rectangle col;

    public int type;

    public Enemy(int type, float x, float y, float w, float h, int hp) {
        this.type = type;
        this.col = new Rectangle(x, y, w, h);
        this.hp = hp;
    }


    public float hitTime;

    public int goRight = 1;
    public int goUp = 1;


    public float shootTimer = 0f;



}
