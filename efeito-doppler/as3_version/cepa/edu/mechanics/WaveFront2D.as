package cepa.edu.mechanics {
	import flash.display.MovieClip;
	import flash.events.Event;
	import flash.utils.getTimer;
	
	public class WaveFront2D extends MovieClip {
		
		public var r:Number = 0;
		private var speed:Number = 0;
		private var t0:int = 0;
		private var t:Number = 0;
		public var bleep:Boolean = false;
		
		public function WaveFront2D (speed:Number, r0:Number) {			
			addEventListener(Event.ENTER_FRAME, onEnterFrame);
			this.speed = speed;
			this.r = r0;
			t0 = getTimer();
		}
		
		private function onEnterFrame (event:Event):void {
			t = getTimer();			
			r += speed * (t - t0) / 1000;
			t0 = t;
			
			graphics.clear();
			graphics.beginFill(0xFFFFFF,0);
			graphics.lineStyle(1.2, 0x22333333);
			graphics.drawCircle(0, 0, r);
			graphics.endFill();
		}
	}
}