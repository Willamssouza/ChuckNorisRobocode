package cnrobot;

import weka.clusterers.DBSCAN;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Classificador {
	private DBSCAN dbscan;
	
	public Classificador() throws Exception {
		Instances dados = DataSource.read("dados_robocode.arff");
		//dados.setClassIndex(dados.numAttributes() - 1);
		dbscan = new DBSCAN();
		dbscan.setMinPoints(2);
		dbscan.buildClusterer(dados);
	}

	public int atirar(Instance instance) throws Exception {
		int classe = dbscan.clusterInstance(instance);
		return classe;
	}
	
	public int atirar(double distanciaInimigo, double velocidadeLateral,
			double potenciaBala, double anguloArmaAtual,
			double anguloAbsolutoInimigo, double anguloArmaPreditor) throws Exception {
		
		double[] array = new double[6];
		array[0] = distanciaInimigo;
		array[1] = velocidadeLateral;
		array[2] = potenciaBala;
		array[3] = anguloArmaAtual;
		array[4] = anguloAbsolutoInimigo;
		array[5] = anguloArmaPreditor;
		
		int classe = dbscan.clusterInstance(new Instance(0,array));
		return classe;
	}
	
	public static void main(String[] args) throws Exception{
		Classificador c = new Classificador();
		
		Instances dados = DataSource.read("dados_teste.arff");
		dados.setClassIndex(dados.numAttributes()-1);
		
		for (int i = 0 ; i < dados.numInstances(); i++){
			int classe = c.atirar(dados.instance(i));
			System.out.println(classe);
		}
	}
}
