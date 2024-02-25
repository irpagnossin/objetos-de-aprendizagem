/*
 * arquivo: EstimatedTime.java
 * autor: Ivan Ramos Pagnossin
 * data: 2006.09.25
 * Copyright 2006 viaVoIP ltda, all rights reserved.
 */
package cepa.edu.util;

/**
 * Um objeto da classe <code>EstimatedTime</code> contabiliza dinamicamente o tempo médio gasto numa determinada tarefa repetitiva.
 * @author Owner
 *
 */
public class EstimatedTime{

	private Cronometer cronometer = new Cronometer();
	private DynamicAverage average = new DynamicAverage();
		
	/**
	 * Marca um instante.
	 */
	public void tag(){
		if( !cronometer.isRunning() ){
			cronometer.start();
		}
		else{
			average.put( (double) cronometer.read() );
			cronometer.reset();
		}
	}
	
	/**
	 * Duração estimada da execução de <code>N</code> tarefas iguais (ETE, Estimated time of enroute).
	 * @param N Quantidade de tarefas restantes.
	 * @return ETE, em milisegundos.
	 */
	public long ETE( long N ){		
		return( Math.round(N * average.getMean()) );
	}
	
	/**
	 * Instante previsto para o término de <code>N</code> tarefas iguais (ETA, Estimated time of arrival).
	 * @param N Quantidade de tarefas restantes.
	 * @return ETA, em milisegundos (<code>System.currentTimeMillis</code>).
	 */
	public long ETA( long N ){
		return( System.currentTimeMillis() + ETE(N) );
	}
	
	/**
	 * Reinicia as estimativas.
	 */
	public void reset(){
		cronometer.reset();
		average.reset();
	}
	
}
