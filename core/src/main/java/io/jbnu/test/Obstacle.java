package io.jbnu.test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Obstacle {
    public final Rectangle bounds;

    private final Vector2 direction = new Vector2();   // 이동 방향 (길이 1)
    private final float speed;

    public int type;




    public Obstacle(int type, float x, float y, float w, float h,
                    float dirX, float dirY, float speed) {
        this.type = type;
        this.bounds = new Rectangle(x, y, w, h);
        this.speed = speed;
        this.direction.set(dirX, dirY);
        if (!direction.isZero()) direction.nor();
    }

    public void update(float dt) {
        bounds.x += direction.x * speed * dt;
        bounds.y += direction.y * speed * dt;
    }


    public float getAngle() {
        return direction.angleDeg();
    }

    public boolean isOutOfWorld(float worldWidth) {
        return bounds.x + bounds.width < -200 || bounds.x > worldWidth + 200
            || bounds.y + bounds.height < -200 || bounds.y > 1000;
    }
}
