import javalib.funworld._
import javalib.worldcanvas._
import javalib.worldimages._
import java.util._
import javalib.colors._
import java.awt._

/*
desired features:
- make objects drop into view on start
- music

*/
////////////////////////////////////////////////////
//singleton object that starts the game
object Game {
	def main(args: Array[String]): Unit = {
		//val defaultInputBar = new InputBar(0, screenHeight - fontSize - offset, screenWidth*2, fontSize, "")

		//new Game(new Player(0, height - 128 - defaultInputBar.height), defaultInputBar).start()
		new TitleScreen(false, 0).start()
	}
}

///////////////////////////////////////////////
//superclass stores some useful constants
abstract class GameTemplate() extends World {
	val ss = Toolkit.getDefaultToolkit().getScreenSize
	val width = ss.getWidth().toInt
	val offset = 50
	val fontSize = 16
	val height = ss.getHeight().toInt - offset
}

/////////////////////////////////////////////////////////
//different subclasses represent different stages/levels
class TitleScreen(kep: Boolean, count: Int) extends GameTemplate {
	var keypressed = kep
	val counter = count
	val max = 10

	def start(): Unit = {
		this.bigBang(width, height, 1.0/30)
	}

	override def makeImage(): WorldImage = {
		if (counter < 10) {
			return Background.blackBack.overlayImages(new FromFileImage( new Posn(width/2.0.toInt, height/2.0.toInt), "img/title_screen.png"))
		} else {
			return Background.blackBack.overlayImages(new FromFileImage( new Posn(width/2.0.toInt, height/2.0.toInt), "img/title_screen.png"),
														new TextImage (new Posn(width/2.0.toInt, height/2.0.toInt + offset), "Press any key to begin.", new White()))
		}
	}

	override def onTick(): World = {
		if (keypressed) {
			// default instatiations of beginning of game
			val player = new Player(50, height - 128 - 16, speechC = new Speech("hey"))
			val partner = new Partner(width - 200, height - 128 - 16)
			return new Intro(player, partner)
		}
		else return new TitleScreen(false, counter + 1)
	}

	override def onKeyEvent(ke: String): World = {
		return new TitleScreen(true, counter)
	}

}

class Intro(playerC: Player, partnerC: Partner, backgroundC: Array[FileImage] = new Array[FileImage](6)) extends GameTemplate {

	val player = playerC
	val partner = partnerC
	var background = backgroundC

	override def makeImage(): WorldImage = {
		return Background.blackBack.overlayImages(player.makeImage())
	}

	override def onTick(): World = {
		/*if (introComplete) {
			return new Journal()
		} else {

		}*/
		return new Intro(player.onTick(), partner.onTick())
	}

	def state(): Int = {
		//if (counter < 30) {

		//}
		return 1
	}
}

/*class Game(playerC: Player, inputBarC: InputBar, entitiesC: Array[Entity]) extends World {
	val ss = Toolkit.getDefaultToolkit().getScreenSize
	val width = ss.getWidth().toInt
	val offset = 50
	val height = ss.getHeight().toInt - offset

	val player = playerC
	val inputBar = inputBarC
	val commandHandler = new CommandHandler()
	val entities = entitiesC

	def start(): Unit = {
		this.bigBang(width, height, .1)
	}

	override def makeImage(): WorldImage = {
		return player.makeImage().overlayImages(inputBar.makeImage(), makeEntiiesImage())
	}

	override def onKeyEvent(ke: String): World = {
		if (ke.equals("/")) {
			return new Game(handleCommand(inputBar.str), inputBar.onKeyEvent(ke))
		} else {
			return new Game(player.onKeyEvent(ke), inputBar.onKeyEvent(ke))
		}
	}

	def handleCommand(com: String): Player = {
		if (isPositive(com)) {

		} else {

		}
	}
}*/

////////////////////////////////////////////////////////
//Different classes that represent various game objects: characters, popups, etc.
class Person(xC: Int, yC: Int, imgPathC: String, speechC: Speech = new Speech()) {
	val imgWidth = 128
	val imgHeight = 128
	val imgPath = imgPathC
	val x = xC
	val y = yC
	val movementSpeed = 10
	val speech = speechC

	def makeImage(): WorldImage = {
		return new FromFileImage(new Posn(x + (.5*imgWidth).toInt, y + (.5*imgHeight).toInt), imgPath)
		                        .overlayImages(speech.makeImage(new Posn(x + imgWidth - 50, y - imgHeight + 20)))
	}
	def onTick(): Person = {
		return new Person(x, y, imgPath, speech.onTick())
	}
	def moveLeft(): Person = {
		return new Person(x - movementSpeed, y, imgPath, speech)
	}
	def moveRight(): Person = {
		return new Person(x + movementSpeed, y, imgPath, speech)
	}
	def say(str: String): Person = {
		return new Person(x, y, imgPath, new Speech(str))
	}
}

