/*
  arquivo: Cronometer.java
  autor: Ivan Ramos Pagnossin
  data: 2006.01.20
  copyright: viavoip
 */
package cepa.edu.util;

/**
 * A classe Cronometer encapsula um cron�metro. Um exemplo de uso �:
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
	private boolean running;	// Estado do cron�metro: cronometrando ou n�o.

	/**
	 * Cria um cron�metro.
	 */
	public Cronometer(){
	  	this.elapsed = 0;
    	this.running = false;
	}

	/**
	 * Inicia a cronometragem.
	 * <p>obs.: iniciar uma cronometragem j� iniciada n�o incorre em nada. Assim, no c�digo
	 * <p><code>cron.start();</code>
	 * <p><code>cron.start();</code>
	 * <p>o segundo <code>start</code> n�o faz nada.
	 */
	public final void start(){
		if ( !this.running ){
			beginning = System.currentTimeMillis();
			this.running = true;
		}		
	}

	/**
	 * Encerra a cronometragem.
	 * <p>obs.: encerrar uma cronometragem j� encerrada n�o incorre em nada. Assim, no c�digo
	 * <p><code>cron.stop();</code>
	 * <p><code>cron.stop();</code>
	 * <p>o segundo <code>stop</code> n�o faz nada.	 
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
	 * P�ra a cronometragem e zera o cron�metro. Equivale a executar
	 * <code>pause()</code> e <code>reset()</code>.
	 */
	public void stop(){
		pause();
		reset();
	}
	
	/**
	 * Fornece o tempo decorrido em milisegundos.
	 * @return o tempo total em estado de cronometragem, ou seja, a soma de todos os tempos entre as execu��es dos m�todos <code>start</code> e
	 * <code>stop</code> ou <code>read</code>, nesta ordem.
	 */
	public final long read(){
		return( running ? System.currentTimeMillis() - beginning + elapsed : elapsed );
	}

	/**
	 * Indica se o cron�metro est� rodando ou n�o.
	 * @return <code>true</code> caso o cron�metro esteja rodando ou <code>false</code> em caso contr�rio.
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

