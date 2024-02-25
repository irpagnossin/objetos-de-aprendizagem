package cepa.edu.mechanics {
	
	import flash.display.MovieClip;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.media.Sound;
	import flash.media.SoundChannel;
	import flash.utils.Timer;
	
	import cepa.util.AverageMouseSpeed;
	
	
	public class ClassicalDopplerEffect extends MovieClip {
		
		private const d:Number = 2; // Distância em pixels que define a colisão da frente de onda com o "detector"
		
		private var n:uint = 0; // A quantidade inicial de elementos da cena (antes de inserir as frentes de onda)
		private var speed:Number = 50; // Velocidade da onda em pixels por segundo
		private var period:int = 1000; // Período da onda em milissegundos
		private var clickOffset:Point = null; // Distância entre o ponto de clique do mouse e o de referência do objeto
		private var bleep:Sound = new Bleep(); // O som (bleep) tocado quando o "detector" colide com uma frente de onda
		private var mouseSpeed:AverageMouseSpeed; // Mede a velocidade média do mouse
		private var vx_source:Number = 0, vy_source:Number = 0; // Velocidade da "source" (pixels por quadro)
		private var vx_detector:Number = 0, vy_detector:Number = 0; // Velocidade do "detector" (pixels por quadro)
		private var draggingSource:Boolean = false; // Indica se a "source" está sendo arrastada
		private var draggingDetector:Boolean = false; // Indica se o "detector" está sendo arrastado
		
		public function ClassicalDopplerEffect () {

			// Mede a velocidade média do mouse.
			mouseSpeed = new AverageMouseSpeed(10);

			// O cronômetro responsável por marcar as emissões de frente de onda.
			var timer:Timer = new Timer(period);

			// Observadores de eventos.
			timer.addEventListener(TimerEvent.TIMER, onTimeComplete);
			stage.addEventListener(Event.ENTER_FRAME, onEnterFrame);
			stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
			source.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			source.addEventListener(Event.ENTER_FRAME, onDraggingSource);
			detector.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			detector.addEventListener(Event.ENTER_FRAME, onDraggingSource);

			// Pára a animação do "detector" (objeto Detector, no .fla).
			detector.gotoAndStop(1);

			// A quantidade de filhos antes de adicionar objetos WaveFront2D.
			n = this.numChildren;

			// Insere "nwave" frentes de onda na cena, de modo a preenchê-la desde o início.
			var nwave:uint = Math.ceil(Math.sqrt(Math.pow(this.stage.stageWidth, 2) + Math.pow(this.stage.stageWidth, 2))/(speed*period/1000));
			for (var i:uint = 0; i < nwave; i++) {
				var wave:WaveFront2D = new WaveFront2D(speed, speed*period*i/1000);
				wave.x = source.x;
				wave.y = source.y;
				this.addChildAt(wave, 1);	
			}
			
			// Inicia o cronômetro que marca a emissão das frentes de onda.
			timer.start();
		}
		
		// Cria uma nova frente de onda a cada intervalo de tempo igual a "period".
		private function onTimeComplete(event:TimerEvent) {
			var wave:WaveFront2D = new WaveFront2D(speed,0);
			wave.x = source.x;
			wave.y = source.y;
			this.addChildAt(wave, 1);			
		}
		
		// Atualiza a cena.
		private function onEnterFrame (event:Event):void {
			
			// Pára a animação do "detector" quando chegar ao último quadro (do objeto Detector, no .fla).
			if (detector.currentFrame == detector.totalFrames) detector.gotoAndStop(1);
			
			for (var i:uint = 1; i < this.numChildren - n + 1; i++) {
				var wave:WaveFront2D = this.getChildAt(i) as WaveFront2D;
				
				if (wave != null) {
					
					// Anima o detector quando cruzar uma frente de onda.
					var pA:Point = new Point(wave.x, wave.y);
					var pB:Point = new Point(detector.x, detector.y);
					
					if (Math.abs(Point.distance(pA, pB) - wave.r) < d) {
						if (!wave.bleep) {
							detector.play();
							playSound(bleep);
							wave.bleep = true;
						}
					}
					else wave.bleep = false;
					
					// Remove a frente de onda quando ela sair da cena.
					if (wave.hitTestPoint(0, 0, true) && wave.hitTestPoint(this.stage.stageWidth, 0, true) && wave.hitTestPoint(0, this.stage.stageHeight, true) && wave.hitTestPoint(this.stage.stageWidth, this.stage.stageHeight, true)) this.removeChild(wave);
				}
			}
			
			// Mantém a "source" dentro da cena.
			if (!draggingSource) {
				source.x += vx_source;
				source.y += vy_source;
				
				if (source.x < source.width / 2) {
					source.x = source.width / 2;
					vx_source = vy_source = 0;
				}
				else if (source.x > stage.stageWidth - source.width / 2) {
					source.x = stage.stageWidth - source.width / 2;
					vx_source = vy_source = 0;
				}
				
				if (source.y < source.height / 2) {
					source.y = source.height / 2;
					vx_source = vy_source = 0;
				}
				else if (source.y > stage.stageHeight - source.height / 2) {
					source.y = stage.stageHeight - source.height / 2;
					vx_source = vy_source = 0;
				}
			}
			
			// Mantém o "detector" dentro da cena.
			if (!draggingDetector) {
				detector.x += vx_detector;
				detector.y += vy_detector;
				
				if (detector.x < detector.width / 2) {
					detector.x = detector.width / 2;
					vx_detector = vy_detector = 0;
				}
				else if (detector.x > stage.stageWidth - detector.width / 2) {
					detector.x = stage.stageWidth - detector.width / 2;
					vx_detector = vy_detector = 0;
				}
				
				if (detector.y < detector.height / 2) {
					detector.y = detector.height / 2;
					vx_detector = vy_detector = 0;
				}
				else if (detector.y > stage.stageHeight - detector.height / 2) {
					detector.y = stage.stageHeight - detector.height / 2;
					vx_detector = vy_detector = 0;
				}
			}
		}
		
		// Marca o início do arraste da "source" ou do "detector".
		private function onMouseDown (event:MouseEvent):void {
			if (event.currentTarget == source) draggingSource = true;
			else if (event.currentTarget == detector) draggingDetector = true;
			
			clickOffset = new Point(event.localX, event.localY);
			mouseSpeed.reset();
		}

		// Atualiza a posição da "source" ou do "detector" durante o arraste.
		private function onDraggingSource (event:Event):void {
			if (draggingSource) {
				source.x = mouseX - clickOffset.x;
				source.y = mouseY - clickOffset.y;
			}
			else if (draggingDetector) {
				detector.x = mouseX - clickOffset.x;
				detector.y = mouseY - clickOffset.y;
			}
		}

		// Marca o fim do arraste da "source" ou do "detector"
		private function onMouseUp (event:MouseEvent):void {
			clickOffset = null;
			
			if (draggingSource) {
				vx_source = mouseSpeed.vx.getMean();
				vy_source = mouseSpeed.vy.getMean();
			}
			else if (draggingDetector) {
				vx_detector = mouseSpeed.vx.getMean();
				vy_detector = mouseSpeed.vy.getMean();
			}
			
			draggingSource = draggingDetector = false;
		}
		
		// Toca o som passado como argumento.
		public function playSound (sound:Object):void {
			var channel:SoundChannel = sound.play();
		}
	}
}