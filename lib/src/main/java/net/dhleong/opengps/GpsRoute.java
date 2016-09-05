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
            AIRWAY,
            AIRWAY_EXIT,
            BEARING_TO,
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

        public static Step airway(AeroObject obj) {
            return new Step(Type.AIRWAY, obj, 0, 0);
        }

        public static Step airwayExit(Airway airway) {
            return new Step(Type.AIRWAY_EXIT, airway, 0, 0);
        }

        public static Step fix(AeroObject obj) {
            return new Step(Type.FIX, obj, 0, 0);
        }

        public static Step to(AeroObject obj, float bearing, float distance) {
            return new Step(Type.BEARING_TO, obj, bearing, distance);
        }

    }

    public static final int FLAG_INCLUDE_FIXES = 1;

    public static final int FLAGS_DEFAULT = FLAG_INCLUDE_FIXES;

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
        if (obj instanceof Airway) {
            steps.add(index, Step.airway(obj));
            return;
        }

        if (!steps.isEmpty() && index > 0) {
            // find the previous FIX (usually right behind, but not always)
            int prevIndex = index - 1;
            Step prev;
            do {
                prev = steps.get(prevIndex--);
            } while (prev.type != Step.Type.FIX && prevIndex >= 0);

            float bearing = prev.ref.bearingTo(obj);
            float distance = prev.ref.distanceTo(obj);

            steps.add(index++, new Step(Step.Type.BEARING_TO, obj, bearing, distance));
        }

        steps.add(index, Step.fix(obj));
    }

    void add(int index, Step step) {
        steps.add(index, step);
    }

    public GpsRoute copy() {
        GpsRoute newRoute = new GpsRoute(flags);
        newRoute.steps.addAll(steps);
        return newRoute;
    }

    public int indexOfWaypoint(AeroObject entry) {
        for (int i=0, len=steps.size(); i < len; i++) {
            final Step step = steps.get(i);
            if (step.type == Step.Type.FIX && step.ref.equals(entry)) {
                return i;
            }
        }

        return -1;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
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

    public void removeStepsAfter(int step) {
        final Step victim = steps.get(step);
        if (victim.type != Step.Type.FIX) {
            throw new IllegalArgumentException("Cannot directly remove " + victim);
        }

        // this is a simpler, special case of the above
        for (int i=0, count=steps.size() - step -1; i < count; i++) {
            steps.remove(step + 1);
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
