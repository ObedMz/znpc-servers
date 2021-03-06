package ak.znetwork.znpcservers.skin;

import ak.znetwork.znpcservers.skin.impl.SkinFetcherImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Copyright (c) ZNetwork, 2020.</p>
 *
 * @author ZNetwork
 * @since 07/02/2020
 */
public class SkinFetcher implements SkinFetcherImpl {

    /**
     * A empty string.
     */
    private static final String EMPTY_STRING = "";

    /**
     * A empty array of size 1.
     */
    private static final String[] EMPTY_ARRAY = new String[]{EMPTY_STRING,
            EMPTY_STRING};

    /**
     * The default charset name.
     */
    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * The executor service to delegate work.
     */
    private static final ExecutorService SKIN_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    /**
     * Creates a new parser.
     */
    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * The skin builder.
     */
    private final SkinBuilder builder;

    /**
     * The skin api type.
     */
    private final SkinAPI skinAPI;

    /**
     * Creates a new skin fetcher.
     * With all the builder provided types.
     *
     * @param builder The skin builder.
     */
    public SkinFetcher(SkinBuilder builder) {
        this.builder = builder;
        this.skinAPI = builder.getApiUrl();
    }

    /**
     * Returns the api server response.
     *
     * @return The http response.
     */
    private CompletableFuture<JsonObject> getResponse() {
        CompletableFuture<JsonObject> completableFuture = new CompletableFuture<>();
        SKIN_EXECUTOR_SERVICE.submit(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(builder.getApiUrl().getApiURL() + getData()).openConnection();
                connection.setRequestMethod(builder.getApiUrl().getMethod());

                connection.setDoInput(true);

                if (builder.getApiUrl() == SkinAPI.GENERATE_API) {
                    // Send data
                    connection.setDoOutput(true);
                    try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                        outputStream.writeBytes("url=" + URLEncoder.encode(builder.getData(), DEFAULT_CHARSET));
                    }
                }

                try (Reader reader = new InputStreamReader(connection.getInputStream(), Charset.forName(DEFAULT_CHARSET))) {
                    // Read json
                    completableFuture.complete(JSON_PARSER.parse(reader).getAsJsonObject());
                } finally {
                    connection.disconnect();
                }
            } catch (Throwable throwable) {
                completableFuture.completeExceptionally(throwable);
            }
        });
        return completableFuture;
    }

    @Override
    public String[] getProfile() {
        try {
            JsonObject data = getResponse().get().getAsJsonObject(skinAPI == SkinAPI.GENERATE_API ? "data" : "textures");
            JsonObject properties = (skinAPI == SkinAPI.GENERATE_API ?
                    data.getAsJsonObject("texture") :
                    data.getAsJsonObject("raw"));

            return new String[]{ properties.get("value").getAsString(), properties.get("signature").getAsString() };
        } catch (InterruptedException | ExecutionException e) {
            // The skin was not found, return the default skin profile (Steve)
            return EMPTY_ARRAY;
        }
    }

    /**
     * Returns real data for skin api.
     *
     * @return The data for skin api.
     */
    private String getData() {
        return skinAPI != SkinAPI.GENERATE_API ?
                "/" + builder.getData() : EMPTY_STRING;
    }
}