class Player(xC: Int, yC: Int, imgPathC: String = "img/player_alone_white.png", speechC: Speech = new Speech()) extends Person(xC, yC, imgPathC, speechC) {
	override def onTick(): Player = {
		return new Player(x, y, imgPath, speech.onTick())
	}
	override def moveLeft(): Player = {
		return new Player(x - movementSpeed, y, imgPath, speech)
	}
	override def moveRight(): Player = {
		return new Player(x + movementSpeed, y, imgPath, speech)
	}
	override def say(str: String): Player = {
		return new Player(x, y, imgPath, new Speech(str))
	}
}

class Partner(xC: Int, yC: Int, imgPathC: String = "img/player_alone_white.png", speechC: Speech = new Speech()) extends Person(xC, yC, imgPathC, speechC) {
	override def onTick(): Partner = {
		return new Partner(x, y, imgPath, speech.onTick())
	}
	override def moveLeft(): Partner = {
		return new Partner(x - movementSpeed, y, imgPath, speech)
	}
	override def moveRight(): Partner = {
		return new Partner(x + movementSpeed, y, imgPath, speech)
	}
	override def say(str: String): Partner = {
		return new Partner(x, y, imgPath, new Speech(str))
	}
}

class Speech(strC: String = "", counterC: Int = 0) {
	//number of ticks for speech bubble to exist
	val SPEECH_TIME = 90
	val str = strC
	//should be initialized to zero when new speech is made
	val counter = counterC
	val imgWidth = 128
	val imgHeight = 128
	val imgPath = "img/speech_bubble.png"

	def makeImage(parentPosHandle: Posn): WorldImage = {
		if (counter < SPEECH_TIME && !str.equals("")) {
			//parentPosHandle should be upper right corner of parent
			val x = parentPosHandle.x + (.5*imgWidth).toInt
			val y = parentPosHandle.y + (.5*imgHeight).toInt
			return new FromFileImage(new Posn(x, y), imgPath)
								.overlayImages(new TextImage(new Posn(x, y), str, new Blue()))
		} else {
			return new TextImage(new Posn(0, 0), "", new White())
		}
	}

	def onTick(): Speech = {
		return new Speech(str, counter + 1)
	}

	def setSpeech(strC: String): Speech = {
		return new Speech(strC, counter)
	}

}

/*class PopUpImage(posC: Posn, imgC: WorldImage, counterC: Int, maxCountC: Int) {

}*/

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
								.overlayImages(new TextImage(new Posn(x + (offset*str.length).toInt, y), str, new White()))
	}

	def onKeyEvent(ke: String): InputBar = {
		//println(ke)
		ke match {
			case "delete" => new InputBar(x, y, width, height, str.dropRight(1))
			case "/" => new InputBar(x, y, width, height, "")
			case "right" | "left" | "up" | "down" => return this
			case n => new InputBar(x, y, width, height, str + n)
		}
	}
}

/*class CommandHandler() {
	def handleCommand(str: String): Player = {
		val parseSentiment = parse(str)
		parseSentiment match {
			case -1 => 
			case 0 =>
			case 1 =>
			case _ => throw new RuneTimeException("woops")
		}
	}
}
*/

//basic utility class for background images
class FileImage(posC: Posn, imagePathC: String) {
	val pos = posC
	val imagePath = imagePathC
	val movementSpeed = 1
	def makeImage(): WorldImage = {
		return new FromFileImage(pos, imagePath)
	}
	def setPos(posC: Posn): FileImage = {
		return new FileImage(posC, imagePath)
	}
	def setImagePath(path: String): FileImage = {
		return new FileImage(pos, path)
	}
	def moveLeft(): FileImage = {
		return new FileImage(new Posn(pos.x - movementSpeed, pos.y), imagePath)
	}
}

//i prefer black to white
object Background {
	val ss = Toolkit.getDefaultToolkit().getScreenSize
	val width = ss.getWidth().toInt
	val offset = 50
	val height = ss.getHeight().toInt - offset
	val blackBack = new RectangleImage(new Posn((width/2.0).toInt, (height/2.0).toInt), width, height, new Black())
}

////////////////////////////////////////////////////////
//ROOMS ARE SINGLETON OBJECTS THAT HAVE AN ARRAY OF FILEIMAGES

object Bedroom {
	var images = new Array[FileImage](6)
	def makeImage(): WorldImage = {
		var image = images(0).makeImage()
		for (i <- 1 to images.length) {

		}
		return image
	}
	def moveLeft() = {
		var counter = 0
		for (i <- images) {
			images(counter) = i.moveLeft()
		}
	}
}