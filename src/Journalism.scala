import javalib.funworld._
import javalib.worldcanvas._
import javalib.worldimages._
import java.util._
import javalib.colors._
import java.awt._

import scala.collection.immutable.List
import scala.collection.immutable.StringOps
//import scala.collection.mutable.StringBuilder


/*
Scala notes:

'val's are immutable,
'var's are mutable


Game plans:
Use Stanford NLP Sentiment analyzer
*/
object Journalism {
	def main(args: Array[String]): Unit = {
		val width = 768
		val height = 768
		val fontSize = 16
		val offset = 20

		val ss = Toolkit.getDefaultToolkit().getScreenSize()
		val screenWidth = ss.getWidth().toInt
		val screenHeight = ss.getHeight().toInt - offset

		val defaultInputBar = new InputBar(0, screenHeight - fontSize - offset, screenWidth*2, fontSize, "")
		val defaultMessageLog = List[String]()
		val startRoom = new StartRoom()
		val player = new PlayerTree()
		new JournalismGame(defaultInputBar, defaultMessageLog, startRoom, player).start()
	}
}


class JournalismGame(inputBarC: InputBar, messageLogC: List[String], roomC: Room, playerTreeC: PlayerTree) extends World {
	val width = 768
	val height = 768
	val fontSize = 16
	val offset = 20

	val ss = Toolkit.getDefaultToolkit().getScreenSize()
	private val screenWidth = ss.getWidth().toInt
	private val screenHeight = ss.getHeight().toInt - offset


	lazy val inputBar = inputBarC
	lazy val messageLog = messageLogC
	lazy val room = roomC
	lazy val playerTree = playerTreeC

	def start(): Unit = {
		//toInt is parens-less method double->int
		this.bigBang(screenWidth, screenHeight, .5)
	}

	override def makeImage(): WorldImage = {

		return new TextImage(new Posn(5,5), "ey", new Black())
					.overlayImages(inputBar.makeImage())
	}

	override def onKeyEvent(ke: String): World = {
		if (false){//!room.acceptsInput) {
			//ignore input during events
			return this
		} else {
			ke match {
				case "return" => return new JournalismGame(inputBar.onKeyEvent("clear"), messageLog, room, playerTree).processCommand(ke)
				case _ => return new JournalismGame(inputBar.onKeyEvent(ke), messageLog, room, playerTree)
			}
		}
	}

	//handles text adventure commands when return key is pressed
	def processCommand(ke: String): World = {
		return new JournalismGame(inputBar, messageLog, room, playerTree)
	}





	//utility methods
	//def get(screen_width, screen_height)
}

//////////////
class InputBar(xc: Int, yc: Int, widthc: Int, heightc: Int, strC: String) {
	var x = xc
	var y = yc
	val posn = new Posn(x, y)

	var width = widthc
	var height = heightc

	val offset = 3
	var str = strC

	def makeImage(): WorldImage = {
		return new RectangleImage(posn, width, height, new Black())
								.overlayImages(new TextImage(new Posn(x + (offset*str.length).toInt, y), str, new Blue()))
	}

	def onKeyEvent(ke: String): InputBar = {
		//println(ke)
		ke match {
			case "delete" => new InputBar(x, y, width, height, str.dropRight(1))
			case "clear" => new InputBar(x, y, width, height, "")
			case "right" | "left" | "up" | "down" => new InputBar(x, y, width, height, str)
			case n => new InputBar(x, y, width, height, str + n)
		}
	}
}


/////////////////////
class PlayerTree() {

}


/////////////////Different rooms
trait Room {
	def acceptsInput(): Boolean
}

class StartRoom() extends Room {
	val eventRunning = false
	def acceptsInput(): Boolean = {
		return eventRunning
	}
}