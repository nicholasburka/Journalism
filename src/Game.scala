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
object Game extends GameInfo {
	var loveCount = 0
	def main(args: Array[String]): Unit = {
		//val defaultInputBar = new InputBar(0, screenHeight - fontSize - offset, screenWidth*2, fontSize, "")
		val test = Tests

		new Intro(new Player(new Posn(50, height - 64 - 8)), IntroPartner).start()
		//new Ending().start()
	}
}

//singleton object that tests the game
object Tests {
	println("Testing...")
	var inputBar = new InputBar()
	inputBar = inputBar.onKeyEvent("s")
	assert(inputBar.str.equals("s"))
	inputBar = inputBar.onKeyEvent("t")
	assert(inputBar.str.equals("st"))
	assert(inputBar.onKeyEvent("\\").str.equals("s"))
	assert(inputBar.onKeyEvent("/").str.equals(""))

	var player = new Player(new Posn(0,0)).say("hey")
	assert(player.speech.str.equals("hey"))

	//only need to test movement with player because other classes are identical
	assert(player.moveRight().pos.x == player.movementSpeed)
	assert(player.moveLeft().pos.x == -1 * player.movementSpeed)



	var counterDefault = new Counter()
	val counterDefaultMax = counterDefault.max
	for (i <- 0 to counterDefaultMax) {
		player = player.onTick()
	}
	assert(player.speech.counter.expired())
	println("All tests passed.")
}

///////////////////////////////////////////////
//superclasses store some useful constants
class GameInfo() {
	val ss = Toolkit.getDefaultToolkit().getScreenSize
	val width = ss.getWidth().toInt
	val offset = 50
	val fontSize = 16
	val height = ss.getHeight().toInt - offset
}
abstract class GameTemplate() extends World {
	val ss = Toolkit.getDefaultToolkit().getScreenSize
	val width = ss.getWidth().toInt
	val offset = 50
	val fontSize = 16
	val height = ss.getHeight().toInt - offset

	def start(): Unit = {
		this.bigBang(width, height, 1.0/300)
	}
	def isPositive(str: String): Boolean = {
		return ("yesyeahyemhmokyersurefine<3loveheartbabybabehoneydear".indexOf(str.toLowerCase()) != -1)
	}
}

/////////////////////////////////////////////////////////
//different subclasses represent different stages/levels
class TitleScreen(kep: Boolean = false, count: Int = 0) extends GameTemplate {
	var keypressed = kep
	val counter = count
	val max = 10
	val END_TIME = 150

	override def makeImage(): WorldImage = {
		if (counter < END_TIME/2) {
			return Background.blackBack.overlayImages(new FromFileImage( new Posn(width/2.0.toInt, height/2.0.toInt), "img/title_screen.png"))
		} else {
			return Background.blackBack.overlayImages(new FromFileImage( new Posn(width/2.0.toInt, height/2.0.toInt), "img/title_screen.png"),
														new TextImage (new Posn(width/2.0.toInt, height/2.0.toInt + offset), "by Nick Burka", new White()))
		}
	}

	override def onTick(): World = {
		if (counter == END_TIME) {
			// default instatiations of beginning of game
			val player = new Player(new Posn(50, height - 64 - 8))
			//val partner = new Partner(width - 200, height - 16)
			//return new Intro(player, IntroPartner)
			return new Ch1()
		}
		else return new TitleScreen(false, counter + 1)
	}

	override def onKeyEvent(ke: String): World = {
		return new TitleScreen(true, counter)
	}

}

class Intro(playerC: Player, partnerC: IntroPartner.type, backgroundC: Array[FileImage] = new Array[FileImage](6), countC: Counter = new Counter(maxC = 500), inputBarC: InputBar = new InputBar(), popupC: Speech = new Speech()) extends GameTemplate {

	val player = playerC
	val partner = partnerC
	var background = backgroundC
	var room = Bedroom
	val counter = countC
	var spoken = false
	var inputBar = inputBarC
	var popup = popupC


	override def makeImage(): WorldImage = {
		if (counter.expired()) {
			return Background.blackBack.overlayImages(room.makeImage(), player.makeImage(), partner.makeImage(), inputBar.makeImage(), popup.makeImage(new Posn(256, 500)))
		} else
		return Background.blackBack.overlayImages(room.makeImage(), player.makeImage(), partner.makeImage())
	}

	override def onTick(): World = {
		/*if (introComplete) {
			return new Journal()
		} else {

		}*/
		if (counter.count == 501) {
			popup = new Speech("Type and hit \"/\" to speak.\n Hit \"\\\" to delete.", new Counter(maxC = 300), "img/popup_background.png")
			assert(popup.imgPath.equals("img/popup_background.png"))
		}
		if (IntroPartner.heard && IntroPartner.heardTime + 500 < counter.count) {
			popup = new Speech(" ", new Counter(), "img/arrow_right.png")
		}
		if (player.pos.x > width - 64) {
			//return new Ch1()
			return new TitleScreen()
		} else {
			return new Intro(player.onTick(), partner.onTick(), countC = counter.onTick(), inputBarC = inputBar, popupC = popup.onTick())
		}
	}

