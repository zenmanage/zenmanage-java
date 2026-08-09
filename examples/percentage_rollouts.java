import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.context.Context;

import java.util.List;

public final class percentage_rollouts {
    public static void main(String[] args) {
        Zenmanage zenmanage = new Zenmanage(
            ConfigBuilder.create()
                .withEnvironmentToken(System.getenv().getOrDefault("ZENMANAGE_ENVIRONMENT_TOKEN", "srv_your_server_key_here"))
                .build()
        );

        List<String> users = List.of("user-1", "user-2", "user-3", "user-4", "user-5");
        for (String userId : users) {
            boolean enabled = zenmanage.flags()
                .withContext(Context.single("user", userId))
                .single("new-checkout-flow", false)
                .isEnabled();

            System.out.println(userId + " in rollout? " + enabled);
        }

        Context stable = Context.single("user", "stable-user");
        boolean first = zenmanage.flags().withContext(stable).single("rollout-flag", false).isEnabled();
        boolean second = zenmanage.flags().withContext(stable).single("rollout-flag", false).isEnabled();
        System.out.println("Deterministic result: " + (first == second));
    }
}
