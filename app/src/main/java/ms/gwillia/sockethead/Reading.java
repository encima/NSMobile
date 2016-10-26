package ms.gwillia.sockethead;

/**
 * Created by chris on 22/10/16.
 */

public class Reading {

    Wave delta;
    Wave theta;
    Wave alpha;
    Wave beta;
    Wave gamma;
    long time;
    String system;
    String fgApp;

    public Reading() {

    }

    public Reading(Wave delta, Wave theta, Wave alpha, Wave beta, Wave gamma, long time, String system, String fgApp) {
        this.delta = delta;
        this.theta = theta;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.time = time;
        this.system = system;
        this.fgApp = fgApp;
    }


    @Override
    public String toString() {
        return "Reading{" +
                "delta=" + delta +
                ", theta=" + theta +
                ", alpha=" + alpha +
                ", beta=" + beta +
                ", gamma=" + gamma +
                ", time=" + time +
                ", system='" + system + '\'' +
                ", fgApp='" + fgApp + '\'' +
                '}';
    }
}
