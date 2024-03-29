package help.lixin.jenkins.service;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import help.lixin.core.exception.jenkins.JenkinsTemplateNotFoundException;
import help.lixin.jenkins.action.entity.JenkinsActionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Map;

public class JenkinsTemplateLoadFaceService {

    private Logger logger = LoggerFactory.getLogger(JenkinsTemplateLoadFaceService.class);

    private ILoadJenkinsTemplateService loadJenkinsTemplateService;

    private static final String JENKINS_SERVICE_TEMPLATE_NAME = "JENKINS_SERVICE_TEMPLATE_NAME";

    private Configuration configuration = null;

    private StringTemplateLoader templateLoader = null;

    public JenkinsTemplateLoadFaceService(ILoadJenkinsTemplateService loadJenkinsTemplateService) {
        this.loadJenkinsTemplateService = loadJenkinsTemplateService;

        templateLoader = new StringTemplateLoader();

        configuration = new Configuration(Configuration.VERSION_2_3_30);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateLoader(templateLoader);
    }

    public String loadAndProcess(JenkinsActionParams params, Map<String, Object> ctx) throws JenkinsTemplateNotFoundException {
        String templateString = loadJenkinsTemplateService.load(params);
        String result = templateString;
        if (null != templateString) {
            try {
                if (null == templateLoader.findTemplateSource(JENKINS_SERVICE_TEMPLATE_NAME)) {
                    synchronized (templateLoader) {
                        if (null == templateLoader.findTemplateSource(JENKINS_SERVICE_TEMPLATE_NAME)) {
                            templateLoader.putTemplate(JENKINS_SERVICE_TEMPLATE_NAME, templateString);
                        }
                    }
                }
                Template template = configuration.getTemplate(JENKINS_SERVICE_TEMPLATE_NAME);
                StringWriter stringWriter = new StringWriter();
                template.process(ctx, stringWriter);
                result = stringWriter.toString();
            } catch (Exception e) {
                String msg = String.format("模板内容:[\n%s]\n渲染出现错误,异常信息如下:[\n%s\n]", templateString, e.getMessage());
                logger.error(msg);
                throw new JenkinsTemplateNotFoundException(msg);
            }
        }
        return result;
    }
}
