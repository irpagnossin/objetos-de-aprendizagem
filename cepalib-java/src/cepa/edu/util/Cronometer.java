/*
  arquivo: Cronometer.java
  autor: Ivan Ramos Pagnossin
  data: 2006.01.20
  copyright: viavoip
 */
package cepa.edu.util;

/**
 * A classe Cronometer encapsula um cronômetro. Um exemplo de uso é:
 * <code>
 * <p>Cronometer cron = new Cronometer();
 * <p>cron.start();
 * <p>System.out.println( cron.read() );</p>
 * <p>cron.stop();</p>
 * </code>
 * 
 * @author Ivan Ramos Pagnossin
 */

public class Cronometer{
	
	private long    beginning,	// Hora inicial do sistema, em milisegundos.
				    elapsed;    // Tempo total decorrido desde o primeiro start.
	private boolean running;	// Estado do cronômetro: cronometrando ou não.

	/**
	 * Cria um cronômetro.
	 */
	public Cronometer(){
	  	this.elapsed = 0;
    	this.running = false;
	}

	/**
	 * Inicia a cronometragem.
	 * <p>obs.: iniciar uma cronometragem já iniciada não incorre em nada. Assim, no código
	 * <p><code>cron.start();</code>
	 * <p><code>cron.start();</code>
	 * <p>o segundo <code>start</code> não faz nada.
	 */
	public final void start(){
		if ( !this.running ){
			beginning = System.currentTimeMillis();
			this.running = true;
		}		
	}

	/**
	 * Encerra a cronometragem.
	 * <p>obs.: encerrar uma cronometragem já encerrada não incorre em nada. Assim, no código
	 * <p><code>cron.stop();</code>
	 * <p><code>cron.stop();</code>
	 * <p>o segundo <code>stop</code> não faz nada.	 
	 */
	public final void pause(){
		if ( this.running ){
			elapsed += System.currentTimeMillis() - beginning;
			this.running = false;
		}		
	}

	/**
	 * Zera o tempo total cronometrado.
	 */
	public final void reset(){
		beginning = System.currentTimeMillis();
		this.elapsed = 0;
	}
	
	/**
	 * Pára a cronometragem e zera o cronômetro. Equivale a executar
	 * <code>pause()</code> e <code>reset()</code>.
	 */
	public void stop(){
		pause();
		reset();
	}
	
	/**
	 * Fornece o tempo decorrido em milisegundos.
	 * @return o tempo total em estado de cronometragem, ou seja, a soma de todos os tempos entre as execuções dos métodos <code>start</code> e
	 * <code>stop</code> ou <code>read</code>, nesta ordem.
	 */
	public final long read(){
		return( running ? System.currentTimeMillis() - beginning + elapsed : elapsed );
	}

	/**
	 * Indica se o cronômetro está rodando ou não.
	 * @return <code>true</code> caso o cronômetro esteja rodando ou <code>false</code> em caso contrário.
	 */
	public boolean isRunning(){
		return( running );
	}
	
	/**
	 * Imprime o tempo decorrido no formato hh:mm:ss.sss 
	 */
	public String toString() {
		
		long millis = this.elapsed % 1000;
		long secs   = this.elapsed / 1000;
		long mins   = secs / 60;
		long hours  = mins / 60;
		
		return( hours + ":" + mins + ":" + secs + "." + millis );
	}
}

