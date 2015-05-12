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
					.overlayImages(makeMessageLogImage(messageLog), inputBar.makeImage())
	}

	override def onKeyEvent(ke: String): World = {
		if (false){//!room.acceptsInput) {
			//ignore input during events
			return this
		} else {
			ke match {
				case "/" => return new JournalismGame(inputBar.onKeyEvent("clear"), messageLog, room, playerTree).processCommand(ke)
				case _ => return new JournalismGame(inputBar.onKeyEvent(ke), messageLog, room, playerTree)
			}
		}
	}

	//handles text adventure commands when return key is pressed
	def processCommand(ke: String): World = {
		return new JournalismGame(inputBar, messageLog :+ inputBar.str, room, playerTree)
	}

	def makeMessageLogImage(l: List[String], counter: Int = 1): WorldImage = {
		if (l.length == 0)
			return new TextImage(new Posn(0,0), "", new White())
		else
			return new TextImage(new Posn(50, 50)//offset, offset + counter*fontSize)
				, l.head, new Black())
							.overlayImages(makeMessageLogImage(l.drop(1), counter + 1))
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

	val offset = 3.2
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

///////////////
/*trait Message {
	def makeMessageImage(x: Int, y: Int): WorldImage
	def getNumLines(): Int
}

class TextMessage(strC: String) extends Message = {
	val str = strC
	val charsPerLine = 80
	val fontSize = 16
	def makeMessageImage(x: Int, y: Int, lineIncrement: Int = 18): WorldImage = {
		var strin = str
		var strL = new List[String]()
		while (strin.length() > 0) {
			if (strin.length() < 80) {
				strL :+ strin
				strin = ""
			} else {
				strL :+ strin.substring(0, 80)
				strin = strin.substring(80)
			}
		}
		img = new TextImage(x, y - (getNumLines() * lineIncrement), strL.head)
		var lineCounter = 1
		for (var x <- strL) {
			img = img.overlayImages(new TextImage(x, y - (getNumLines())))
		}
	}
	def getNumLines(): Int = {
		return str.length()/charsPerLine
	}
}
*/
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