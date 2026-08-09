import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.flags.DefaultsCollection;

import java.util.Map;

public final class defaults {
    public static void main(String[] args) {
        Zenmanage zenmanage = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(System.getenv().getOrDefault("ZENMANAGE_ENVIRONMENT_TOKEN", "srv_your_server_key_here"))
                .build()
        );

        System.out.println("Inline default: " + zenmanage.flags().single("missing-flag", true).isEnabled());

        DefaultsCollection defaults = DefaultsCollection.fromMap(Map.of(
            "feature-a", true,
            "api-version", "v2",
            "max-items", 100
        ));

        String version = zenmanage.flags().withDefaults(defaults).single("api-version").asString();
        System.out.println("Collection default: " + version);

        String inlineWins = zenmanage.flags().withDefaults(defaults).single("api-version", "v3").asString();
        System.out.println("Inline default precedence: " + inlineWins);
    }
}
