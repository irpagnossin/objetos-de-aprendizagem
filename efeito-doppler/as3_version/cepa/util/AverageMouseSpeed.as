package cepa.util {
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.geom.Point;
	
	public class AverageMouseSpeed extends Sprite {
		
		public var vx:DynamicAverage;
		public var vy:DynamicAverage;
		private var pos:Point, lastPos:Point;
		
		//public function AverageMouseSpeed (n:uint, frameRate:int):void {
		public function AverageMouseSpeed (n:uint):void {
			vx = new DynamicAverage(n);
			vy = new DynamicAverage(n);
			
			pos = new Point();
			lastPos = new Point();
			
			this.addEventListener(Event.ENTER_FRAME, onEnterFrame);
		}
		
		public function reset ():void {
			vx.reset();
			vy.reset();
		}
		
		private function onEnterFrame (event:Event):void {
			pos.x = mouseX;
			pos.y = mouseY;
			
			vx.push(pos.x - lastPos.x);
			vy.push(pos.y - lastPos.y);
			
			lastPos.x = pos.x;
			lastPos.y = pos.y;
		}
	}
}