package nablarch.integration.mail.velocity;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import nablarch.common.mail.TemplateEngineProcessedResult;
import nablarch.common.mail.TemplateEngineProcessingException;

/**
 * {@link VelocityMailProcessor}のテストクラス。
 */
public class VelocityMailProcessorTest {

    private final VelocityMailProcessor sut = new VelocityMailProcessor();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * テンプレートエンジンの処理の確認。
     */
    @Test
    public void testProcess() {
        String templateId = "hello";
        String lang = null;
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("foo", templateId);
        variables.put("bar", 123);

        TemplateEngineProcessedResult result = sut.process(templateId, lang, variables);

        assertThat(result.getSubject(), is("件名テスト：hello"));
        assertThat(result.getMailBody(), is("本文テスト１：hello\r\n本文テスト２：123\r\n"));
    }

    /**
     * 発生した{@link VelocityException}を{@link TemplateEngineProcessingException}でラップすること。
     */
    @Test
    public void testProcess_velocity_exception() {
        expectedException.expect(TemplateEngineProcessingException.class);
        expectedException.expectCause(isA(VelocityException.class));

        //テンプレートが見つからない場合、VelocityExceptionのサブクラスである
        //ResourceNotFoundExceptionが投げられる。
        sut.process("not_found", null, Collections.<String, Object> emptyMap());
    }

    /**
     * デリミタを変更した場合の確認。
     */
    @Test
    public void testProcess_alter_delimiter() {
        String templateId = "alter-delimiter";
        String lang = null;
        Map<String, Object> variables = Collections.emptyMap();

        sut.setDelimiter("@@@");

        TemplateEngineProcessedResult result = sut.process(templateId, lang, variables);

        assertThat(result.getSubject(), is("---"));
        assertThat(result.getMailBody(), is("Alter delimiter test."));
    }

    @Before
    public void setUp() {

        VelocityEngine velocityEngine = new VelocityEngine();

        String repositoryName = VelocityMailProcessorTest.class.getName();

        //インメモリでテンプレートを管理するための設定
        velocityEngine.setProperty("resource.loader", "string");
        velocityEngine.setProperty("string.resource.loader.class",
                StringResourceLoader.class.getName());
        velocityEngine.setProperty("string.resource.loader.repository.name", repositoryName);
        velocityEngine.setProperty("string.resource.loader.repository.static", "false");
        velocityEngine.init();

        //テンプレートの準備
        StringResourceRepository repos = (StringResourceRepository) velocityEngine
                .getApplicationAttribute(repositoryName);
        repos.putStringResource("hello",
                "件名テスト：$foo\r\n---\r\n本文テスト１：$foo\r\n本文テスト２：$bar\r\n");
        repos.putStringResource("alter-delimiter",
                "---\r\n@@@\r\nAlter delimiter test.");

        sut.setVelocityEngine(velocityEngine);
    }
}
