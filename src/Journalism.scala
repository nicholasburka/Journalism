import javalib.funworld._
import javalib.worldcanvas._
import javalib.worldimages._
import java.util._
import javalib.colors._
import java.awt._


object Journalism extends World {
	final var width = 768
	final var height = 768

	var ss = Toolkit.getDefaultToolkit().getScreenSize()
	private var screen_width = ss.getWidth()
	private var screen_height = ss.getHeight()

	def main(args: Array[String]): Unit = {
		//val game = new Journalism() 
		this.bigBang(screen_width.toInt, screen_height.toInt, 100)
	}

	def makeImage(): WorldImage = {
		return new TextImage(new Posn(5,5), "ey", new Black())
	}
}