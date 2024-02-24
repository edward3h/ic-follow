package org.ethelred.icfollow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Consumer;

public class FilePersistence implements Persistence {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePersistence.class);

    private final Path path;

    public FilePersistence(Path path) {
        this.path = path;
    }

    private String encode(Result result) {
        return String.join("|",
                result.pair().first().name(),
                result.pair().first().emoji(),
                result.pair().second().name(),
                result.pair().second().emoji(),
                result.result().name(),
                result.result().emoji(),
                String.valueOf(result.isNew())
        );
    }

    private Result decode(String line) {
        try {
            var parts = line.split("\\|");
            if (parts.length < 5) {
                throw new IllegalArgumentException("Not enough columns");
            }
            Pair pair = new Pair(new Item(parts[0], parts[1]),
                    new Item(parts[2], parts[3]));
            Item item = new Item(parts[4], parts.length > 5 ? parts[5] : "");
            boolean isNew = parts.length > 6 && Boolean.parseBoolean(parts[6]);
            return new Result(pair, item, isNew);
        } catch (Exception e) {
            LOGGER.warn("Could not decode line {}", line, e);
        }
        return null;
    }

    @Override
    public void store(Result result) {
        try {
            Files.writeString(path, encode(result) + "\n", StandardOpenOption.APPEND, StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(Consumer<Result> resultConsumer) {
        if (Files.exists(path)) {
            try {
                Files.lines(path).map(this::decode).filter(Objects::nonNull).forEach(resultConsumer::accept);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
