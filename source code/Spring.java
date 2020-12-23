public class Spring {
    protected int name;
    protected Mass mass1, mass2;
    protected double restLength;

    public Spring(int name) {
        this.name = name;
    }

    public Spring() {
    }

    public int getName() {
        return name;
    }

    public Mass getMass1() {
        return mass1;
    }

    public void setMass1(Mass m) {
        mass1 = m;
    }

    public Mass getMass2() {
        return mass2;
    }

    public void setMass2(Mass m) {
        mass2 = m;
    }

    public double getRestLength() {
        return restLength;
    }

    public void setRestLength(double r) {
        this.restLength = r;
    }

    public String toString() {
        return name + " a:" + mass1 + " b:" + mass2 + " restlength:" + restLength;
    }

}