	override def onKeyEvent(ke: String): World = {
		if (counter.expired()) {
			if (ke.equals("/")) {
				IntroPartner.hear()
				return new Intro(player.say(inputBar.str), partner, background, counter, inputBar.onKeyEvent(ke), popup)
			}
			if (ke.equals("right")) {
				IntroPartner.playerMove()
				return new Intro(player.moveRight(), partner, background, counter, inputBar.onKeyEvent(ke), popup)
			}
			inputBar = inputBar.onKeyEvent(ke)
		}
		return this
	}

}

class Ch1(playerC: Player = new Player(new Posn(128, 0)), partnerC: Partner = new Partner(new Posn(0, 0)).say("..."), sittingC: Boolean = false, countC: Int = 0, inputBarC:InputBar = new InputBar()) extends GameTemplate {
	var player = playerC
	val partner = partnerC
	val back = Bench
	val sitting = sittingC
	val counter = countC
	val inputBar = inputBarC
	if (playerC.pos.y == 0) {
		player = new Player(new Posn(128, height - 72), "img/player_normal_white.png")
	}

	override def makeImage: WorldImage = {
		if (sitting) {
			return Background.blackBack.overlayImages(back.makeImage(), player.makeImage(), partner.makeImage(), inputBar.makeImage())
		} else {
			return Background.blackBack.overlayImages(back.makeImage(), player.makeImage())
		}
	}

	override def onTick(): World = {
		counter match {
			case 180 => return new Ch1(player, partner.say("I'm leaving soon..."), sitting, counter + 1, inputBar)
			case 300 => return new Ch1point5(new Player(new Posn(player.pos.x - 32, back.images(0).pos.y), "img/player_alone_white.png"), new Partner(new Posn(player.pos.x + 32, back.images(0).pos.y), "img/player_alone_white.png").say("Walk me home?"), inputBar)
			case _ =>
		}
		if (sitting) {
			return new Ch1(player, partner, sitting, counter + 1, inputBar)
		}
		return new Ch1(player, partner, sitting, counter, inputBar)
	}



	override def onKeyEvent(ke: String): World = {
		if (ke.equals("right")) {
			if (player.pos.x + 50 > back.images(0).pos.x) {
				return new Ch1(new Player(new Posn(player.pos.x, back.images(0).pos.y - 6), "img/player_sitting.png"), new Partner(new Posn(player.pos.x + 64, back.images(0).pos.y - 6), "img/partner_sitting.png"), true, counter, inputBar)
			} else if (player.pos.x < width/2) {
				return new Ch1(player.moveRight(), partner, sitting, counter, inputBar)
			} else {
				back.moveLeft()
			}
		}
		if (sitting) {
			if (ke.equals("/")) {
				return new Ch1(player.say(inputBar.str), partner, sitting, counter, inputBar.onKeyEvent(ke))
			} else
			return new Ch1(player, partner, sitting, counter, inputBar.onKeyEvent(ke))
		}
		return this
	}
}

class Ch1point5(playerC: Player, partnerC: Partner, inputBarC:InputBar) extends GameTemplate {
	var player = playerC
	val partner = partnerC
	val back = Bench
	val inputBar = inputBarC
	override def makeImage(): WorldImage = {
		return Background.blackBack.overlayImages(back.makeImage(), player.makeImage(), partner.makeImage())
	}

	override def onKeyEvent(ke: String): World = {
		if (ke.equals("/")) {
			Journal.entries(0) = inputBar.str
			if (isPositive(inputBar.str)) {
				Game.loveCount = Game.loveCount + 1
			}
			return new Ch1point5(player.say(inputBar.str), partner.say("<3"), inputBar.onKeyEvent(ke))
		} else if (ke.equals("right")) {
			if (player.pos.x > width - 64) {
				return new Ending(inputBarC = inputBar)
			} else return new Ch1point5(player.moveRight(), partner.moveRight(), inputBar)
		} else if (ke.equals("left")) {
			return new Ch1point5(player.moveLeft(), partner.say("ok"), inputBar)
		}
		return new Ch1point5(player, partner, inputBar.onKeyEvent(ke))
	}

	override def onTick(): World = {
		return this
	}
}

class Ending(playerC: Player = new Player(new Posn(500, 500)), partnerC: Partner = new Partner(new Posn(700, 500)), inputBarC: InputBar, countC: Int = 0) extends GameTemplate {
	var player = playerC
	var partner = partnerC
	val back = Picnic
	val inputBar = inputBarC
	val count = countC
	override def makeImage(): WorldImage = {
		return Background.blackBack.overlayImages(back.makeImage(), player.makeImage(), partner.makeImage())
	}

