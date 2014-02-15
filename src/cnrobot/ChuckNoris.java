package cnrobot;


import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
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
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

public class ChuckNoris extends AdvancedRobot {
	HashMap<Bullet, Dados> dadosBala = new HashMap<Bullet, Dados>();
	double largura;
	double altura;
	int direcaoDeMovimento = 1;// -1 direção contrária
	boolean aprendendo = true;
	public static int contadorBalas;
	
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
		setScanColor(Color.WHITE);
		setBulletColor(Color.YELLOW);
		
		while(true){
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		//rolamento absoluto do inimigo
		double anguloAbsolutoInimigo = e.getBearingRadians() + getHeadingRadians();
		anguloAbsolutoInimigo = Double.valueOf(String.format(Locale.US, "%.2f", anguloAbsolutoInimigo));
		
		//velocidade lateral do inimigo
		double velocidadeLateral = e.getVelocity() * Math.sin(e.getHeadingRadians() - anguloAbsolutoInimigo);
		velocidadeLateral = Double.valueOf(String.format(Locale.US, "%.2f", velocidadeLateral));
		
		double anguloArmaPreditor;
		double anguloArmaAtual;
		double distInimigo = e.getDistance();
		double dist_discretizada = 0.0;
		
		if(distInimigo%50<25)
			dist_discretizada = distInimigo - distInimigo%50;
		else
			dist_discretizada = distInimigo + (50 - (distInimigo%50));
	
		//trava o radar no focando o inimigo
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		
		//Alterna randomicamente a velocidade do robô
		if(Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);
		}
		
		
		double potenciaBala;
		Random rnd = new Random();
		
		//A movimentação do é realizada de acordo com a localização do alvo, 
		//tentando ficar o mais próximo possível dele.
		if (e.getDistance() > 150) {
			anguloArmaAtual = getGunHeadingRadians();
			anguloArmaAtual = Double.valueOf(String.format(Locale.US, "%.2f", anguloArmaAtual));
			
			anguloArmaPreditor = robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo - anguloArmaAtual + velocidadeLateral /22);
			anguloArmaPreditor = Double.valueOf(String.format(Locale.US, "%.2f", anguloArmaPreditor));
			
			//gira arma para a posição prevista do inimigo
			setTurnGunRightRadians(anguloArmaPreditor);
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo-getHeadingRadians()+velocidadeLateral /getVelocity()));//drive towards the enemies predicted future location
			
			//move-se em direção ao inimigo
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);
			
			//potenciaBala = 2*(1 + rnd.nextInt(15))/10.0;
			if (getEnergy() < 10)
				potenciaBala = 0.5;
			else
				potenciaBala = 3;
			
			Bullet bala = fireBullet(potenciaBala);
			
			//Dados referente ao tiro
			Dados dados = new Dados();
			dados.setDistanciaInimigo(dist_discretizada);
			dados.setVelocidadeLateral(velocidadeLateral);
			dados.setPotenciaBala(potenciaBala);
			dados.setAnguloArmaAtual(anguloArmaAtual);
			dados.setAnguloAbsolutoInimigo(anguloAbsolutoInimigo);
			dados.setAnguloArmaPreditor(anguloArmaPreditor);
			dados.setAcertou(false);
			
			dadosBala.put(bala, dados);
			
		} 
		else{ //Se estiver próximo do inimigo, tenta ficar perpendicular a ele
			anguloArmaAtual = getGunHeadingRadians();
			anguloArmaAtual = Double.valueOf(String.format(Locale.US, "%.2f", anguloArmaAtual));
			
			anguloArmaPreditor = robocode.util.Utils.normalRelativeAngle(anguloAbsolutoInimigo - anguloArmaAtual + velocidadeLateral /15);
			anguloArmaPreditor = Double.valueOf(String.format(Locale.US, "%.2f", anguloArmaPreditor));
			
			//gira arma para a posição prevista do inimigo
			setTurnGunRightRadians(anguloArmaPreditor);
			
			//mantém-se perpendicular ao inimigo
			setTurnLeft(-90-e.getBearing());
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);
			
			//potencia = 2*(1 + rnd.nextInt(15))/10.0;
			if (getEnergy() < 10)
				potenciaBala = 0.5;
			else
				potenciaBala = 3;
			
			Bullet bala = fireBullet(potenciaBala);
			
			//Dados referente ao tiro
			Dados dados = new Dados();
			dados.setDistanciaInimigo(dist_discretizada);
			dados.setVelocidadeLateral(velocidadeLateral);
			dados.setPotenciaBala(potenciaBala);
			dados.setAnguloArmaAtual(anguloArmaAtual);
			dados.setAnguloAbsolutoInimigo(anguloAbsolutoInimigo);
			dados.setAnguloArmaPreditor(anguloArmaPreditor);
			dados.setAcertou(false);
			
			dadosBala.put(bala, dados);
		}	
		
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(true);
		//String linha = dadosBala.get(bala) + ", SIM\n";
		//buffer.append(linha);
		out.println(++contadorBalas+dados.toString());
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		//String linha = dadosBala.get(bala) + ", NAO\n";
		//buffer.append(linha);
		out.println(++contadorBalas+dados.toString());
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet bala = event.getBullet();
		Dados dados = dadosBala.get(bala);
		dados.setAcertou(false);
		//String linha = dadosBala.get(bala) + ", NAO\n";
		//buffer.append(linha);
		out.println(++contadorBalas+dados.toString());
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
						d.getVelocidadeLateral()+","+
						d.getPotenciaBala()+","+
						d.getAnguloArmaAtual()+","+
						d.getAnguloAbsolutoInimigo()+","+
						d.getAnguloArmaPreditor()+","+
						(d.isAcertou() ? "SIM" : "NAO")+"\n";
				
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
