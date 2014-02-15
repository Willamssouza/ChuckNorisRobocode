package cnrobot;

public class Dados {
	private double distanciaInimigo;
	private double velocidadeLateral; 
	private double potenciaBala;
	private double anguloArmaAtual; 
	private double anguloAbsolutoInimigo; 
	private double anguloArmaPreditor;
	private boolean acertou;
	
	public double getDistanciaInimigo() {
		return distanciaInimigo;
	}
	public void setDistanciaInimigo(double distanciaInimigo) {
		this.distanciaInimigo = distanciaInimigo;
	}
	public double getVelocidadeLateral() {
		return velocidadeLateral;
	}
	public void setVelocidadeLateral(double velocidadeLateral) {
		this.velocidadeLateral = velocidadeLateral;
	}
	public double getPotenciaBala() {
		return potenciaBala;
	}
	public void setPotenciaBala(double potenciaBala) {
		this.potenciaBala = potenciaBala;
	}
	public double getAnguloArmaAtual() {
		return anguloArmaAtual;
	}
	public void setAnguloArmaAtual(double anguloArmaAtual) {
		this.anguloArmaAtual = anguloArmaAtual;
	}
	public double getAnguloAbsolutoInimigo() {
		return anguloAbsolutoInimigo;
	}
	public void setAnguloAbsolutoInimigo(double anguloAbsolutoInimigo) {
		this.anguloAbsolutoInimigo = anguloAbsolutoInimigo;
	}
	public double getAnguloArmaPreditor() {
		return anguloArmaPreditor;
	}
	public void setAnguloArmaPreditor(double anguloArmaPreditor) {
		this.anguloArmaPreditor = anguloArmaPreditor;
	}
	public boolean isAcertou() {
		return acertou;
	}
	public void setAcertou(boolean acertou) {
		this.acertou = acertou;
	}
	@Override
	public String toString() {
		return "Dados [distanciaInimigo=" + distanciaInimigo
				+ ", velocidadeLateral=" + velocidadeLateral
				+ ", potenciaBala=" + potenciaBala + ", anguloArmaAtual="
				+ anguloArmaAtual + ", anguloAbsolutoInimigo="
				+ anguloAbsolutoInimigo + ", anguloArmaPreditor="
				+ anguloArmaPreditor + ", acertou=" + acertou + "]";
	}

	

}
