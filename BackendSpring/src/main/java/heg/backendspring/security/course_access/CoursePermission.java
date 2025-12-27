package heg.backendspring.security.course_access;


public enum CoursePermission {
    READ(1),    // Can view course content
    WRITE(2),   // Can modify course content
    OWNER(3);   // Full control (can share, delete)

    private final int level;

    CoursePermission(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean includes(CoursePermission required) {
        return this.level >= required.level;
    }

    public static CoursePermission fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        return valueOf(value.toUpperCase().trim());
    }

}
