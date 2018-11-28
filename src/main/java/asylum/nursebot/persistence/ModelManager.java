package asylum.nursebot.persistence;

import asylum.nursebot.persistence.selfbuilding.Selfbuilding;

public class ModelManager {

    private static boolean wasAnythingCreated = false;

    public static <T extends Selfbuilding> void build(Class<T> c) {
        System.out.println("building model class " + c.getCanonicalName() + "...");
        try {
            if (c.newInstance().selfbuild()) {
                wasAnythingCreated = true;
            }
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean wasAnythingCreated() {
        return wasAnythingCreated;
    }
}
