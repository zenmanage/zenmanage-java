import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.cache.FileSystemCache;
import com.zenmanage.sdk.config.ConfigBuilder;

import java.nio.file.Paths;

public final class caching {
    public static void main(String[] args) {
        String token = System.getenv().getOrDefault("ZENMANAGE_ENVIRONMENT_TOKEN", "srv_your_server_key_here");

        Zenmanage memory = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(token)
                .withCacheBackend("memory")
                .withCacheTtlSeconds(3600)
                .build()
        );

        System.out.println("Memory cache flag: " + memory.flags().single("example-flag", true).isEnabled());

        Zenmanage filesystem = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(token)
                .withCache(new FileSystemCache(Paths.get(System.getProperty("java.io.tmpdir"), "zenmanage-cache")))
                .withCacheTtlSeconds(7200)
                .build()
        );

        System.out.println("Filesystem cache flag: " + filesystem.flags().single("example-flag", true).isEnabled());

        Zenmanage noCache = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(token)
                .withCacheBackend("null")
                .build()
        );

        System.out.println("Null cache flag: " + noCache.flags().single("example-flag", true).isEnabled());
    }
}
