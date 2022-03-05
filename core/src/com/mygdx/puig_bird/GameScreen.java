package com.mygdx.puig_bird;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final Bird game;
    Texture backgroundImage;
    OrthographicCamera camera;
    Texture birdImage;
    Rectangle player;
    Texture pipeUpImage;
    Texture pipeDownImage;
    Array<Rectangle> obstacles;
    long lastObstacleTime;

    float speedy;
    float gravity;

    float score;
    boolean dead;

    Sound flapSound;
    Sound failSound;

    public GameScreen(final Bird gam) {
        this.game = gam;
        // load the images
        backgroundImage = new Texture(Gdx.files.internal("background.png"));
        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        birdImage = new Texture(Gdx.files.internal("bird.png"));
        // create a Rectangle to logically represent the player
        player = new Rectangle();
        player.x = 200;
        player.y = 480 / 2 - 64 / 2;
        player.width = 64;
        player.height = 45;

        speedy = 0;
        gravity = 850f;

        pipeUpImage = new Texture(Gdx.files.internal("pipe_up.png"));
        pipeDownImage = new Texture(Gdx.files.internal("pipe_down.png"));

        // create the obstacles array and spawn the first obstacle
        obstacles = new Array<Rectangle>();
        spawnObstacle();
        score = 0;

        // load the sound effects
        flapSound = Gdx.audio.newSound(Gdx.files.internal("flap.wav"));
        failSound = Gdx.audio.newSound(Gdx.files.internal("fail.wav"));
    }

    @Override
    public void render(float delta) {

        //Render
        // clear the screen with a color
        ScreenUtils.clear(0.3f, 0.8f, 0.8f, 1);
        // tell the camera to update its matrices.
        camera.update();
        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);
        // begin a new batch
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0);
        game.batch.draw(birdImage, player.x, player.y);
        // Dibuixa els obstacles: Els parells son tuberia inferior,
        //els imparells tuberia superior
        for(int i = 0; i < obstacles.size; i++) {
            game.batch.draw(
                    i % 2 == 0 ? pipeUpImage : pipeDownImage,
                    obstacles.get(i).x, obstacles.get(i).y);
        }
        game.font.draw(game.batch, "Score: " + (int)score, 10, 470);
        game.batch.end();
        //Logica
        // process user input
        if (Gdx.input.justTouched()) {
            speedy = 400f;
            flapSound.play();
        }
        // Comprova que el jugador no es surt de la pantalla.
        // Si surt per la part inferior, game over
        if (player.y > 480 - 45){
            player.y = 480 - 45;
        }
        if (player.y < 0 - 45) {
            dead = true;
        }

        //Actualitza la posició del jugador amb la velocitat vertical
        player.y += speedy * Gdx.graphics.getDeltaTime();
        //Actualitza la velocitat vertical amb la gravetat
        speedy -= gravity * Gdx.graphics.getDeltaTime();
        //La puntuació augmenta amb el temps de joc
        score += Gdx.graphics.getDeltaTime();

        // Comprova que el jugador no es surt de la pantalla.
        // Si surt per la part inferior, game over
        if (player.y > 480 - 45){
            player.y = 480 - 45;
        }else if (player.y < 0 - 45){
            dead = true;
        }

        // Comprova si cal generar un obstacle nou
        if (TimeUtils.nanoTime() - lastObstacleTime > 1500000000)
            spawnObstacle();
        // Mou els obstacles. Elimina els que estan fora de la pantalla
        // Comprova si el jugador colisiona amb un obstacle,
        // llavors game over
        Iterator<Rectangle> iter = obstacles.iterator();
        while (iter.hasNext()) {
            Rectangle tuberia = iter.next();
            tuberia.x -= 200 * Gdx.graphics.getDeltaTime();
            if (tuberia.x < -64)
                iter.remove();
            if (tuberia.overlaps(player)) {
                dead = true;
            }
        }

        if(dead)
        {
            game.lastScore = (int)score;
            failSound.play();
            if(game.lastScore > game.topScore)
                game.topScore = game.lastScore;
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        pipeUpImage.dispose();
        pipeDownImage.dispose();

        failSound.dispose();
        flapSound.dispose();
    }

    private void spawnObstacle() {
        // Calcula la alçada de l'obstacle aleatòriament
        float holey = MathUtils.random(50, 230);
        // Crea dos obstacles: Una tubería superior i una inferior
        Rectangle pipe1 = new Rectangle();
        pipe1.x = 800;
        pipe1.y = holey - 230;
        pipe1.width = 64;
        pipe1.height = 230;
        obstacles.add(pipe1);
        Rectangle pipe2 = new Rectangle();
        pipe2.x = 800;
        pipe2.y = holey + 200;
        pipe2.width = 64;
        pipe2.height = 230;
        obstacles.add(pipe2);
        lastObstacleTime = TimeUtils.nanoTime();
    }
}