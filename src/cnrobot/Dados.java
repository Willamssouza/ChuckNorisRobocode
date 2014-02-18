package cnrobot;

public class Dados {
	private double distanciaInimigo;
	private double velocidadeLateralInimigo; 
	private double anguloAbsolutoInimigo;
	private double fatorAnguloTiro;
	private boolean acertou;
	
	public double getDistanciaInimigo() {
		return distanciaInimigo;
	}
	public void setDistanciaInimigo(double distanciaInimigo) {
		this.distanciaInimigo = distanciaInimigo;
	}
	public double getVelocidadeLateralInimigo() {
		return velocidadeLateralInimigo;
	}
	public void setVelocidadeLateral(double velocidadeLateralInimigo) {
		this.velocidadeLateralInimigo = velocidadeLateralInimigo;
	}

	public boolean isAcertou() {
		return acertou;
	}
	public void setAcertou(boolean acertou) {
		this.acertou = acertou;
	}
	public double getAnguloAbsolutoInimigo() {
		return anguloAbsolutoInimigo;
	}
	public void setAnguloAbsolutoInimigo(double anguloAbsolutoInimigo) {
		this.anguloAbsolutoInimigo = anguloAbsolutoInimigo;
	}
	public double getFatorAnguloTiro() {
		return fatorAnguloTiro;
	}
	public void setFatorAnguloTiro(double fatorAnguloTiro) {
		this.fatorAnguloTiro = fatorAnguloTiro;
	}
	@Override
	public String toString() {
		return "Dados [distanciaInimigo=" + distanciaInimigo
				+ ", velocidadeLateralInimigo=" + velocidadeLateralInimigo
				+ ", anguloAbsolutoInimigo=" + anguloAbsolutoInimigo
				+ ", fatorAnguloTiro=" + fatorAnguloTiro + ", acertou="
				+ acertou + "]";
	}

}
