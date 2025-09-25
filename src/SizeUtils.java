import java.util.Optional;

final class SizeUtils {
    private SizeUtils() {}
    static Optional<Size> parse(String size) {
        if (size == null) return Optional.empty();
        try {
            return Optional.of(Size.valueOf(size.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}