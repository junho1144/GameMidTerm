package io.jbnu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector2;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {


    private static final float MOVE_SPEED = 260;
    private static final float JUMP_SPEED = 650;
    private static final float GRAVITY = 1700;

    private SpriteBatch batch;
    private Texture playerTexture, blockTexture, flagTexture,backgroundTexture, energyBallTexture;
    private Texture enemy1Texture, enemy2Texture, enemy3Texture;
    private Texture obstacle1Texture, obstacle2Texture, obstacle3Texture;
    private Texture heartTexture;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private final Rectangle player = new Rectangle(80, 60, 32, 44);
    private final Rectangle flag = new Rectangle(2420, 344, 20, 100);
    private final Array<Rectangle> blocks = new Array<>();
    private float velocityX, velocityY;
    private boolean grounded;
    private boolean gameOver;


    private boolean facingLeft = true;

    private static final float BG_SPEED = 0.3f;


    private int playerHP = 3;

    private final Vector3 mouse = new Vector3(); // 마우스 클릭시
    private final Vector2 forceDir = new Vector2(); // 힘의 방향
    private float remainDash = 0f; // 남은 대쉬 횟수

    private float invincibleTime = 0f; // 남은 무적 시간



    private float shakeTimer = 0; // 남은시간
    private float shakeDuration = 0.2f; // 전체시간
    private float shakePower = 0;
    private void CameraShake(float power, float duration) {
        shakePower = power;
        shakeDuration = duration;
        shakeTimer = duration;
    }




    private int stage = 1;
    private static final int MAX_STAGE = 3;
    private float worldWidth = 2600;
    private final Vector2 spawn = new Vector2(80, 60);



    private final Array<Enemy> enemies = new Array<>();
    private final Array<Obstacle> obstacles = new Array<>();



    private static class Ghost {
        float x, y, life;
        boolean flip; // 좌우반전
    }
    private final Array<Ghost> ghosts = new Array<>();
    private float ghostTimer = 0f;
    private static final float GHOST_INTERVAL = 0.03f;   // 잔상 생성 간격(초)
    private static final float GHOST_LIFE = 0.25f;       // 잔상 유지 시간(초)
    private static final float GHOST_MIN_FORCE = 200f;   // 이 힘 이상일 때만 잔상 생성


    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal("KnightLeft.png"));
        blockTexture = new Texture(Gdx.files.internal("block.png"));
        flagTexture = new Texture(Gdx.files.internal("flag.png"));
        backgroundTexture = new Texture(Gdx.files.internal("Background1.jpg"));
        energyBallTexture = new Texture(Gdx.files.internal("DashEnergyBall.png"));
        enemy1Texture = new Texture(Gdx.files.internal("Enemy1.png"));
        enemy2Texture = new Texture(Gdx.files.internal("Enemy2.png"));
        enemy3Texture = new Texture(Gdx.files.internal("Enemy3.png"));
        heartTexture = new Texture(Gdx.files.internal("HealthSkull.png"));
        obstacle1Texture = new Texture(Gdx.files.internal("Obstacle1.png"));
        obstacle2Texture = new Texture(Gdx.files.internal("obstacle2.png"));
        obstacle3Texture = new Texture(Gdx.files.internal("obstacle3.png"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        viewport = new FitViewport(800, 600, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        loadStage(1);
    }

    private void loadStage(int n) {
        stage = n;

        // 초기화
        blocks.clear();
        enemies.clear();
        obstacles.clear();
        player.setPosition(spawn.x, spawn.y);
        velocityX = velocityY = 0;
        forceDir.x = 0f;
        forceDir.y = 0f;
        grounded = false;
        playerHP = 5;
        invincibleTime = 0;
        shakeTimer = 0;
        ghosts.clear();
        ghostTimer = 0f;

        switch (n) {
            case 1:
                worldWidth = 2600;
                blocks.add(new Rectangle(0, 0, 620, 60));
                blocks.add(new Rectangle(760, 0, 540, 60));
                blocks.add(new Rectangle(1440, 0, 1160, 60));
                blocks.add(new Rectangle(300, 60, 80, 64));
                blocks.add(new Rectangle(1080, 180, 160, 24));

                enemies.add(new Enemy(1, 300, 300, 80, 80, 2));

                obstacles.add(new Obstacle(1, 100f, 100f, 60f, 10f, 1f, 1f, 20f));
                obstacles.add(new Obstacle(2, 200f, 200f, 50f, 50f, 1f, 1f, 20f));
                obstacles.add(new Obstacle(3, 300f, 300f, 50f, 50f, 1f, -1f, 20f));
                break;

            case 2:
                worldWidth = 3000;
                blocks.add(new Rectangle(0, 0, 800, 60));
                blocks.add(new Rectangle(950, 0, 700, 60));
                blocks.add(new Rectangle(1800, 0, 1200, 60));
                blocks.add(new Rectangle(500, 60, 100, 100));
                blocks.add(new Rectangle(1200, 150, 200, 24));
                blocks.add(new Rectangle(2200, 60, 120, 120));

                enemies.add(new Enemy(2, 300, 300, 80, 80, 2));
                break;

            case 3:
                worldWidth = 3400;
                blocks.add(new Rectangle(0, 0, 500, 60));
                blocks.add(new Rectangle(650, 0, 400, 60));
                blocks.add(new Rectangle(1200, 0, 400, 60));
                blocks.add(new Rectangle(1750, 0, 1650, 60));
                blocks.add(new Rectangle(900, 120, 120, 24));
                blocks.add(new Rectangle(2300, 60, 150, 150));

                enemies.add(new Enemy(3, 300, 300, 80, 80, 2));
                break;
        }
    }


    @Override
    public void render() {

        // 큰 프레임 시간으로 인해 한 번에 너무 멀리 이동하는 것을 제한합니다.
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        update(dt);



        ScreenUtils.clear(1f,1f,1f,1f);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // BG
        float screenLeft = camera.position.x - 400;
        float bgX = screenLeft * (1 - BG_SPEED);
        float bgWidth = 800 + (worldWidth - 800) * BG_SPEED;
        batch.draw(backgroundTexture, bgX, 0, bgWidth, 600);

        for (Rectangle block : blocks) {
            batch.draw(blockTexture, block.x, block.y, block.width, block.height);
        }
        //batch.draw(flagTexture, flag.x, flag.y, flag.width, flag.height);



        // 잔상
        for (int i = 0; i < ghosts.size; i++) {
            Ghost g = ghosts.get(i);
            float alpha = (g.life / GHOST_LIFE) * 0.5f;
            batch.setColor(0.5f, 0.8f, 1f, alpha);
            batch.draw(playerTexture,
                g.x, g.y, player.width, player.height,
                0, 0, playerTexture.getWidth(), playerTexture.getHeight(),
                g.flip, false);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        // 플레이어
        if (invincibleTime <= 0 || ((int) (invincibleTime * 20) % 2 == 0)){
            batch.draw(playerTexture,
                player.x, player.y, player.width, player.height,
                0, 0, playerTexture.getWidth(), playerTexture.getHeight(),
                !facingLeft, false);
        }

        // hp표시
        int heartSize = 14;
        float heartGap = 1f;
        float totalWidth = playerHP * heartSize + (playerHP - 1) * heartGap;
        float startX = player.x + player.width / 2 - totalWidth / 2;
        float heartY = player.y + player.height + 1f;

        for (int i = 0; i < playerHP; i++) {
            batch.draw(heartTexture, startX + i * (heartSize + heartGap), heartY, heartSize, heartSize);
        }


        // 적들
        for (Enemy e : enemies) {

            if (e.hitTime > 0) {
                batch.setColor(1f, 0.2f, 0.2f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }

            switch (e.type){
                case 1:
                    batch.draw(enemy1Texture, e.col.x, e.col.y, e.col.width, e.col.height);
                    break;
                case 2:
                    batch.draw(enemy2Texture, e.col.x, e.col.y, e.col.width, e.col.height);
                    break;
                case 3:
                    batch.draw(enemy3Texture, e.col.x, e.col.y, e.col.width, e.col.height);
                    break;
            }

        }
        batch.setColor(1f, 1f, 1f, 1f);


        // 장애물
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle o = obstacles.get(i);

            switch (o.type){
                case 1:
                    batch.draw(obstacle1Texture,
                        o.bounds.x, o.bounds.y,
                        o.bounds.width / 2, o.bounds.height / 2,
                        o.bounds.width * 1.8f, o.bounds.height * 2,
                        1f, 1f,
                        o.getAngle(),
                        0, 0, obstacle1Texture.getWidth(), obstacle1Texture.getHeight(),
                        false, false);
                    break;
                case 2:
                    batch.draw(obstacle2Texture,
                        o.bounds.x, o.bounds.y,
                        o.bounds.width / 2, o.bounds.height / 2,
                        o.bounds.width * 1.3f, o.bounds.height * 1.3f,
                        1f, 1f,
                        o.getAngle(),
                        0, 0, obstacle2Texture.getWidth(), obstacle2Texture.getHeight(),
                        false, false);
                    break;
                case 3:
                    batch.draw(obstacle3Texture,
                        o.bounds.x, o.bounds.y,
                        o.bounds.width / 2, o.bounds.height / 2,
                        o.bounds.width * 1.3f, o.bounds.height * 1.3f,
                        1f, 1f,
                        o.getAngle(),
                        0, 0, obstacle3Texture.getWidth(), obstacle3Texture.getHeight(),
                        false, false);
                    break;

            }



        }


        // 마우스 주변 에너지볼
        if (remainDash >= 1){
            batch.draw(energyBallTexture, mouse.x - 10f, mouse.y + 8f, 20,20);
        }
        if (remainDash >= 2){
            batch.draw(energyBallTexture, mouse.x - 30f, mouse.y - 35f, 20,20);
        }
        if (remainDash >= 3){
            batch.draw(energyBallTexture, mouse.x + 10f, mouse.y - 35f, 20,20);
        }


        batch.end();

    }



    private void update(float dt) {
        if (gameOver || dt <= 0) return;

        invincibleTime -= dt;

        if (remainDash < stage)
            remainDash += dt * 1f;


        mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouse);

        // 마우스 클릭 - 그 방향으로 힘 대쉬함
        if (Gdx.input.justTouched() && remainDash > 0) {
            forceDir.set(mouse.x - player.x, mouse.y - player.y);

            forceDir.nor().scl(1000); // 일반화후 힘만큼
            velocityX = 0;
            velocityY = 0;
            grounded = false;
            facingLeft = (forceDir.x < 0f);
            remainDash--;

            CameraShake(2, 0.1f);
        }


        // 1. 좌우 이동과 지상 점프
        velocityX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= MOVE_SPEED;
            facingLeft = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) ||
            Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += MOVE_SPEED;
            facingLeft = false;
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.W)) &&
            grounded) {
            velocityY = JUMP_SPEED;
            grounded = false;
        }

        // 2. 중력: 아래쪽 속도는 음수이며 최대 낙하 속도를 제한합니다.
        velocityY = Math.max(velocityY - GRAVITY * dt, -900);

        // 힘 감소
        forceDir.x *= 0.95f;
        forceDir.y *= 0.95f;
        if (forceDir.len() < 10f) {
            forceDir.x = 0f;
            forceDir.y = 0f;
        }

        // 3. X축과 Y축을 나누어 이동하고 충돌을 보정합니다.
        moveX(dt);
        moveY(dt);

        // 잔상 생성: 대쉬
        if (forceDir.len() > GHOST_MIN_FORCE) {
            ghostTimer -= dt;
            if (ghostTimer <= 0f) {
                Ghost g = new Ghost();
                g.x = player.x;
                g.y = player.y;
                g.life = GHOST_LIFE;
                g.flip = !facingLeft;
                ghosts.add(g);
                ghostTimer = GHOST_INTERVAL;
            }
        } else {
            ghostTimer = 0f;
        }
        // 잔상 수명 감소
        for (int i = ghosts.size - 1; i >= 0; i--) {
            Ghost g = ghosts.get(i);
            g.life -= dt;
            if (g.life <= 0f) ghosts.removeIndex(i);
        }

        // 적 충돌 처리
        // FIXME: 적-플레이어 충돌 처리만 있음
        for (int i = 0; i < enemies.size; i++){
            Enemy e = enemies.get(i);

            e.hitTime -= dt;

            // 이동
            switch (e.type){
                case 1:
                    break;
                case 2:
                    e.col.x += 100f * dt * e.goRight;
                    if (e.col.x < 300f) {
                        e.goRight = 1;
                    } else if (e.col.x > worldWidth - 300f) {
                        e.goRight = -1;
                    }
                    break;
                case 3:
                    e.col.x += 120f * dt * e.goRight;
                    if (e.col.x < 300f) {
                        e.goRight = 1;
                    } else if (e.col.x > worldWidth - 300f) {
                        e.goRight = -1;
                    }
                    e.col.y += 80f * dt * e.goUp;
                    if (e.col.y < 100f) {
                        e.goUp = 1;
                    } else if (e.col.y > 500f) {
                        e.goUp = -1;
                    }
                    break;


            }

            // 플레이어-적 충돌
            if (player.overlaps(e.col)){
                Vector2 e2pVec = new Vector2(player.x - e.col.x, player.y - e.col.y).nor();
                // 돌진 중이라면
                if (forceDir.len() > 0.1f && e.hitTime <= 0f){
                    EnemyDealt(e);
                    // 튕김
                    forceDir.x = e2pVec.x * 900;
                    forceDir.y = e2pVec.y * 900;

                    CameraShake(10, 0.15f);
                }
                else if (invincibleTime <= 0f) {
                    PlayerDealt();
                    // 살짝 튕김
                    forceDir.x = e2pVec.x * 600;
                    forceDir.y = e2pVec.y * 600;

                }
            }
        }

        // 장애물
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle o = obstacles.get(i);
            o.update(dt);
            if (o.isOutOfWorld(worldWidth)) {
                obstacles.removeIndex(i);
                continue;
            }

            // 피해
            if (invincibleTime > 0 || !player.overlaps(o.bounds) || forceDir.len() > 0.1f) continue;
            PlayerDealt();
            CameraShake(12f, 0.3f);
        }

        // 4. 낭떠러지에 떨어지면 시작점으로 복귀합니다.
        if (player.y < -100) {
            PlayerDealt();
            player.setPosition(80, 60);
            velocityY = 0;
            grounded = false;
        }

        // 5. 깃발에 닿으면 게임 로직을 멈추고 마지막 화면을 유지합니다.
        //if (player.overlaps(flag)) gameOver = true;

        // 6. 카메라는 X축으로만 따라갑니다.
        float camX = MathUtils.clamp(player.x + player.width / 2, 400, worldWidth - 400);
        float camY = 300;

        if (shakeTimer > 0f) {
            shakeTimer -= dt;
            float power = shakePower * (shakeTimer / shakeDuration);
            camX += MathUtils.random(-power, power);
            camY += MathUtils.random(-power, power);
        }

        camera.position.set(camX, camY, 0);
        camera.update();

        if (allEnemiesDead()) {
            loadStage(stage % MAX_STAGE + 1);
            return;
        }
    }
    private boolean allEnemiesDead() {
        for (int i = 0; i < enemies.size; i++) {
            if (enemies.get(i).hp > 0) return false;
        }
        return true;
    }

    private void moveX(float dt) {
        player.x += (velocityX + forceDir.x) * dt;
        for (Rectangle block : blocks) {
            if (!player.overlaps(block)) continue;
            if (velocityX > 0) player.x = block.x - player.width;
            else if (velocityX < 0) player.x = block.x + block.width;
        }
        player.x = MathUtils.clamp(player.x, 0, worldWidth - player.width);
    }

    private void moveY(float dt) {
        float dy = (velocityY + forceDir.y) * dt;
        player.y += dy;
        grounded = false;

        for (Rectangle block : blocks) {
            if (!player.overlaps(block)) continue;
            if (dy < 0) { // 내려오던 중: 블록 위에 착지
                player.y = block.y + block.height;
                velocityY = 0;
                Land();
            } else if (dy > 0) { // 올라가던 중: 블록 아래에 머리 충돌
                player.y = block.y - player.height;
                velocityY = 0;
            }
        }
    }
    private void Land(){
        grounded = true;
        // 현재 스테이지에 따라 최대대쉬 수 다름
        //remainDash = stage;
        forceDir.x = 0f;
        forceDir.y = 0f;
    }

    private void PlayerDealt(){
        playerHP--;
        if (playerHP <= 0){
            loadStage(stage);
            return;
        }

        invincibleTime = 1.0f;
        CameraShake(10, 0.15f);
    }
    private void EnemyDealt(Enemy e){
        e.hp--;
        e.hitTime = 0.3f;
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        blockTexture.dispose();
        flagTexture.dispose();
    }
}
