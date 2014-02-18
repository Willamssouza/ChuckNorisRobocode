package cnrobot;

public class KNN {
		/* Algoritmo k-NN
		nearestNeighbors(allPoints, currentPoint, numNeighbors, numDimensions)
	    define nearestPoints, collection of size <numNeighbors>
	    fill nearestPoints with the first <numNeighbors> points from allPoints
	
	    define nearestDistancesSq, double array of size <numNeighbors>
	    set nearestDistancesSq[0] to distanceSquared(currentPoint, nearestPoints[0])
	    define longestDistanceSq, initialize to nearestDistanceSq[0]
	    define longestIndex, initialize to 0
	
	    for x = 1 to numNeighbors-1
	        nearestDistancesSq[x] = distanceSquared(currentPoint, nearestPoints[x]);
	        if (nearestDistancesSq[x] > longestDistanceSq)
	            longestDistanceSq = nearestDistancesSq[x]
	            longestIndex = x
	
	    for x = numNeighbors to allPoints.length-1
	        thisDistanceSq = distanceSquared(currentPoint, allPoints[x]);
	        if (thisDistanceSq < longestDistanceSq)
	            nearestPoints[longestIndex] = allPoints[x]
	            nearestDistanceSq[longestIndex] = thisDistanceSq
	            find the new maximum value in nearestDistancesSq
	                set longestDistanceSq to the maximum value
	                set longestIndex to the array index of the maximum value
	
	    return nearestPoints
		end*/
	
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
