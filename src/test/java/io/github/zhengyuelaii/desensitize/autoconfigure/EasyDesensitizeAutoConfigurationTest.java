package io.github.zhengyuelaii.desensitize.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * 测试 EasyDesensitizeAutoConfiguration
 *
 * @author zhengyuelaii
 * @version 1.0.0
 * @since 2026-01-27
 */
public class EasyDesensitizeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(EasyDesensitizeAutoConfiguration.class);

    @Test
    void should_register_core_beans_by_default() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EasyDesensitizeResponseAdvice.class);
            assertThat(context).hasSingleBean(EasyDesensitizeProperties.class);
        });
    }

    @Test
    void should_not_register_when_disabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(EasyDesensitizeAutoConfiguration.class)
                .withPropertyValues("easy.desensitize.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(EasyDesensitizeResponseAdvice.class);
                });
    }

}
