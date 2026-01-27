package io.github.zhengyuelaii.desensitize.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 测试 EasyDesensitizeAutoConfiguration
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-27
 */
public class EasyDesensitizePropertiesTest {

    @Test
    void should_bind_properties_correctly() {
        new ApplicationContextRunner()
                .withUserConfiguration(EasyDesensitizeAutoConfiguration.class)
                .withPropertyValues(
                        "easy.desensitize.enabled=true",
                        "easy.desensitize.use-global-cache=false",
                        "easy.desensitize.use-global-resolver=false"
                )
                .run(context -> {
                    EasyDesensitizeProperties props =
                            context.getBean(EasyDesensitizeProperties.class);

                    assertThat(props.isEnabled()).isTrue();
                    assertThat(props.isUseGlobalCache()).isFalse();
                    assertThat(props.isUseGlobalResolver()).isFalse();
                });
    }

}
