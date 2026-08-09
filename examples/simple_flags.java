import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.config.Config;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.flags.Flag;

import java.util.List;

public final class simple_flags {
    public static void main(String[] args) {
        Config config = ConfigBuilder.create()
            .withEnvironmentToken(System.getenv().getOrDefault("ZENMANAGE_ENVIRONMENT_TOKEN", "srv_your_server_key_here"))
            .build();

        Zenmanage zenmanage = new Zenmanage(config);

        Flag boolFlag = zenmanage.flags().single("example-boolean-flag", false);
        System.out.println("Boolean flag: " + boolFlag.isEnabled());

        Flag stringFlag = zenmanage.flags().single("example-string-flag", "default-value");
        System.out.println("String flag: " + stringFlag.asString());

        Flag numberFlag = zenmanage.flags().single("example-number-flag", 42);
        System.out.println("Number flag: " + numberFlag.asNumber());

        List<Flag> all = zenmanage.flags().all();
        System.out.println("Total flags loaded: " + all.size());
    }
}
