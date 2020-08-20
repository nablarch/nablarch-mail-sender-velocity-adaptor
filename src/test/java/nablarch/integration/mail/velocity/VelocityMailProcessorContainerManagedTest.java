package nablarch.integration.mail.velocity;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.Rule;
import org.junit.Test;

import nablarch.common.mail.TemplateEngineProcessedResult;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.ComponentFactory;
import nablarch.test.support.SystemRepositoryResource;

/**
 * {@link VelocityMailProcessor}をコンポーネント設定ファイルで構築する場合のテストクラス。
 */
public class VelocityMailProcessorContainerManagedTest {

    @Rule
    public SystemRepositoryResource systemRepositoryResource = new SystemRepositoryResource(
            "nablarch/integration/mail/velocity/VelocityMailProcessorContainerManagedTest.xml");

    /**
     * コンポーネント設定ファイルで構築するテスト。
     */
    @Test
    public void testProcessConfiguredByXml() {

        VelocityMailProcessor sut = SystemRepository.get("templateEngineMailProcessor");

        //テンプレートエンジンの処理をして設定済みVelocityEngineを
        //使用できていることを確認する。
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("foo", "0");
        variables.put("bar", false);
        variables.put("bazs", Arrays.asList("1", "2", "3"));
        TemplateEngineProcessedResult result = sut.process(
                "nablarch/integration/mail/velocity/testProcessConfiguredByXml.txt", null,
                Collections.unmodifiableMap(variables));

        assertThat(result.getSubject(), is("あああ0"));
        assertThat(result.getMailBody(), is("いいい\r\nえええ1\r\nえええ2\r\nえええ3\r\n"));
    }

    public static class VelocityEngineFactory implements ComponentFactory<VelocityEngine> {

        @Override
        public VelocityEngine createObject() {
            VelocityEngine velocityEngine = new VelocityEngine();

            velocityEngine.setProperty("resource.loader", "classloader");
            velocityEngine.setProperty("classloader.resource.loader.class",
                    ClasspathResourceLoader.class.getName());
            velocityEngine.init();

            return velocityEngine;
        }
    }
}
