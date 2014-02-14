package cnrobot;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;
import robocode.WinEvent;
import robocode.util.Utils;

public class ChuckNoris extends AdvancedRobot {
	HashMap<Bullet, String> dados = new HashMap<Bullet, String>();
	StringBuffer buffer = new StringBuffer();
	double largura;
	double altura;
	int direcaoDeMovimento = 1;// -1 direção contrária
	
	@Override
	public void run() {
		largura = getBattleFieldWidth();
		altura = getBattleFieldHeight();
		
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true); 
		
		//Design do Robô
		setBodyColor(new Color(128, 128, 50));
		setGunColor(new Color(50, 50, 20));
		setRadarColor(new Color(200, 200, 70));
		setScanColor(Color.white);
		setBulletColor(Color.blue);
		
		while(true){
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();//enemies absolute bearing
		double velocidadeLateral = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//enemies later velocity
		double gunTurnAmt;//amount to turn our gun
		double distInimigo = e.getDistance();
		double dist_discretizada = 0.0;
		
		if(distInimigo%50<25)
			dist_discretizada = distInimigo - distInimigo%50;
		else
			dist_discretizada = distInimigo + (50 - (distInimigo%50));
	
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
		
		if(Math.random()>.9){
			setMaxVelocity((12*Math.random())+12);//randomly change speed
		}
		
		Bullet bala;
		double potencia;
		
		//A movimentação do é realizada de acordo com a localização do alvo, 
		//tentando ficar o mais próximo possível dele.
		
		//se a distância for maior que 150
		if (e.getDistance() > 150) {
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+velocidadeLateral /22);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt); //turn our gun
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+velocidadeLateral /getVelocity()));//drive towards the enemies predicted future location
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);//move forward
			
			potencia = 3 - (Math.random()*3);
			bala = fireBullet(potencia);
			dados.put(bala, dist_discretizada +","+velocidadeLateral+","+potencia);
			
			
		}
		else{//if we are close enough...
			gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+velocidadeLateral /15);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt);//turn our gun
			setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
			setAhead((e.getDistance() - 140)*direcaoDeMovimento);//move forward
			
			potencia = 3 - (Math.random()*3);
			bala = fireBullet(potencia);
			dados.put(bala, dist_discretizada +","+velocidadeLateral+","+potencia);
		}	
		
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		Bullet bala = event.getBullet();
		
		String linha = dados.get(bala) + ",SIM";
		buffer.append(linha);
		out.println(linha);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Bullet bala = event.getBullet();
		
		String linha = dados.get(bala) + ",NAO";
		buffer.append(linha);
		out.println(linha);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet bala = event.getBullet();
		
		String linha = dados.get(bala) + ",NAO";
		buffer.append(linha);
		out.println(linha);
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
		// TODO Auto-generated method stub
		super.onDeath(event);
	}

	@Override
	public void onWin(WinEvent event) {
		// TODO Auto-generated method stub
		super.onWin(event);
	}
	
	public void saveFile(){
		/*try {
			File resultados= new File("resultado.txt");
			StringBuffer buffer = new StringBuffer();
			
			BufferedReader bufferReader = new BufferedReader(new FileReader(resultados));
			
			String linha;
			while((linha = bufferReader.readLine())!= null)
				buffer.append(linha + "\n");
			
			FileOutputStream escritor = new FileOutputStream(resultados);
			DataOutputStream escritorTempoReal = new DataOutputStream(escritor);
			
			String aSerEscrito = buffer.substring(0,buffer.length()-1);
			String nova = "";
			
			for(int i=0; i<aSerEscrito.length();i++){
				char c = aSerEscrito.charAt(i);
				if(!ehSujeira(c))
					nova += c;
			}
			escritorTempoReal.writeChars(nova);
			
			bufferReader.close();
			escritor.close();
			escritorTempoReal.close();
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}*/
	}
	
}
