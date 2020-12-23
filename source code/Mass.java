public class Mass {
    private final int name;
    private double vx = 0;
    private double vy = 0;
    private double x = 0;
    private double y = 0;
    private double oldVx = 0;
    private double oldVy = 0;
    private double oldX = 0;
    private double oldY = 0;
    private boolean toRevertX = false, toRevertY = false;

    public Mass(int name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        oldX = this.x;
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        oldY = this.y;
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        oldVx = this.vx;
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        oldVy = this.vy;
        this.vy = vy;
    }

    public double getOldVx() {
        return oldVx;
    }

    public double getOldVy() {
        return oldVy;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public void revertX() {
        x = oldX;
    }

    public void revertY() {
        y = oldY;
    }

    public int getName() {
        return name;
    }

    public void finallyRevert() {
        if (toRevertX) {
            x = oldX;
            toRevertX = false;
        }
        if (toRevertY) {
            y = oldY;
            toRevertY = false;
        }
    }

    public String toString() {
        return name + " Vx:" + vx + " Vy:" + vy + " X:" + x + " Y:" + y;
    }
}
