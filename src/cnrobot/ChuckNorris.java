package cnrobot;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
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
/**
 * Universidade Federal de Sergipe
 * 
 * @author Willams de Souza Santos (willamssouza@hotmail.com)
 * 
 *
 */
public class ChuckNorris extends AdvancedRobot {
	final static int TAMANHO_DATASET = 5000;
	final int VELOCIDADE_MAXIMA = 8;
	
	HashMap<Bullet, Dados> dadosBala = new HashMap<Bullet, Dados>();
	public static ArrayList<Dados> dataSet = new ArrayList<Dados>(TAMANHO_DATASET);
	double largura;
	double altura;
	
	int direcaoDeMovimento = 1;// -1 direção contrária
	boolean salvarLog = false;
	
	static int totalDisparos = 0;
	static int totalErros = 0;
	
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
		//Move - se Acordo com a localização do oponente
		movimentacao(e);
		
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
		
		//Definindo potência da bala baseado na distância do inimigo
		if (e.getDistance() > 500) {
			potenciaBala = 1.0;
		} 
		else if (e.getDistance() > 150){	
			potenciaBala = 2.0;		
		} else {
			potenciaBala = 3.0;
		}
		
		//Diminiu o gasto de energia quando o inimigo estar com pouca energia
		if (getEnergy() < 10 || e.getEnergy() < 10)
			potenciaBala = 1.0;
		
		/****Normalizando os dados****/
		double distanciaInimigoN = distanciaInimigo / Math.sqrt((Math.pow(largura, 2)+Math.pow(altura, 2)));
		double velocidadeLateralN = Math.abs(velocidadeLateral / VELOCIDADE_MAXIMA);
		double anguloAbsolutoInimigoN = anguloAbsolutoInimigo / (2 * Math.PI);
		
		Dados dadosAtual = new Dados();
		dadosAtual.setDistanciaInimigo(distanciaInimigoN);
		dadosAtual.setVelocidadeLateral(velocidadeLateralN);
		dadosAtual.setAnguloAbsolutoInimigo(anguloAbsolutoInimigoN);
		
		
		KNN knn = new KNN();
		
		double porcentagemDeRoundParaTreino = (double)this.getBattleNum()/this.getNumBattles();
		
		//Utiliza 30 % do número total de rounds da batalha para treino.
		//Gerando ângulo de tiros aleatórios
		if (porcentagemDeRoundParaTreino > 0.3){
			Dados[] dataSetArray = new Dados[dataSet.size()];
			dataSetArray = dataSet.toArray(dataSetArray);
			
			int k = 3;
			Dados[] vizinhos = knn.kVizinhos(dataSetArray, dadosAtual, k);
			fatorAnguloTiro = 0.0;
			
			for (int i = 0; i < k; i++)
				fatorAnguloTiro += vizinhos[i].getFatorAnguloTiro() * (2*Math.PI);
			
			fatorAnguloTiro /= k;
			

		} else {
			if (velocidadeLateral == 0)
				fatorAnguloTiro = 0;
			else
				fatorAnguloTiro = Math.random() * 0.5;
		}
		
		//Ajustando o fator ângulo para direção que o oponente se move
		if (velocidadeLateral < 0){
			fatorAnguloTiro = -fatorAnguloTiro; 
		}
		
		//calcula o ângulo relativo para virar a arma
		anguloArmaAtual = getGunHeadingRadians();
		anguloVirarArma = robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo - anguloArmaAtual + fatorAnguloTiro);
		setTurnGunRightRadians(anguloVirarArma);
		
		//Normalizando angulo de tiro
		double fatorAnguloTiroN = Math.abs(fatorAnguloTiro/(2 * Math.PI));
		dadosAtual.setFatorAnguloTiro(fatorAnguloTiroN);
		
		//para de atirar se tiver com pouca energia 
		if (getEnergy() > 2.0){
			bala = fireBullet(potenciaBala);
			dadosBala.put(bala, dadosAtual);
		} else if (e.getEnergy() == 0.0){
			bala = fireBullet(0.1);
			dadosBala.put(bala, dadosAtual);
		}
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
		totalDisparos++;
		out.println(dataSet.size()+dados.toString());
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		dadosBala.remove(bala);
		totalErros++;
		totalDisparos++;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		totalErros++;
		totalDisparos++;
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
		if (salvarLog){
			saveFile();
		}
	}

	@Override
	public void onWin(WinEvent event) {
		if (salvarLog){
			saveFile();
		}
	}
	
	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		double taxaDeAcerto = 0;
		if (totalDisparos > 0){
			taxaDeAcerto = (double) (totalDisparos-totalErros)/totalDisparos;
		}
		out.println(taxaDeAcerto);
	}
	
	/** Movimentação Randômica com Wall Avoidance **/
	public void movimentacao(ScannedRobotEvent e){
		//Alterna randomicamente a velocidade e direção de movimento do robô
		if(Math.random()> 0.9){
			setMaxVelocity((12*Math.random())+12);
			direcaoDeMovimento = -direcaoDeMovimento;
		}
		
		double direcaoObjetivo = e.getBearingRadians() + getHeadingRadians()- (Math.PI/2*direcaoDeMovimento);
		
		//Cria uma área segura evitando colisão com a parede
		Rectangle2D areaSegura = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36,getBattleFieldHeight()-36);
		while (!areaSegura.contains(getX()+Math.sin(direcaoObjetivo)*120, getY()+Math.cos(direcaoObjetivo)*120))
		{
			direcaoObjetivo += direcaoDeMovimento*0.1;	
		}
		double anguloGirar = robocode.util.Utils.normalRelativeAngle(direcaoObjetivo-getHeadingRadians());
		if (Math.abs(anguloGirar) > Math.PI/2)
		{
			anguloGirar = robocode.util.Utils.normalRelativeAngle(anguloGirar + Math.PI);
			setBack(100);
		}
		else
			setAhead(100);
		setTurnRightRadians(anguloGirar);
	}

	/** Salva log */
	public void saveFile(){
		try {
			File resultados = getDataFile("logChuckNoris.txt");	
			String linha;
			StringBuffer buffer = new StringBuffer();
			
			RobocodeFileWriter fw = new RobocodeFileWriter(resultados);
			fw.write(buffer.toString());
			
			for (Dados d : dataSet){
				linha = d.getDistanciaInimigo()+","+
						d.getVelocidadeLateralInimigo()+","+
						d.getAnguloAbsolutoInimigo()+","+
						d.getFatorAnguloTiro()+"\n";
				
				fw.write(linha);
			}
			
			fw.flush();
			fw.close();
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
}
