package io.jbnu.test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class Obstacle {
    public final Rectangle bounds;

    private final Vector2 direction = new Vector2();   // 이동 방향 (길이 1)
    private float speed;

    public int type;

    public boolean homing = false;
    public float turnRate = 75f;




    public Obstacle(int type, float x, float y, float w, float h,
                    float dirX, float dirY, float speed) {
        this.type = type;
        this.bounds = new Rectangle(x, y, w, h);
        this.speed = speed;
        this.direction.set(dirX, dirY);
        if (!direction.isZero()) direction.nor();
    }

    public void update(float dt) {

        life -= dt;
        if (spiral) {
            spiralAngle -= angularSpeed * dt;     // 시계방향 = 각도 감소
            spiralRadius += radialSpeed * dt;     // 점점 퍼짐

            float px = originX + MathUtils.cosDeg(spiralAngle) * spiralRadius;
            float py = originY + MathUtils.sinDeg(spiralAngle) * spiralRadius;

            // 진행 방향(이미지 회전용): 이전 위치 → 새 위치
            float cx = bounds.x + bounds.width / 2;
            float cy = bounds.y + bounds.height / 2;
            if (px != cx || py != cy) direction.set(px - cx, py - cy).nor();

            bounds.setCenter(px, py);
            return;
        }

        bounds.x += direction.x * speed * dt;
        bounds.y += direction.y * speed * dt;

        if (type == 1){
            speed += 150f * dt;
        }
        if (type == 2){
            speed += 100f * dt;
        }
        if (type == 3){
            speed += 100f * dt;
        }
    }
    public boolean isExpired() {
        return life <= 0f;
    }

    public float getAngle() {
        return direction.angleDeg();
    }

    public boolean isOutOfWorld(float worldWidth) {
        return bounds.x + bounds.width < -200 || bounds.x > worldWidth + 200
            || bounds.y + bounds.height < -200 || bounds.y > 1000;
    }


    public void steerToward(float targetX, float targetY, float dt) {
        float cx = bounds.x + bounds.width / 2;
        float cy = bounds.y + bounds.height / 2;

        float targetAngle = MathUtils.atan2(targetY - cy, targetX - cx) * MathUtils.radiansToDegrees;
        float currentAngle = direction.angleDeg();

        // 현재 → 목표 각도 차이를 -180 ~ 180 범위로 변환 (가까운 쪽으로 회전)
        float diff = ((targetAngle - currentAngle + 540f) % 360f) - 180f;

        float maxTurn = turnRate * dt;
        float turn = MathUtils.clamp(diff, -maxTurn, maxTurn);

        direction.rotateDeg(turn);
    }



    public boolean spiral = false;
    private float originX, originY;     // 출발점 (중심)
    private float spiralAngle;          // 현재 각도(도)
    private float spiralRadius = 0f;    // 현재 반지름
    public float angularSpeed = 120f;   // 초당 회전각(도), 시계방향이라 각도가 감소
    public float radialSpeed = 100f;     // 초당 반지름 증가량
    /** (cx, cy)를 출발점으로, startAngle 방향에서 시작하는 시계방향 나선으로 설정 */
    public void setSpiral(float cx, float cy, float startAngle) {
        spiral = true;
        originX = cx;
        originY = cy;
        spiralAngle = startAngle;
        spiralRadius = 0f;
    }


    private static final float LIFE_TIME = 5f;   // 모든 장애물의 수명(초)
    private float life = LIFE_TIME;
}
