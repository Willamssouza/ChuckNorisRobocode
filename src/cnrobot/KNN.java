package cnrobot;

public class KNN {
	
	public Dados[] kVizinhos(Dados[] dataSet, Dados atual, int numVizinhos){
		Dados[] vizinhos = new Dados[numVizinhos];
		double[] distanciaVizinho = new double[numVizinhos];
		double maiorDistancia;
		int indiceMaiorDistancia;
		
		if(dataSet.length <= numVizinhos)
			return dataSet;
		
		for (int i = 0; i < numVizinhos; i++){
			vizinhos[i] = dataSet[i];
		}
		
		distanciaVizinho[0] = distanciaEuclidiana(atual, dataSet[0]);
		maiorDistancia = distanciaVizinho[0];
		indiceMaiorDistancia = 0;
		
		for (int i = 1; i < numVizinhos; i++){
			distanciaVizinho[i] = distanciaEuclidiana(atual, vizinhos[i]);
			if (distanciaVizinho[i] > maiorDistancia){
				maiorDistancia = distanciaVizinho[i];
				indiceMaiorDistancia = i;
			}
		}
		
		double distanciaAtual;
		for (int i = numVizinhos; i < dataSet.length; i++){
			distanciaAtual = distanciaEuclidiana(atual, dataSet[i]);
			
			if(distanciaAtual < maiorDistancia){
				vizinhos[indiceMaiorDistancia] = dataSet[i];
				distanciaVizinho[indiceMaiorDistancia] = distanciaAtual;
				
				maiorDistancia = distanciaVizinho[0];
				indiceMaiorDistancia = 0;
				for (int j = 1; j < numVizinhos; j++){
					if (distanciaVizinho[j] > maiorDistancia){
						maiorDistancia = distanciaVizinho[j];
						indiceMaiorDistancia = j;
					}
				}
				
			}
		}
		
		return vizinhos;
	}
	
	private double distanciaEuclidiana(Dados x, Dados y){
		double resultado = Math.pow(x.getDistanciaInimigo() - y.getDistanciaInimigo(), 2);
		resultado += Math.pow(x.getVelocidadeLateralInimigo() - y.getVelocidadeLateralInimigo(), 2);
		resultado += Math.pow(x.getAnguloAbsolutoInimigo() - y.getAnguloAbsolutoInimigo(), 2);
		return Math.sqrt(resultado);
	}
}
