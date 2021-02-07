package com.example.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] passaro;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Random numeroRandomico;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoTopo;
	private Rectangle retanguloCanoBaixo;



	//atributos de configuração
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float posicaoInicialVertical;
	private int estadoJogo = 0; // 0 == jogo não iniciado, 1 == jogo iniciado, 2 == game over
	private int pontuacao=0;


	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;
	private float deltaTime;
	private float alturaEntreCanosRandomica;
	private boolean marcouPonto;

	//camera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final  float VIRTUAL_WIDTH = 768;
    private final  float VIRTUAL_HEIGHT = 1024;


	@Override
	public void create () {

		batch = new SpriteBatch();
		numeroRandomico = new Random();
		circuloPassaro = new Circle();

		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(6);

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		passaro = new Texture[ 3 ];
		passaro[0] = new Texture("passaro1.png");
		passaro[1] = new Texture("passaro2.png");
		passaro[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo.png");
		canoTopo = new Texture("cano_topo.png");
		gameOver = new Texture("game_over.png");

        //configuração camera
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2,VIRTUAL_HEIGHT/2,0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera );

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo  = VIRTUAL_HEIGHT;
		posicaoInicialVertical = alturaDispositivo / 2;
		posicaoMovimentoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 300;

	}

	@Override
	public void render () {

		camera.update();

		//limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();

		//variação para a troca de sprite do passaro
		variacao += deltaTime * 7;
		if (variacao > 2) variacao = 0;

		//verifica o estado do jogo para iniciar 0=jogo não inicado 1=jogo iniciado
		if (estadoJogo ==0){
			if (Gdx.input.justTouched()){
				estadoJogo = 1;
			}
		} else {

			velocidadeQueda++;
			//posiçao e queda do passaro
			if (posicaoInicialVertical > 2 || velocidadeQueda < 0)
				posicaoInicialVertical = (int) (posicaoInicialVertical - velocidadeQueda);

			//estado de jogo ==1 jogo rodando normal
			if (estadoJogo == 1){

				//configura movimento do cano
				posicaoMovimentoCanoHorizontal -= deltaTime * 200;

				//configura salto do passaro
				if (Gdx.input.justTouched()) {
					velocidadeQueda = -15;
					variacao = 2;
				}

				//verifica se o cano saiu da tela
				if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
					posicaoMovimentoCanoHorizontal = larguraDispositivo + 50;
					alturaEntreCanosRandomica = numeroRandomico.nextInt(400) - 200;
					marcouPonto = false;
				}

				//altera a poontuação
				if (posicaoMovimentoCanoHorizontal < 120){
					if (!marcouPonto)
						pontuacao++;
					marcouPonto = true;
				}

			} else { // gameover- estado == 2
				if (Gdx.input.justTouched()){
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialVertical = alturaDispositivo / 2;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
				}
			}
		}

		// configurar dados de projeção da camera
		batch.setProjectionMatrix( camera.combined );

		batch.begin();
		batch.draw(fundo,0,0, larguraDispositivo, alturaDispositivo );

		batch.draw( canoTopo, posicaoMovimentoCanoHorizontal,alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandomica);
		batch.draw( canoBaixo, posicaoMovimentoCanoHorizontal,alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos /2 + alturaEntreCanosRandomica );

		batch.draw(passaro[ (int) variacao ] ,120, posicaoInicialVertical);

		fonte.draw(batch, String.valueOf(pontuacao),larguraDispositivo / 2, alturaDispositivo - 50);

		if (estadoJogo == 2){
			batch.draw(gameOver,larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
			mensagem.draw(batch, "Toque para Reiniciar!",larguraDispositivo / 2 - 200, alturaDispositivo / 2 - gameOver.getHeight() / 2);
		}

		batch.end();

		// configura formar para colisão
		circuloPassaro.set(120 + passaro[0].getWidth() / 2,posicaoInicialVertical + passaro[0].getHeight() / 2,passaro[0].getWidth() /2);

		retanguloCanoTopo = new Rectangle(
				posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandomica,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		retanguloCanoBaixo = new Rectangle(
				posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos /2 + alturaEntreCanosRandomica,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);


		//teste de colisao
		if (Intersector.overlaps(circuloPassaro,retanguloCanoBaixo) || Intersector.overlaps(circuloPassaro, retanguloCanoTopo)
		|| posicaoInicialVertical <=0 || posicaoInicialVertical >= alturaDispositivo ){
            estadoJogo = 2;
		}

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width,height);
	}
}
