import healpix.essentials.HealpixBase
import healpix.essentials.Pointing
import healpix.essentials.Scheme.RING

object HelloPixelWorld extends App {
  // Simple object to retrieve pixel number
  // given (theta, phi) position of a point.

  // Instanciate our HealpixBase class
  val nside: Int = 512
  val hp = new HealpixBase(nside, RING)

  // Define our point on the sky
  val theta = Math.toRadians(80)
  val phi = Math.toRadians(45)
  val p = new Pointing(theta, phi)

  // query for pixel number
  val ipix2 = hp.ang2pix(p)

  // Python check!
  // In [3]: hp.ang2pix(512, 80*np.pi/180., 45*np.pi/180.)
  // Out[3]: 1299712
  println("Pixel number (nside=512): " + ipix2.toString)
}
