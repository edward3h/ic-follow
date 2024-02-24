package org.ethelred.icfollow;

import com.github.mizosoft.methanol.Methanol;
import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultPairClient implements PairClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPairClient.class);
    private final ReentrantLock singleUseLock = new ReentrantLock();
    private final Random random = new Random();
    private final HttpClient httpClient = Methanol.newBuilder()
            .userAgent("just testing jaq@ethelred.org")
            .defaultHeader("Accept", "*/*")
            .defaultHeader("Referer", "https://neal.fun/infinite-craft/")
            .autoAcceptEncoding(true)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final JsonType<Item> itemJsonType;
    private final JsonType<IsNewHolder> isNewHolderType;

    public DefaultPairClient() {
        var jsonb = Jsonb.builder().build();
        itemJsonType = jsonb.type(Item.class);
        isNewHolderType = jsonb.type(IsNewHolder.class);
    }

    @Override
    public Result pair(Pair pair) {
        singleUseLock.lock();
        try {
            Thread.sleep((random.nextInt(11) + 1) * 666);
            return request(pair);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            singleUseLock.unlock();
        }
    }

    private Result request(Pair pair) {
        var uri = URI.create("https://neal.fun/api/infinite-craft/pair?" + queryString(pair));
        var request = HttpRequest.newBuilder(uri)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            var body = response.body();
            LOGGER.debug("Response body\n{}", body);
            var item = itemJsonType.fromJson(body);
            var inh = isNewHolderType.fromJson(body);
            return new Result(pair, item, inh.isNew());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Json
    public record IsNewHolder(boolean isNew) {

    }

    private String queryString(Pair pair) {
        return "first=%s&second=%s".formatted(URLEncoder.encode(pair.first().name(), StandardCharsets.UTF_8), URLEncoder.encode(pair.second().name(), StandardCharsets.UTF_8));
    }
}
