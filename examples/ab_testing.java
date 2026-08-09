import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;

import java.util.List;

public final class ab_testing {
    public static void main(String[] args) {
        Zenmanage zenmanage = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(System.getenv().getOrDefault("ZENMANAGE_ENVIRONMENT_TOKEN", "srv_your_server_key_here"))
                .build()
        );

        List<String> users = List.of("user-001", "user-002", "user-003");

        for (String userId : users) {
            Context context = Context.single("user", userId)
                .addAttribute(new Attribute("country").addValue("US"));

            String variant = zenmanage.flags()
                .withContext(context)
                .single("checkout-flow", "control")
                .asString();

            System.out.println(userId + " -> " + variant);
        }
    }
}
