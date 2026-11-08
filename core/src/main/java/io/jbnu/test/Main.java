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

    private static final float WORLD_WIDTH = 2600;
    private static final float MOVE_SPEED = 260;
    private static final float JUMP_SPEED = 650;
    private static final float GRAVITY = 1700;

    private SpriteBatch batch;
    private Texture playerTexture, blockTexture, flagTexture;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private final Rectangle player = new Rectangle(80, 60, 32, 44);
    private final Rectangle flag = new Rectangle(2420, 344, 20, 100);
    private final Array<Rectangle> blocks = new Array<>();
    private float velocityX, velocityY;
    private boolean grounded;
    private boolean gameOver;


    private boolean facingLeft = true;


    private Texture backgroundTexture;
    private static final float BG_SPEED = 0.3f;




    private final Vector3 mouse = new Vector3(); // 마우스 클릭시
    private final Vector2 forceDir = new Vector2(); // 힘의 방향


    @Override
    public void create() {
        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal("KnightLeft.png"));
        blockTexture = new Texture(Gdx.files.internal("block.png"));
        flagTexture = new Texture(Gdx.files.internal("flag.png"));
        backgroundTexture = new Texture(Gdx.files.internal("Background1.jpg"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        viewport = new FitViewport(800, 600, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        // x, y, 너비, 높이. 바닥도 같은 충돌 블록입니다.
        blocks.add(new Rectangle(0, 0, 620, 60));
        blocks.add(new Rectangle(760, 0, 540, 60));
        blocks.add(new Rectangle(1440, 0, 1160, 60));
        blocks.add(new Rectangle(300, 60, 80, 64));
        blocks.add(new Rectangle(900, 60, 96, 80));
        blocks.add(new Rectangle(1080, 180, 160, 24));
        blocks.add(new Rectangle(1650, 60, 120, 70));
        blocks.add(new Rectangle(1790, 60, 120, 140));
        blocks.add(new Rectangle(1930, 60, 120, 210));
        blocks.add(new Rectangle(2100, 270, 110, 24));
        blocks.add(new Rectangle(2280, 320, 210, 24));
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
        float bgWidth = 800 + (WORLD_WIDTH - 800) * BG_SPEED;
        batch.draw(backgroundTexture, bgX, 0, bgWidth, 600);

        for (Rectangle block : blocks) {
            batch.draw(blockTexture, block.x, block.y, block.width, block.height);
        }
        batch.draw(flagTexture, flag.x, flag.y, flag.width, flag.height);
        batch.draw(playerTexture,
            player.x, player.y, player.width, player.height,
            0, 0, playerTexture.getWidth(), playerTexture.getHeight(),
            !facingLeft, false);
        batch.end();

    }



    private void update(float dt) {
        if (gameOver || dt <= 0) return;

        // 마우스 클릭 - 그 방향으로 힘
        if (Gdx.input.justTouched()) {
            mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mouse);

            forceDir.set(mouse.x - player.x, mouse.y - player.y);

            if (forceDir.len() > 0f) {
                forceDir.nor().scl(700); // 일반화후 힘만큼
                velocityX = 0;
                velocityY = 0;
                grounded = false;
            }
        }


        // 1. 좌우 이동과 지상 점프
        velocityX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocityX -= MOVE_SPEED;
            facingLeft = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocityX += MOVE_SPEED;
            facingLeft = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && grounded) {
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

        // 4. 낭떠러지에 떨어지면 시작점으로 복귀합니다.
        if (player.y < -100) {
            player.setPosition(80, 60);
            velocityY = 0;
            grounded = false;
        }

        // 5. 깃발에 닿으면 게임 로직을 멈추고 마지막 화면을 유지합니다.
        if (player.overlaps(flag)) gameOver = true;

        // 6. 카메라는 X축으로만 따라갑니다.
        camera.position.x = MathUtils.clamp(player.x + player.width / 2, 400, WORLD_WIDTH - 400);
        camera.update();
    }

    private void moveX(float dt) {
        player.x += (velocityX + forceDir.x) * dt;
        for (Rectangle block : blocks) {
            if (!player.overlaps(block)) continue;
            if (velocityX > 0) player.x = block.x - player.width;
            else if (velocityX < 0) player.x = block.x + block.width;
        }
        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
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
                grounded = true;
            } else if (dy > 0) { // 올라가던 중: 블록 아래에 머리 충돌
                player.y = block.y - player.height;
                velocityY = 0;
            }
        }
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
