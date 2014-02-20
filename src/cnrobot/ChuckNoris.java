package cnrobot;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
	final static int TAMANHO_DATASET = 2000;
	final int VELOCIDADE_MAXIMA = 8;
	
	HashMap<Bullet, Dados> dadosBala = new HashMap<Bullet, Dados>();
	public static ArrayList<Dados> dataSet = new ArrayList<Dados>(TAMANHO_DATASET);
	double largura;
	double altura;
	
	int direcaoDeMovimento = 1;// -1 direção contrária
	boolean aprendendo = false;
	

	
	@Override
	public void run() {

		largura = getBattleFieldWidth();
		altura = getBattleFieldHeight();
		
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true); 
		
		//Design do Robô
		setBodyColor(Color.WHITE);
		setGunColor(Color.WHITE);
		setRadarColor(Color.WHITE);
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
		
		//distância do inimigo
		double distanciaInimigo = e.getDistance();
		
		double fatorAnguloTiro;
		double anguloVirarArma;
		double anguloArmaAtual;
		double potenciaBala;
		Bullet bala;
		
		//trava o radar no focando o inimigo
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		
		//Alterna randomicamente a velocidade do robô
		if(Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);
		}
		
		//A movimentação do é realizada de acordo com a localização do alvo, 
		//tentando ficar o mais próximo possível dele.
		if (e.getDistance() > 150) {
			setBulletColor(Color.WHITE);
			fatorAnguloTiro = velocidadeLateral/22;
			
			//move-se em direção ao inimigo
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo-getHeadingRadians()+velocidadeLateral /getVelocity()));//drive towards the enemies predicted future location
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);
			
			if (getEnergy() < 10)
				potenciaBala = 1.0;
			else
				potenciaBala = 2;	
			
		} 
		else{ //Se estiver próximo do inimigo, tenta ficar perpendicular a ele
			setBulletColor(Color.WHITE);	
			fatorAnguloTiro = velocidadeLateral/15;
						
			//mantém-se perpendicular ao inimigo
			setTurnLeft(-90-e.getBearing());
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);
			
			if (getEnergy() < 10)
				potenciaBala = 1.0;
			else
				potenciaBala = 3;					
		}
		
		/****Normalizando os dados****/
		double distanciaInimigoN = distanciaInimigo / Math.sqrt((Math.pow(largura, 2)+Math.pow(altura, 2)));
		double velocidadeLateralN = velocidadeLateral / VELOCIDADE_MAXIMA;
		double anguloAbsolutoInimigoN = anguloAbsolutoInimigo / (2 * Math.PI);
		
		Dados dadosAtual = new Dados();
		dadosAtual.setDistanciaInimigo(distanciaInimigoN);
		dadosAtual.setVelocidadeLateral(velocidadeLateralN);
		dadosAtual.setAnguloAbsolutoInimigo(anguloAbsolutoInimigoN);
		
		
		KNN knn = new KNN();
		
		if (dataSet.size() > 100){
			Dados[] dataSetArray = new Dados[dataSet.size()];
			dataSetArray = dataSet.toArray(dataSetArray);
			Dados[] vizinhos = knn.kVizinhos(dataSetArray, dadosAtual, 1);
			fatorAnguloTiro = 0.0;
			fatorAnguloTiro = vizinhos[0].getFatorAnguloTiro() * (2*Math.PI);
			/*for (int i = 0; i < 3; i++){
				fatorAnguloTiro += vizinhos[i].getFatorAnguloTiro() * (2*Math.PI);
			}
			fatorAnguloTiro /= 3;*/
		} else {
			/*if (velocidadeLateral == 0)
				fatorAnguloTiro = 0;
			else
				fatorAnguloTiro = velocidadeLateral > 0 ? Math.random() * 0.5 : -Math.random() * 0.5;*/
		}
		
		//calcula o ângulo relativo para virar a arma
		anguloArmaAtual = getGunHeadingRadians();
		anguloVirarArma = robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo - anguloArmaAtual + fatorAnguloTiro);
		setTurnGunRightRadians(anguloVirarArma);
		
		double fatorAnguloTiroN = fatorAnguloTiro/(2 * Math.PI);
		dadosAtual.setFatorAnguloTiro(fatorAnguloTiroN);
		
		bala = fireBullet(potenciaBala);
		
		dadosBala.put(bala, dadosAtual);	
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(true);
		
		Random rnd = new Random();
		//quando encher o buffer retorna para o inicio
		if (dataSet.size() == TAMANHO_DATASET){
			dataSet.remove(rnd.nextInt(TAMANHO_DATASET));
		}
		dataSet.add(dados);
		
		out.println(dataSet.size()+dados.toString());
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		dadosBala.remove(bala);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);

	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		//direcaoDeMovimento = -direcaoDeMovimento;
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
						d.getFatorAnguloTiro()+"\n";
				
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
