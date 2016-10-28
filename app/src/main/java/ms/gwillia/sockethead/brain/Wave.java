package ms.gwillia.sockethead.brain;

/**
 * Created by chris on 23/10/16.
 */

public class Wave {

    private double powerHigh;
    private double powerLow;
    private double power;

    public Wave(double powerLow, double powerHigh) {
        this.powerHigh = powerHigh;
        this.powerLow = powerLow;
    }

    public Wave(double power) {
        this.power = power;
    }

    public Wave() {

    }

    public double getPowerHigh() {
        return powerHigh;
    }

    public void setPowerHigh(double powerHigh) {
        this.powerHigh = powerHigh;
    }

    public double getPowerLow() {
        return powerLow;
    }

    public void setPowerLow(double powerLow) {
        this.powerLow = powerLow;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    @Override
    public String toString() {
        return "Wave{" +
                "powerHigh=" + powerHigh +
                ", powerLow=" + powerLow +
                ", power=" + power +
                '}';
    }
}
