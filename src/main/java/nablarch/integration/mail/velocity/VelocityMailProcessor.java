package nablarch.integration.mail.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import nablarch.common.mail.TemplateEngineMailProcessor;
import nablarch.common.mail.TemplateEngineProcessedResult;
import nablarch.common.mail.TemplateEngineProcessingException;

/**
 * Velocityを使用する{@link TemplateEngineMailProcessor}の実装クラス。
 * 
 * @author Taichi Uragami
 *
 */
public class VelocityMailProcessor implements TemplateEngineMailProcessor {

    /** Velocityのエンジン */
    private VelocityEngine velocityEngine;

    /** 件名と本文を分けるデリミタ */
    private String delimiter;

    /**
     * テンプレートIDから取得されたテンプレートと変数をマージして、その結果を返す。
     * 
     * <p>
     * テンプレートの検索は{@link VelocityEngine#getTemplate(String)}が使われる。
     * テンプレートと変数のマージは{@link Template#merge(Context, Writer)}が使われる。
     * </p>
     * 
     * <p>
     * ※この実装ではテンプレートの検索が多言語対応していないため、第二引数の言語は使用されない。
     * </p>
     * 
     * @see VelocityEngine#getTemplate(String)
     * @see Template#merge(Context, Writer)
     */
    @Override
    public TemplateEngineProcessedResult process(String templateId, String lang,
            Map<String, Object> variables) {

        try {
            Template template = velocityEngine.getTemplate(templateId);
            Context context = createContext(variables);
            StringWriter out = new StringWriter();
            template.merge(context, out);
            if (delimiter != null) {
                return TemplateEngineProcessedResult.valueOf(out.toString(), delimiter);
            }
            return TemplateEngineProcessedResult.valueOf(out.toString());
        } catch (VelocityException e) {
            throw new TemplateEngineProcessingException(e);
        }
    }

    /**
     * {@link Context}を作成する。
     * 
     * @param variables {@link #process(String, String, Map)}に渡された変数
     * @return {@link Context}のインスタンス
     */
    protected Context createContext(Map<String, Object> variables) {
        // メール機能の実装により、変更不可なMapとしてvariablesが渡ってくるが、
        // テンプレート内で#foreachを使用するとVelocityがここで指定したMapにputするためMapを作り直す。
        return new VelocityContext(new HashMap<String, Object>(variables));
    }

    /**
     * Velocityのエントリーポイントとなる{@link VelocityEngine}を設定する。
     * 
     * @param velocityEngine Velocityのエンジン
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * 件名と本文を分けるデリミタを設定する。
     * 
     * <p>
     * なにも設定されていなければ{@link TemplateEngineProcessedResult#DEFAULT_DELIMITER デフォルトのデリミタ}が使用される。
     * </p>
     * 
     * @param delimiter 件名と本文を分けるデリミタ
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
