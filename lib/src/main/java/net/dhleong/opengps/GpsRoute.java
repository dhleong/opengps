package net.dhleong.opengps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dhleong
 */
public final class GpsRoute {

    public static class Step {

        public enum Type {
            BEARING_TO,
            BEARING_FROM,
            FIX
        }

        public final Type type;
        public final AeroObject ref;
        public final float bearing;
        public final float distance;

        Step(Type type, AeroObject ref, float bearing, float distance) {
            this.type = type;
            this.ref = ref;
            this.bearing = bearing;
            this.distance = distance;
        }

        @Override
        public String toString() {
            return "Step{" +
                "type=" + type +
                ", ref=" + ref +
                ", bearing=" + bearing +
                ", distance=" + distance +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Step)) return false;

            Step step = (Step) o;

            if (Float.compare(step.bearing, bearing) != 0) return false;
            if (Float.compare(step.distance, distance) != 0) return false;
            if (type != step.type) return false;
            return ref.equals(step.ref);

        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + ref.hashCode();
            result = 31 * result + (bearing != +0.0f ? Float.floatToIntBits(bearing) : 0);
            result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
            return result;
        }

        public static Step fix(AeroObject obj) {
            return new Step(Type.FIX, obj, 0, 0);
        }

    }

    public static final int FLAG_INCLUDE_FIXES = 1;

    public static final int FLAGS_DEFAULT = FLAG_INCLUDE_FIXES;

    static final float MIN_BEARING_FROM_DISTANCE = 1f;

    final List<Step> steps = new ArrayList<>();
    final int flags;

    public GpsRoute() {
        this(FLAG_INCLUDE_FIXES);
    }

    public GpsRoute(int flags) {
        this.flags = flags;
    }

    public void add(AeroObject obj) {
        if (!steps.isEmpty()) {
            Step prev = steps.get(steps.size() - 1);
            float bearing = prev.ref.bearingTo(obj);
            float distance = prev.ref.distanceTo(obj);
            if (distance > MIN_BEARING_FROM_DISTANCE) {
                float halfDistance = distance * 0.5f;
                steps.add(new Step(Step.Type.BEARING_FROM, prev.ref, bearing, halfDistance));
                steps.add(new Step(Step.Type.BEARING_TO, obj, bearing, halfDistance));
            } else {

                steps.add(new Step(Step.Type.BEARING_TO, obj, bearing, distance));
            }
        }

        steps.add(Step.fix(obj));
    }

    public GpsRoute copy() {
        GpsRoute newRoute = new GpsRoute(flags);
        newRoute.steps.addAll(steps);
        return newRoute;
    }

    /** @deprecated Don't access the list directly; use {@link #step(int)} */
    @Deprecated
    public List<Step> steps() {
        return Collections.unmodifiableList(steps);
    }

    public Step step(int index) {
        return steps.get(index);
    }

    public int size() {
        return steps.size();
    }

    @Override
    public String toString() {
        return "GpsRoute{" +
            "steps=" + steps +
            '}';
    }

}
