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
import com.badlogic.gdx.audio.Music;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {


    private static final float MOVE_SPEED = 260;
    private static final float JUMP_SPEED = 650;
    private static final float GRAVITY = 1700;

    private SpriteBatch batch;
    private Texture playerTexture, blockTexture, flagTexture, energyBallTexture;
    private Texture enemy1Texture, enemy2Texture, enemy3Texture;
    private Texture obstacle1Texture, obstacle2Texture, obstacle3Texture;
    private Texture background1Texture, background2Texture, background3Texture;
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




    private Music bgm;


    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal("KnightLeft.png"));
        blockTexture = new Texture(Gdx.files.internal("BlockTexture.png"));
        flagTexture = new Texture(Gdx.files.internal("flag.png"));
        background1Texture = new Texture(Gdx.files.internal("Background1.png"));
        background2Texture = new Texture(Gdx.files.internal("Background2.png"));
        background3Texture = new Texture(Gdx.files.internal("Background3.png"));
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

        bgm = Gdx.audio.newMusic(Gdx.files.internal("NightmareKing.mp3"));
        bgm.setLooping(true);
        bgm.setVolume(0.5f);
        bgm.play();

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
        remainDash = 0f;

        switch (n) {
            case 1:
                worldWidth = 1000;
                blocks.add(new Rectangle(-30, 0, 620, 60));
                blocks.add(new Rectangle(760, 0, 800, 60));
                //blocks.add(new Rectangle(1440, 0, 1160, 60));
                //blocks.add(new Rectangle(300, 60, 80, 64));
                //blocks.add(new Rectangle(1080, 180, 160, 24));

                enemies.add(new Enemy(1, 300, 200, 90, 140, 4));

                //obstacles.add(new Obstacle(1, 100f, 100f, 60f, 10f, 1f, 1f, 20f));
                //obstacles.add(new Obstacle(2, 200f, 200f, 50f, 50f, 1f, 1f, 20f));
                //obstacles.add(new Obstacle(3, 300f, 300f, 50f, 50f, 1f, -1f, 20f));
                break;

            case 2:
                worldWidth = 1200;
                blocks.add(new Rectangle(0, 0, 500, 60));
                blocks.add(new Rectangle(1000, 0, 200, 60));
                //blocks.add(new Rectangle(1800, 0, 1200, 60));
                blocks.add(new Rectangle(700, 30, 250, 100));
                //blocks.add(new Rectangle(1300, 30, 100, 100));
                //blocks.add(new Rectangle(2200, 60, 120, 120));

                enemies.add(new Enemy(2, 300, 310, 150, 150, 5));
                break;

            case 3:
                worldWidth = 1500;
                blocks.add(new Rectangle(0, 0, 500, 60));
                blocks.add(new Rectangle(650, 0, 300, 120));
                blocks.add(new Rectangle(1200, 0, 400, 60));
                //blocks.add(new Rectangle(1750, 0, 1650, 60));
                //blocks.add(new Rectangle(900, 120, 120, 60));
                //blocks.add(new Rectangle(2300, 60, 150, 150));

                enemies.add(new Enemy(3, 300, 300, 140, 140, 7));
                break;
        }
    }


    @Override
    public void render() {

        // 큰 프레임 시간으로 인해 한 번에 너무 멀리 이동하는 것을 제한합니다.
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) paused = !paused;
        if (!paused) update(dt);



        ScreenUtils.clear(1f,1f,1f,1f);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // BG
        // BG
        Texture bg;
        switch (stage) {
            case 2:  bg = background2Texture; break;
            case 3:  bg = background3Texture; break;
            default: bg = background1Texture; break;
        }

        float screenLeft = camera.position.x - 400;
        float bgX = screenLeft * (1 - BG_SPEED);
        float bgWidth = 800 + (worldWidth - 800) * BG_SPEED;
        batch.draw(bg, bgX, 0, bgWidth, 600);

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

            // 타입별 텍스처와 배율 (가로, 세로)
            Texture tex;
            float scaleX, scaleY;
            switch (e.type) {
                case 1:  tex = enemy1Texture; scaleX = 2.0f; scaleY = 1.2f; break;
                case 2:  tex = enemy2Texture; scaleX = 1.5f; scaleY = 1.5f; break;
                default: tex = enemy3Texture; scaleX = 1.7f; scaleY = 1.7f; break;
            }

            // 그림 크기를 정하고, 판정 박스(col) 중심에 맞춰 배치
            float w = e.col.width * scaleX;
            float h = e.col.height * scaleY;
            float dx = e.col.x + e.col.width / 2 - w / 2;
            float dy = e.col.y + e.col.height / 2 - h / 2;

            batch.draw(tex, dx, dy, w, h);
        }
        batch.setColor(1f, 1f, 1f, 1f);


        // 장애물
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle o = obstacles.get(i);

            // 타입별 텍스처와 배율 (가로, 세로)
            Texture tex;
            float scaleX, scaleY;
            switch (o.type) {
                case 1:  tex = obstacle1Texture; scaleX = 1.8f; scaleY = 2.0f; break;
                case 2:  tex = obstacle2Texture; scaleX = 1.4f; scaleY = 1.4f; break;
                default: tex = obstacle3Texture; scaleX = 1.4f; scaleY = 1.4f; break;
            }

            // 그림 크기를 정하고, 판정 박스(bounds) 중심에 맞춰 배치
            float w = o.bounds.width * scaleX;
            float h = o.bounds.height * scaleY;
            float dx = o.bounds.x + o.bounds.width / 2 - w / 2;
            float dy = o.bounds.y + o.bounds.height / 2 - h / 2;

            batch.draw(tex,
                dx, dy,
                w / 2, h / 2,          // 회전 중심 = 그림 중심 (= bounds 중심)
                w, h,
                1f, 1f,
                o.getAngle(),
                0, 0, tex.getWidth(), tex.getHeight(),
                false, false);
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



    private boolean paused = false;
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


            // type 1: 5초마다 8방향으로 장애물 발사
            if ((e.type == 1 || e.type == 2 || e.type == 3) && e.hp > 0) {
                e.shootTimer -= dt;
                if (e.shootTimer <= 0f) {
                    e.shootTimer = 5f;
                    fireEightWay(e);
                }
            }
            // type 2: 4초마다 유도 장애물 1개 발사
            if ((e.type == 2 || e.type == 3) && e.hp > 0) {
                e.shootTimer2 -= dt;
                if (e.shootTimer2 <= 0f) {
                    e.shootTimer2 = 4f;
                    fireHoming(e);
                }
            }
            // type 3: 6초마다 3방향 나선 장애물 발사
            if (e.type == 3 && e.hp > 0) {
                e.shootTimer3 -= dt;
                if (e.shootTimer3 <= 0f) {
                    e.shootTimer3 = 6f;
                    fireSpiral(e);
                }
            }

            // 이동
            switch (e.type){
                case 1:
                    e.col.x += 80f * dt * e.goRight;
                    if (e.col.x < 300f) {
                        e.goRight = 1;
                    } else if (e.col.x > worldWidth - 300f) {
                        e.goRight = -1;
                    }
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
                    e.col.x += 140f * dt * e.goRight;
                    if (e.col.x < 300f) {
                        e.goRight = 1;
                    } else if (e.col.x > worldWidth - 300f) {
                        e.goRight = -1;
                    }
                    e.col.y += 60f * dt * e.goUp;
                    if (e.col.y < 150f) {
                        e.goUp = 1;
                    } else if (e.col.y > 400f) {
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
                    forceDir.y = -1 * 900;

                    CameraShake(10, 0.15f);
                }
                else if (invincibleTime <= 0f) {
                    if (PlayerDealt()) return;
                    // 살짝 튕김
                    forceDir.x = e2pVec.x * 600;
                    forceDir.y = e2pVec.y * 600;

                }
            }
        }

        // 장애물
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle o = obstacles.get(i);
            if (o.homing) {
                o.steerToward(player.x + player.width / 2, player.y + player.height / 2, dt);
            }
            o.update(dt);
            if (o.isOutOfWorld(worldWidth) || o.isExpired()) {
                obstacles.removeIndex(i);
                continue;
            }

            // 피해
            if (invincibleTime > 0 || !player.overlaps(o.bounds) || forceDir.len() > 0.1f) continue;
            if (PlayerDealt()) return;
            CameraShake(12f, 0.3f);
        }

        // 4. 낭떠러지에 떨어지면 시작점으로 복귀합니다.
        if (player.y < -100) {
            if (PlayerDealt()) return;
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
    private void fireEightWay(Enemy e) {
        float cx = e.col.x + e.col.width / 2;
        float cy = e.col.y + e.col.height / 2;
        float size = 30f;   // 장애물 크기

        for (int i = 0; i < 8; i++) {
            float angle = i * 45f;
            float dirX = MathUtils.cosDeg(angle);
            float dirY = MathUtils.sinDeg(angle);

            // 적 중심에서 생성되도록 크기의 절반만큼 보정
            obstacles.add(new Obstacle(1,
                cx - size / 2, cy - size / 2, size * 1.5f, size * 0.15f,
                dirX, dirY, 10f));
        }
    }
    private void fireHoming(Enemy e) {
        float cx = e.col.x + e.col.width / 2;
        float cy = e.col.y + e.col.height / 2;
        float size = 30f;

        // 처음 방향은 발사 시점의 플레이어 쪽
        float dirX = player.x - cx;
        float dirY = player.y - cy;

        Obstacle o = new Obstacle(2, cx - size / 2, cy - size / 2, size, size,
            dirX, dirY, 160f);
        o.homing = true;
        o.turnRate = 45f;
        obstacles.add(o);
    }
    private void fireSpiral(Enemy e) {
        float cx = e.col.x + e.col.width / 2;
        float cy = e.col.y + e.col.height / 2;
        float size = 30f;

        for (int i = 0; i < 3; i++) {
            float angle = 90f + i * 120f;   // 위, 왼쪽 아래, 오른쪽 아래 (120도 간격)

            Obstacle o = new Obstacle(3, cx - size / 2, cy - size / 2, size, size,
                1f, 0f, 0f);            // 방향/속도는 나선이 덮어씀
            o.setSpiral(cx, cy, angle);
            obstacles.add(o);
        }
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

    private boolean PlayerDealt() {
        playerHP--;
        if (playerHP <= 0) {
            loadStage(stage);
            return true;   // 사망 → 스테이지 재시작됨
        }
        invincibleTime = 1.0f;
        CameraShake(10, 0.15f);
        return false;
    }
    private void EnemyDealt(Enemy e){
        e.hp--;
        e.hitTime = 0.5f;
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
        background1Texture.dispose();
        background2Texture.dispose();
        background3Texture.dispose();
    }
}