	override def onTick(): World = {
		if (count > 60) {
			return new Ending(player, partner.say(getResponse(Game.loveCount)), inputBar, count + 1)
		} else if (count > 150) {
			return new Ending(player, partner.moveRight(), inputBar, count + 1)
		} else return new Ending(player, partner, inputBar, count + 1)
	}

	def getResponse(i: Int): String = {
		i match {
			case 2 => return "I'll miss you"
			case 1 => return "Bye, dear"
			case _ => return ""
		}
	}
}


////////////////////////////////////////////////////////
//Different classes that represent various game objects: characters, popups, etc.
class Person(posC: Posn, imgPathC: String, speechC: Speech = new Speech()) extends GameInfo {
	val imgWidth = 128
	val imgHeight = 128
	val imgPath = imgPathC
	val pos = posC
	val movementSpeed = 10
	val speech = speechC

	def makeImage(): WorldImage = {
		return new FromFileImage(pos, imgPath)
		                        .overlayImages(speech.makeImage(new Posn(pos.x + imgWidth/2, pos.y - imgHeight/2)))
	}
	def onTick(): Person = {
		return new Person(pos, imgPath, speech.onTick())
	}
	def moveLeft(): Person = {
		return new Person(new Posn(pos.x - movementSpeed, pos.y), imgPath, speech)
	}
	def moveRight(): Person = {
		return new Person(new Posn(pos.x + movementSpeed, pos.y), imgPath, speech)
	}
	def say(str: String): Person = {
		return new Person(pos, imgPath, new Speech(str))
	}
}

class Player(posC: Posn, imgPathC: String = "img/player_alone_white.png", speechC: Speech = new Speech()) extends Person(posC, imgPathC, speechC) {
	override def onTick(): Player = {
		return new Player(pos, imgPath, speech.onTick())
	}
	override def moveLeft(): Player = {
		return new Player(new Posn(pos.x - movementSpeed, pos.y), imgPath, speech)
	}
	override def moveRight(): Player = {
		return new Player(new Posn(pos.x + movementSpeed, pos.y), imgPath, speech)
	}
	override def say(str: String): Player = {
		return new Player(pos, imgPath, new Speech(str))
	}
}

class Partner(posC: Posn, imgPathC: String = "img/player_alone_white.png", speechC: Speech = new Speech()) extends Person(posC, imgPathC, speechC) {
	override def onTick(): Partner = {
		return new Partner(pos, imgPath, speech.onTick())
	}
	override def moveLeft(): Partner = {
		return new Partner(new Posn(pos.x - movementSpeed, pos.y), imgPath, speech)
	}
	override def moveRight(): Partner = {
		return new Partner(new Posn(pos.x + movementSpeed, pos.y), imgPath, speech)
	}
	override def say(str: String): Partner = {
		return new Partner(pos, imgPath, new Speech(str))
	}
}

class Counter(startC: Int = 0, maxC: Int = 90) {
	var count = startC
	val max = maxC
	def onTick(): Counter = {
		count = count + 1
		return this
	}
	def expired(): Boolean = {
		return count >= max
	}
}

class Speech(strC: String = "", counterC: Counter = new Counter(), baseImagePathC: String = "img/speech_bubble.png") {
	val str = strC
	val counter = counterC
	val imgWidth = 128
	val imgHeight = 128
	val imgPath = baseImagePathC
	val RESERVED = "?<3"

	def makeImage(parentPosHandle: Posn): WorldImage = {
		val x = parentPosHandle.x + (.75*imgWidth).toInt
		val y = parentPosHandle.y - (.75*imgHeight).toInt
		val baseImage = new FromFileImage(new Posn(x, y), imgPath)
		if (!counter.expired() && RESERVED.indexOf(str) == -1) {
			//parentPosHandle should be upper right corner of parent
			return baseImage.overlayImages(new TextImage(new Posn(x, y), str, new Blue()))
		} else if (str.equals("") || counter.expired()) {
			return new TextImage(new Posn(0, 0), "", new White())
		} else {
			str match {
				case "?" => return baseImage.overlayImages(new FromFileImage(new Posn(x, y), "img/question_mark.png"))
				case "<3" => return baseImage.overlayImages(new FromFileImage(new Posn(x, y), "img/heart.png"))
			}
		}
	}

	def onTick(): Speech = {
		return new Speech(str, counter.onTick(), imgPath)
	}

	def setSpeech(strC: String): Speech = {
		return new Speech(strC, counter, imgPath)
	}

}

/*class PopUpImage(posC: Posn, imgC: WorldImage, counterC: Int, maxCountC: Int) {

}*/

class InputBar(strC: String = "") extends GameInfo {
	val pos = new Posn(width/2, height - 9)

