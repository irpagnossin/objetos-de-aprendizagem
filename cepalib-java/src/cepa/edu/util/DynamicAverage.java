/*
 * arquivo: DynamicMean.java
 * autor: Ivan Ramos Pagnossin
 * data: 2006.09.22
 * Copyright 2006 viaVoIP ltda, all rights reserved.
 */
package cepa.edu.util;

/**
 * Um objeto da classe <code>DynamicMean</code> calcula continuamente a média de uma lista de números (<code>double</code>), passados um a um
 * através do método <code>put</code>. Os números individualmente, contudo, não são armazenados.
 * @author Ivan Ramos Pagnossin
 */
public class DynamicAverage{

	private double mean = 0;	// Média.
	private int N = 0;			// Quantidade de itens inseridos.
	private double[] numbers;
	private boolean fixedN = false;
	private int lastInserted = 0;
	
	public DynamicAverage () {}
	
	public DynamicAverage ( final int N ) {
		this.N = N;		
		fixedN = true;
		
		numbers = new double[N];
		for ( short i = 0; i < N; i++ ) numbers[i] = 0;
	}
	
	/**
	 * Acrescenta um valor à média.
	 * @param item valor a ser inserido na média.
	 */
	public void put( double item ){
		
		if (fixedN) {
			lastInserted = (++lastInserted) % N;
			numbers[lastInserted] = item;
			
			mean = 0;
			for (short i = 0; i < N; i++) mean += numbers[i];
			mean /= N;
			
		} else {
			++N;
			mean = ( N == 1 ? item : (double) (N-1)/N * (item/(N-1) + mean));
		}
	}
	
	/**
	 * Obtém a média.
	 * @return a média.
	 */
	public double getMean(){
		return( mean );
	}	
	
	/**
	 * Retorna a média para o valor inicial: zero.
	 */
	public void reset(){
		mean = 0;
		if (fixedN) for (short i = 0; i < N; i++ ) numbers[i] = 0;
		else N = 0;
	}
	
}
