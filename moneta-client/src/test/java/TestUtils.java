import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

@Slf4j
public final class TestUtils {
    private TestUtils() {
        throw new UnsupportedOperationException();
    }

    public static Optional<Integer> getRandomPort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return Optional.of(socket.getLocalPort());
        } catch (Exception e) {
            log.warn("", e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warn("", e);
                }
            }
        }

        return Optional.empty();
    }
}
