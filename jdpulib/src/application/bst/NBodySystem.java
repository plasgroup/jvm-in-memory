/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package application.bst;

public class NBodySystem {
    private final Body[] bodies;

    public NBodySystem() {
        // bodies = createBodies();
        bodies = null;
    }

    public int test() {
        return 1;
    }

    public Body[] createBodies() {
        Body[] bodies = new Body[] { Body.sun(),
                Body.jupiter(),
                Body.saturn(),
                Body.uranus(),
                Body.neptune() };

        float px = (float) 0.0;
        float py = (float) 0.0;
        float pz = (float) 0.0;

        for (Body b : bodies) {
            px += b.vx * b.mass;
            py += b.vy * b.mass;
            pz += b.vz * b.mass;
        }

        bodies[0].offsetMomentum(px, py, pz);

        return bodies;
    }

    public void advance(final float dt) {

        for (int i = 0; i < bodies.length; ++i) {
            Body iBody = bodies[i];

            for (int j = i + 1; j < bodies.length; ++j) {
                Body jBody = bodies[j];
                float dx = iBody.x - jBody.x;
                float dy = iBody.y - jBody.y;
                float dz = iBody.z - jBody.z;

                float dSquared = dx * dx + dy * dy + dz * dz;
                float distance = (float) Sqrt.compute(dSquared);
                float mag = dt / (dSquared * distance);

                iBody.vx = iBody.vx - (dx * jBody.mass * mag);
                iBody.vy = iBody.vy - (dy * jBody.mass * mag);
                iBody.vz = iBody.vz - (dz * jBody.mass * mag);

                jBody.vx = jBody.vx + (dx * iBody.mass * mag);
                jBody.vy = jBody.vy + (dy * iBody.mass * mag);
                jBody.vz = jBody.vz + (dz * iBody.mass * mag);
            }
        }

        for (Body body : bodies) {
            body.x = body.x + dt * body.vx;
            body.y = body.y + dt * body.vy;
            body.z = body.z + dt * body.vz;
        }
    }

    public float energy() {
        float e = (float) 0.0;

        for (int i = 0; i < bodies.length; ++i) {
            Body iBody = bodies[i];
            e += (float) 0.5 * iBody.mass
                    * (iBody.vx * iBody.vx +
                            iBody.vy * iBody.vy +
                            iBody.vz * iBody.vz);

            for (int j = i + 1; j < bodies.length; ++j) {
                Body jBody = bodies[j];
                float dx = iBody.x - jBody.x;
                float dy = iBody.y - jBody.y;
                float dz = iBody.z - jBody.z;

                float distance = (float) Sqrt.compute(dx * dx + dy * dy + dz * dz);
                e -= (iBody.mass * jBody.mass) / distance;
            }
        }
        return (float) e;
    }

    public NBodySystem _new() {
        return new NBodySystem();
    }

}
