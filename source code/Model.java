import java.util.Collection;
import java.util.HashMap;

public class Model {
    private final HashMap<Integer, Mass> massMap = new HashMap<>();
    private final HashMap<Integer, Spring> springMap = new HashMap<>();
    private final HashMap<Integer, Muscle> muscleMap = new HashMap<>();
    private double friction, gravity, springyness, wavePhase, waveSpeed, waveAmplitude,
            boundTop = -10000, boundBottom = 10000, boundRight = -10000, boundLeft = 10000;
    private String name;

    public Model() {
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double g) {
        gravity = g;
    }

    public double getFriction() {
        return friction;
    }

    public void setFriction(double f) {
        friction = f;
    }

    public double getSpringyness() {
        return springyness;
    }

    public void setSpringyness(double s) {
        springyness = s;
    }

    public double getWavePhase() {
        return wavePhase;
    }

    public void setWavePhase(double p) {
        wavePhase = p;
    }

    public double getWaveSpeed() {
        return waveSpeed;
    }

    public void setWaveSpeed(double s) {
        waveSpeed = s;
    }

    public double getWaveAmplitude() {
        return waveAmplitude;
    }

    public void setWaveAmplitude(double a) {
        waveAmplitude = a;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public void setWaveDirection(int d) {
    }

    public void addMass(Mass m) {
        massMap.put(m.getName(), m);
        adjustBoundRect(m);
    }

    public void adjustBoundRect(Mass m) {
        if (m.getX() < boundLeft)
            boundLeft = m.getX();
        if (m.getX() > boundRight)
            boundRight = m.getX();
        if (m.getY() > boundTop)
            boundTop = m.getY();
        if (m.getY() < boundBottom)
            boundBottom = m.getY();
    }

    public void resetBoundRect() {
        boundTop = -10000;
        boundBottom = 10000;
        boundRight = -10000;
        boundLeft = 10000;
    }

    public double[] getBoundingRectangle() {
        double[] boundingRectangle = new double[4];
        boundingRectangle[0] = boundTop;
        boundingRectangle[1] = boundBottom;
        boundingRectangle[2] = boundLeft;
        boundingRectangle[3] = boundRight;
        return boundingRectangle;
    }

    public double getBoundRight() {
        return boundRight;
    }

    public double getBoundLeft() {
        return boundLeft;
    }

    public Mass getMass(int s) {
        return massMap.get(s);
    }

    public Spring getSpring(int s) {
        return springMap.get(s);
    }

    public Muscle getMuscle(int s) {
        return muscleMap.get(s);
    }

    public Muscle[] getAllMuscle() {
        Collection<Muscle> collection = muscleMap.values();
        return collection.toArray(new Muscle[0]);
    }

    public void addSpring(Spring s) {
        springMap.put(s.getName(), s);
    }

    public void addMuscle(Muscle m) {
        muscleMap.put(m.getName(), m);
    }

    public int totalNoOfMass() {
        return massMap.size();
    }

    public int totalNoOfSpring() {
        return springMap.size();
    }

    public int totalNoOfMuscle() {
        return muscleMap.size();
    }

    public String toString() {
        return "[Model] " + "Masses:" + massMap.size() + "\nSprings:" + springMap.size() + "\nMuscles:"
                + muscleMap.size();
    }
}