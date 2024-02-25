package cepa.util
{
	import flash.display.MovieClip;
	import flash.events.Event;
	public class DynamicAverageTest extends MovieClip
	{		
		var ms:AverageMouseSpeed;
		
		public function DynamicAverageTest () {
			ms = new AverageMouseSpeed(5,stage.frameRate);
			addEventListener(Event.ENTER_FRAME, onEnterFrame);
		}
		
		private function onEnterFrame (event:Event):void {
			trace(ms.vx.getMean() + "\t" + ms.vy.getMean());
		}
	}
	
}