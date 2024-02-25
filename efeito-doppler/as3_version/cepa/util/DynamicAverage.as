package cepa.util {
	public class DynamicAverage {

		private var mean:Number = 0;	// Média.
		private var N:uint = 5;			// Quantidade de itens inseridos.
		private var numbers:Array;
		
		public function DynamicAverage (N:uint):void {
			this.N = N;			
			reset();
		}
		
		// Acrescenta um valor à média
		public function push (item:Number):void {
			
			if (numbers.length > N) numbers.shift();
			numbers.push(item);
			
			mean = 0;
			for (var i:uint = 0; i < N; i++) mean += numbers[i];
			mean /= N;
		}
		
		// Obtém a média.
		public function getMean ():Number {
			return mean;
		}
		
		// Retorna a média para o valor inicial: zero.
		public function reset ():void {
			mean = 0;
			numbers = new Array();
			for (var i:uint = 0; i < N; i++) numbers.push(0);
		}		
	}
}