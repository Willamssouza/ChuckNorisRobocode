package cnrobot;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Classificador {
	private MultilayerPerceptron rNN;
	
	public Classificador() throws Exception {
		Instances dados = DataSource.read("dados_robocode.arff");
		dados.setClassIndex(dados.numAttributes() - 1);
		rNN = new MultilayerPerceptron();
		rNN.buildClassifier(dados);
	}

	public double atirar(Instance instance) throws Exception {
		double classe = rNN.classifyInstance(instance);
		return classe;
	}
	
	public static void main(String[] args) throws Exception{
		Classificador c = new Classificador();
		
		Instances dados = DataSource.read("dados_teste.arff");
		dados.setClassIndex(dados.numAttributes()-1);
		
		for (int i = 0 ; i < dados.numInstances(); i++){
			double classe = c.atirar(dados.instance(i));
			System.out.println(classe);
		}
	}
}
