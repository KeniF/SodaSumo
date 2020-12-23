public class Muscle extends Spring {
    private double amplitude, phase;

    public Muscle(int name) {
        super.name = name;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double a) {
        amplitude = a;
    }

    public double getPhase() {
        return phase;
    }

    public void setPhase(double p) {
        phase = p;
    }

    public String toString() {
        return name + " a:" + mass1 + " b:" + mass2 + " amp:" + amplitude + " phase:" + phase + " restLeng:" + restLength;
    }
}