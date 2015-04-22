JFLAGS = -cp "./javalib-1.0.3.jar:lib" -d bin
JC = scalac
.SUFFIXES: .scala .class
.scala.class:
	$(JC) $(JFLAGS) $*.scala

CLASSES = \
	./src/Journalism.scala \

default: 
	$(JC) ./src/Journalism.scala -cp "./javalib-1.0.3.jar:lib" -d bin

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	$(RM) bin/*.class

run:
	scala -cp bin:javalib-1.0.3.jar Journalism