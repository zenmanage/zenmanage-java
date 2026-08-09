import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;

import java.util.List;
import java.util.Map;

public final class context_based_flags {
    public static void main(String[] args) {
        Zenmanage zenmanage = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(System.getenv().getOrDefault("ZENMANAGE_ENVIRONMENT_TOKEN", "srv_your_server_key_here"))
                .build()
        );

        Context userContext = Context.single("user", "user-12345", "Jane Doe")
            .addAttribute(new Attribute("country", List.of("US")))
            .addAttribute(new Attribute("plan", List.of("premium")));

        boolean featureEnabled = zenmanage.flags()
            .withContext(userContext)
            .single("premium-feature", false)
            .isEnabled();

        System.out.println("Premium feature enabled: " + featureEnabled);

        Context fromMap = Context.fromMap(Map.of(
            "type", "organization",
            "identifier", "org-1",
            "name", "Acme",
            "attributes", List.of(
                Map.of("key", "region", "values", List.of(Map.of("value", "us-east")))
            )
        ));

        boolean orgFeature = zenmanage.flags()
            .withContext(fromMap)
            .single("org-dashboard", false)
            .isEnabled();

        System.out.println("Org dashboard enabled: " + orgFeature);
    }
}
