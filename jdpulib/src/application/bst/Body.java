/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package application.bst;

final class Body {
    private static final float PI = (float) 3.141592653589793;
    private static final float SOLAR_MASS = 4 * PI * PI;
    private static final float DAYS_PER_YER = (float) 365.24;

    float x;
    float y;
    float z;
    float vx;
    float vy;
    float vz;
    final float mass;

    void offsetMomentum(final float px, final float py, final float pz) {
        vx = (float) 0.0 - (px / SOLAR_MASS);
        vy = (float) 0.0 - (py / SOLAR_MASS);
        vz = (float) 0.0 - (pz / SOLAR_MASS);
    }

    Body(final float x, final float y, final float z,
            final float vx, final float vy, final float vz, final float mass) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx * DAYS_PER_YER;
        this.vy = vy * DAYS_PER_YER;
        this.vz = vz * DAYS_PER_YER;
        this.mass = mass * SOLAR_MASS;
    }

    static Body jupiter() {
        return new Body(
                (float) 4.84143144246472090e+00,
                (float) -1.16032004402742839e+00,
                (float) -1.03622044471123109e-01,
                (float) 1.66007664274403694e-03,
                (float) 7.69901118419740425e-03,
                (float) -6.90460016972063023e-05,
                (float) 9.54791938424326609e-04);
    }

    static Body saturn() {
        return new Body(
                (float) 8.34336671824457987e+00,
                (float) 4.12479856412430479e+00,
                (float) -4.03523417114321381e-01,
                (float) -2.76742510726862411e-03,
                (float) 4.99852801234917238e-03,
                (float) 2.30417297573763929e-05,
                (float) 2.85885980666130812e-04);
    }

    static Body uranus() {
        return new Body(
                (float) 1.28943695621391310e+01,
                (float) -1.51111514016986312e+01,
                (float) -2.23307578892655734e-01,
                (float) 2.96460137564761618e-03,
                (float) 2.37847173959480950e-03,
                (float) -2.96589568540237556e-05,
                (float) 4.36624404335156298e-05);
    }

    static Body neptune() {
        return new Body(
                (float) 1.53796971148509165e+01,
                (float) -2.59193146099879641e+01,
                (float) 1.79258772950371181e-01,
                (float) 2.68067772490389322e-03,
                (float) 1.62824170038242295e-03,
                (float) -9.51592254519715870e-05,
                (float) 5.15138902046611451e-05);
    }

    static Body sun() {
        return new Body((float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0,
                (float) 1.0);
    }
}