	val charOffset = 3.2
	var str = strC

	def makeImage(): WorldImage = {
		return new RectangleImage(pos, width, 18, new White())
								.overlayImages(new TextImage(new Posn(20 + (charOffset*str.length).toInt, pos.y), str, new Black()))
	}

	def onKeyEvent(ke: String): InputBar = {
		//println(ke)
		ke match {
			case "\\" => return new InputBar(str.dropRight(1))
			case "/" => return new InputBar("")
			case "right" | "left" | "up" | "down" => return this
			case n => return new InputBar(str + n)
		}
	}
}


//basic utility class for background images
class FileImage(posC: Posn, imagePathC: String, movementSpeedC: Int = 2) {
	val pos = posC
	val imagePath = imagePathC
	val movementSpeed = movementSpeedC
	def makeImage(): WorldImage = {
		return new FromFileImage(pos, imagePath)
	}
	def setPos(posC: Posn): FileImage = {
		return new FileImage(posC, imagePath, movementSpeed)
	}
	def setImagePath(path: String): FileImage = {
		return new FileImage(pos, path, movementSpeed)
	}
	def moveLeft(): FileImage = {
		return new FileImage(new Posn(pos.x - movementSpeed, pos.y), imagePath, movementSpeed)
	}
	def moveRight(): FileImage = {
		return new FileImage(new Posn(pos.x + movementSpeed, pos.y), imagePath, movementSpeed)
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

object Bedroom extends GameInfo {
	val bed = new FileImage(new Posn(128 + offset, height - 128 - offset), "img/bed_small.png")
	var images = Array[FileImage](bed)
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

object Bench extends GameInfo {
	val bench = new FileImage(new Posn(1024 + offset, height - 64 - offset), "img/bench_white.png", 10)
	var images = Array[FileImage](bench)
	def makeImage(): WorldImage = {
		var image = images(0).makeImage()
		for (i <- 1 to images.length) {
			//image = image.overlayImages(images(i).makeImage())
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

object Picnic extends GameInfo {
	val picnic = new FileImage(new Posn(300, height - 128 - offset), "img/picnic.png")
	val sP = "img/star.png"
	val stars = Array[FileImage](new FileImage(new Posn(56, 100), sP))
	def makeImage(): WorldImage = {
		var image = stars(0).makeImage()
		/*for (i <- 0 to stars.length) {
			image = image.overlayImages(stars(i).makeImage())
		}*/
		return image.overlayImages(stars(0).makeImage())
	}
}

object Journal extends GameInfo {
	var entries = Array[String]("")
	def makeImage(): WorldImage = {
		val imgPath = "img/notebook.png"
		val imgHeight = 18
		var baseImage: WorldImage = new FromFileImage(new Posn(width/2, imgHeight/2), imgPath)
		for (i <- 0 to height/imgHeight) {
			baseImage = baseImage.overlayImages(new FromFileImage(new Posn(width/2, i*imgHeight + imgHeight/2), imgPath))
		}
		return baseImage
	}
}

object IntroPartner extends Partner(new Posn(0, 0)) {
	var counter = 0
	var mutableSpeech = new Speech()
	var img = new FileImage(new Posn(width - 100, height - 64 - 8), "img/player_alone_white.png")
	var heard = false
	var playerMoved = false
	var heardTime = 0
	val eventTimes = Array[Int](80, 240, 400, 100, 280, 400, 490)

	override def onTick(): IntroPartner.type = {
		if (counter < 60 || (counter > 300 && counter < 360)) {
			moveLeft()
		}
		counter match {
			case 80 => say("hey")
			case 240 => say("i missed you")
			case 400 => say("you tired?")
			case _ => 
		}
		if (heard) {
			assert(counter > (eventTimes(2)))
			if (heardTime + eventTimes(6) < counter) {
				if (playerMoved) moveRight()
			} else if (heardTime + eventTimes(5) < counter) say("let's go")
			else if (heardTime + eventTimes(4) < counter) say("<3") 
			else if (heardTime + eventTimes(3) < counter) moveLeft
		}
		mutableSpeech = mutableSpeech.onTick()
		counter = counter + 1
		return this
	}

	override def say(str: String): IntroPartner.type = {
		mutableSpeech = new Speech(str)
		return this
	}
	override def makeImage(): WorldImage = {
		return img.makeImage.overlayImages(mutableSpeech.makeImage(img.pos))
	}
	override def moveLeft(): IntroPartner.type = {
		img = img.moveLeft()
		return this
	}
	override def moveRight(): IntroPartner.type = {
		img = img.moveRight()
		return this
	}
	def hear(): IntroPartner.type = {
		if (!heard) {
			heard = true
			heardTime = counter
		}
		return this
	}
	def playerMove(): IntroPartner.type = {
		playerMoved = true
		return this
	}
}