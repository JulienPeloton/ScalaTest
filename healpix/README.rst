====
Healpix - JAVA
====

(under construction)

Create the jar
====

Download `Healpix <https://sourceforge.net/projects/healpix/>`_ and install ant. Then run

::

  $ HEALPIX_JAVA_DIR=$HEALPIX_DIR/src/java
  $ cd $HEALPIX_JAVA_DIR; ant compile dist
  $ mkdir $YOUR_PROJECT/lib
  $ cp $HEALPIX_JAVA_DIR/dist/jhealpix.jar $YOUR_PROJECT/lib

et voilà! Notice that we also provide directly the jar in this repo.

Run the example
====

Install sbt and run

::

  $ sbt run

Documentation
====

See `here <http://healpix.sourceforge.net/html/java/index.html>`_
