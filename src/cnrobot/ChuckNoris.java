package cnrobot;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileWriter;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

public class ChuckNoris extends AdvancedRobot {
	HashMap<Bullet, Dados> dadosBala = new HashMap<Bullet, Dados>();
	double largura;
	double altura;
	
	int direcaoDeMovimento = 1;// -1 direção contrária
	boolean aprendendo = true;
	
	static Dados[] dataSet;
	static int posicaoDataSet = 0;
	
	final int TAMANHO_DATASET = 10000;
	final int VELOCIDADE_MAXIMA = 8;
	
	
	@Override
	public void run() {

		largura = getBattleFieldWidth();
		altura = getBattleFieldHeight();
		
		dataSet = new Dados[TAMANHO_DATASET];
		
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true); 
		
		//Design do Robô
		setBodyColor(Color.BLACK);
		setGunColor(Color.BLACK);
		setRadarColor(Color.BLACK);
		setScanColor(Color.RED);
		setBulletColor(Color.WHITE);
		
		while(true){
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		//ângulo absoluto do inimigo
		double anguloAbsolutoInimigo = e.getBearingRadians() + getHeadingRadians();
		
		//velocidade lateral do inimigo
		double velocidadeLateral = e.getVelocity() * Math.sin(e.getHeadingRadians() - anguloAbsolutoInimigo);
		//velocidadeLateral = Double.valueOf(String.format(Locale.US, "%.1f", velocidadeLateral));
		
		//distância do inimigo
		double distanciaInimigo = e.getDistance();
		
		double fatorAnguloTiro;
		double anguloVirarArma;
		double anguloArmaAtual;
		
		//trava o radar no focando o inimigo
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		
		//Alterna randomicamente a velocidade do robô
		if(Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);
		}
		
		
		double potenciaBala;
		Random rnd = new Random();
		Bullet bala;
		
		//A movimentação do é realizada de acordo com a localização do alvo, 
		//tentando ficar o mais próximo possível dele.
		if (e.getDistance() > 150) {
			anguloArmaAtual = getGunHeadingRadians();
			
			fatorAnguloTiro = velocidadeLateral/22;
			fatorAnguloTiro = Double.valueOf(String.format(Locale.US, "%.1f", fatorAnguloTiro));
			
			//calcula o ângulo relativo para virar a arma
			anguloVirarArma = robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo - anguloArmaAtual + fatorAnguloTiro);
			
			//gira arma para a posição prevista do inimigo
			setTurnGunRightRadians(anguloVirarArma);
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo-getHeadingRadians()+velocidadeLateral /getVelocity()));//drive towards the enemies predicted future location
			
			//move-se em direção ao inimigo
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);
			
			//potenciaBala = (2*(1 + rnd.nextInt(10))/10.0)+1;
			if (getEnergy() < 10)
				potenciaBala = 1.0;
			else
				potenciaBala = 2;
			

			bala = fireBullet(potenciaBala);
			
		} 
		else{ //Se estiver próximo do inimigo, tenta ficar perpendicular a ele
			anguloArmaAtual = getGunHeadingRadians();
			
			fatorAnguloTiro = velocidadeLateral/15;
			fatorAnguloTiro = Double.valueOf(String.format(Locale.US, "%.1f", fatorAnguloTiro));
			
			//calcula o ângulo relativo para virar a arma
			anguloVirarArma = robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo - anguloArmaAtual + fatorAnguloTiro);
			setTurnGunRightRadians(anguloVirarArma);
			
			//mantém-se perpendicular ao inimigo
			setTurnLeft(-90-e.getBearing());
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);
			
			//potenciaBala = (2*(1 + rnd.nextInt(10))/10.0)+1;
			if (getEnergy() < 10)
				potenciaBala = 1.0;
			else
				potenciaBala = 3;
			

			bala = fireBullet(potenciaBala);		
		}
		
		/****Normalizando os dados****/
		double distanciaInimigoN = distanciaInimigo / Math.sqrt((Math.pow(largura, 2)+Math.pow(altura, 2)));
		double velocidadeLateralN = velocidadeLateral / VELOCIDADE_MAXIMA;
		double anguloAbsolutoInimigoN = anguloAbsolutoInimigo / (2 * Math.PI);
		double fatorAnguloTiroN = fatorAnguloTiro/(2 * Math.PI);
		
		Dados dados = new Dados();
		dados.setDistanciaInimigo(distanciaInimigoN);
		dados.setVelocidadeLateral(velocidadeLateralN);
		dados.setAnguloAbsolutoInimigo(anguloAbsolutoInimigoN);
		dados.setFatorAnguloTiro(fatorAnguloTiroN);
		
		dadosBala.put(bala, dados);	
		
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(true);
		
		//quando encher o buffer retorna para o inicio
		if (posicaoDataSet == TAMANHO_DATASET){
			posicaoDataSet = 0;
		}
		dataSet[posicaoDataSet] = dados;
		posicaoDataSet++;
		
		out.println(posicaoDataSet+dados.toString());
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		dadosBala.remove(bala);
		
		//out.println(++contadorBalas+dados.toString());
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		//out.println(posicaoDataSet+dados.toString());
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		/*if (++contadorBalasRecebidas >= 3){
			direcaoDeMovimento = -direcaoDeMovimento;
		}*/
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		direcaoDeMovimento = -direcaoDeMovimento;
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		direcaoDeMovimento = -direcaoDeMovimento;
	}

	@Override
	public void onDeath(DeathEvent event) {
		if (aprendendo){
			saveFile();
		}
	}

	@Override
	public void onWin(WinEvent event) {
		if (aprendendo){
			saveFile();
		}
	}
	
	//Salva o log
	public void saveFile(){
		try {
			File resultados = getDataFile("logChuckNoris.txt");
			
			BufferedReader bufferReader = new BufferedReader(new FileReader(resultados));
			
			String linha;
			StringBuffer buffer = new StringBuffer();
			
			while((linha = bufferReader.readLine())!= null)
				buffer.append(linha + "\n");
			
			RobocodeFileWriter fw = new RobocodeFileWriter(resultados);
			fw.write(buffer.toString());
			
			for (Bullet key : dadosBala.keySet()){
				Dados d = dadosBala.get(key);
				linha = d.getDistanciaInimigo()+","+
						d.getVelocidadeLateralInimigo()+","+
						d.getAnguloAbsolutoInimigo()+","+
						d.getFatorAnguloTiro();
				
				fw.write(linha);
			}
			
			fw.flush();
			fw.close();
			bufferReader.close();
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
}
