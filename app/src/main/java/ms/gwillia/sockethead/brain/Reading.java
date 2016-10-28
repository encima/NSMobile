package ms.gwillia.sockethead.brain;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> getMap() {
        Map<String, Object> values = new HashMap<String,Object>();
        values.put("delta", this.delta);
        values.put("theta", this.theta);
        values.put("alpha", this.alpha);
        values.put("beta", this.beta);
        values.put("gamma", this.gamma);
        values.put("time", this.time);
        values.put("system", this.system);
        values.put("fgApp", this.fgApp);
        return values;

    }
}
