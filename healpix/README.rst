====
Healpix - JAVA
====

Create the jar
====

* Download Healpix
* Install ant (brew install ant)
* ant compile dist
* mkdir $YOUR_PROJECT/lib
* cp $HEALPIX_JAVA_DIR/dist/jhealpix.jar $YOUR_PROJECT/lib

et voilà! Notice that we provide the jar in this repo.

Run the example
====

* Install sbt (brew install sbt)
* sbt run

Documentation
====

See http://healpix.sourceforge.net/html/java/index.html
