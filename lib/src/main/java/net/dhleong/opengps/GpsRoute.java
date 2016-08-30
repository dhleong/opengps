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

        public static Step from(AeroObject obj, float bearing, float distance) {
            return new Step(Type.BEARING_FROM, obj, bearing, distance);
        }

        public static Step to(AeroObject obj, float bearing, float distance) {
            return new Step(Type.BEARING_TO, obj, bearing, distance);
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
        add(steps.size(), obj);
    }

    public void add(int index, AeroObject obj) {
        if (!steps.isEmpty() && index > 0) {
            Step prev = steps.get(index - 1);
            float bearing = prev.ref.bearingTo(obj);
            float distance = prev.ref.distanceTo(obj);
            if (distance > MIN_BEARING_FROM_DISTANCE) {
                float halfDistance = distance * 0.5f;
                steps.add(index++, new Step(Step.Type.BEARING_FROM, prev.ref, bearing, halfDistance));
                steps.add(index++, new Step(Step.Type.BEARING_TO, obj, bearing, halfDistance));
            } else {

                steps.add(index++, new Step(Step.Type.BEARING_TO, obj, bearing, distance));
            }
        }

        steps.add(index, Step.fix(obj));
    }

    public GpsRoute copy() {
        GpsRoute newRoute = new GpsRoute(flags);
        newRoute.steps.addAll(steps);
        return newRoute;
    }

    public int indexOfWaypoint(AeroObject entry) {
        for (int i=0, len=steps.size(); i < len; i++) {
            if (steps.get(i).ref.equals(entry)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * NB: You can ONLY remove {@link Step.Type#FIX}-type steps
     */
    public void removeStep(int step) {
        final Step victim = steps.get(step);
        if (victim.type != Step.Type.FIX) {
            throw new IllegalArgumentException("Cannot directly remove " + victim);
        }

        int nextFixIdx = findFix(steps, step, 1);
        int prevFixIdx = findFix(steps, step, -1);

        if (nextFixIdx != -1) {
            // dec FIRST so we don't remove the next fix
            while (--nextFixIdx != step) {
                steps.remove(nextFixIdx);
            }
        }

        steps.remove(step);

        if (prevFixIdx != -1) {
            int prevFixStep = prevFixIdx + 1;
            for (int i=0, limit=step - prevFixStep; i < limit; i++) {
                steps.remove(prevFixStep);
            }

            // just remove and re-add
            if (nextFixIdx != -1) {
                add(prevFixStep, steps.remove(prevFixStep).ref);
            }
        }
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

    static int findFix(List<Step> steps, int start, int increment) {
        for (int i=start+increment, len=steps.size(); i < len && i >= 0; i += increment) {
            if (steps.get(i).type == Step.Type.FIX) {
                return i;
            }
        }
        return -1;
    }

}
