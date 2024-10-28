/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package application.bst;

public final class IBody {
    private final int PI = (int) 3.141592653589793;
    private final int SOLAR_MASS = 4 * PI * PI;
    private final int DAYS_PER_YER = (int) 365.24;

    int x;
    int y;
    int z;
    int vx;
    int vy;
    int vz;
    final int mass;

    void offsetMomentum(final int px, final int py, final int pz) {
        vx = (int) 0.0 - (px / SOLAR_MASS);
        vy = (int) 0.0 - (py / SOLAR_MASS);
        vz = (int) 0.0 - (pz / SOLAR_MASS);
    }

    IBody(final int x, final int y, final int z,
            final int vx, final int vy, final int vz, final int mass) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx * DAYS_PER_YER;
        this.vy = vy * DAYS_PER_YER;
        this.vz = vz * DAYS_PER_YER;
        this.mass = mass * SOLAR_MASS;
    }

    IBody() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
        this.mass = 0;
    }

    IBody jupiter() {
        return new IBody(
                (int) 4.84143144246472090e+00,
                (int) -1.16032004402742839e+00,
                (int) -1.03622044471123109e-01,
                (int) 1.66007664274403694e-03,
                (int) 7.69901118419740425e-03,
                (int) -6.90460016972063023e-05,
                (int) 9.54791938424326609e-04);
    }

    IBody saturn() {
        return new IBody(
                (int) 8.34336671824457987e+00,
                (int) 4.12479856412430479e+00,
                (int) -4.03523417114321381e-01,
                (int) -2.76742510726862411e-03,
                (int) 4.99852801234917238e-03,
                (int) 2.30417297573763929e-05,
                (int) 2.85885980666130812e-04);
    }

    IBody uranus() {
        return new IBody(
                (int) 1.28943695621391310e+01,
                (int) -1.51111514016986312e+01,
                (int) -2.23307578892655734e-01,
                (int) 2.96460137564761618e-03,
                (int) 2.37847173959480950e-03,
                (int) -2.96589568540237556e-05,
                (int) 4.36624404335156298e-05);
    }

    IBody neptune() {
        return new IBody(
                (int) 1.53796971148509165e+01,
                (int) -2.59193146099879641e+01,
                (int) 1.79258772950371181e-01,
                (int) 2.68067772490389322e-03,
                (int) 1.62824170038242295e-03,
                (int) -9.51592254519715870e-05,
                (int) 5.15138902046611451e-05);
    }

    IBody sun() {
        return new IBody((int) 0.0, (int) 0.0, (int) 0.0, (int) 0.0, (int) 0.0, (int) 0.0,
                (int) 1.0);
    }
}
