package org.ethelred.icfollow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Main implements Runnable{
    private static final Logger RESULT_LOGGER = LoggerFactory.getLogger("result");

    public static void main(String[] args) {
//        System.setProperty("jdk.httpclient.HttpClient.log", "errors,requests,headers,content");
        new Main().run();
    }

    @Override
    public void run() {
        var persistence = new FilePersistence(Path.of("icfollow.psv"));
        var client = new DefaultPairClient();
        var combiner = new Combiner(client, Base.ELEMENTS, persistence, this::logResult);

        combiner.awaitCompletion();
    }

    private void logResult(Result result) {
        RESULT_LOGGER.info("{} and {} make {}", result.pair().first(), result.pair().second(), result.result());
        if (result.isNew()) {
            RESULT_LOGGER.info("^^^ NEW! ^^^");
        }
    }
}
