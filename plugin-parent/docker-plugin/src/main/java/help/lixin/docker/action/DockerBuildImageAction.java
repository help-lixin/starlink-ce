package help.lixin.docker.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import help.lixin.core.constants.Constant;
import help.lixin.core.pipeline.action.Action;
import help.lixin.core.pipeline.ctx.PipelineContext;
import help.lixin.docker.service.DockerFaceService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DockerBuildImageAction implements Action {
    private final Logger logger = LoggerFactory.getLogger(DockerBuildImageAction.class);

    private static final String DOCKER_BUILD_IMAGE_ACTION = "docker-build-image";

    private final DockerFaceService dockerFaceService;

    public DockerBuildImageAction(DockerFaceService dockerFaceService) {
        this.dockerFaceService = dockerFaceService;
    }

    @Override
    public boolean execute(PipelineContext ctx) throws Exception {
        logger.info("开始执行Docker镜像构建插件");

        String stageParams = ctx.getStageParams();
        ObjectMapper mapper = new ObjectMapper();
        DockerParams actionParams = mapper.readValue(stageParams, DockerParams.class);

        String dockerFile = expression(actionParams.getDockerFile(), ctx.getVars());
        String arfifactDir = getArfifactDir(actionParams, ctx.getVars());
        String arfifactDockerFullPath = String.format("%s%s%s", arfifactDir, File.separator, "Dockerfile");

        // 拷贝DockerFile到成品库目录底下,因为:Dockerfile需要与成品库在同一目录下的.
        copyDockerFileToArfifactDir(dockerFile, arfifactDockerFullPath);

        // args
        List<DockerBuildArg> buildArgs = processArgs(actionParams.getArgs(), ctx.getVars());
        // tags
        Set<String> tags = processTags(actionParams.getTags(), ctx.getVars());

        // 私有仓库的账号和密码
        String userName = expression(actionParams.getRepositoryUserName(), ctx.getVars());
        String userPwd = expression(actionParams.getRepositoryPassword(), ctx.getVars());


        Set<String> images = dockerFaceService.getDockerImageApiService().buildImage(
                //
                arfifactDockerFullPath,
                //
                buildArgs,
                //
                tags);

        if (!images.isEmpty()) {
            for (String imageName : images) {
                dockerFaceService.getDockerImageApiService().pushImage(imageName, userName, userPwd);
            }
        }

        logger.info("Docker镜像构建插件执行结束");
        return true;
    }

    protected void copyDockerFileToArfifactDir(String dockerFile, String arfifactDockerFullPath) throws Exception {
        File dockerFileRef = new File(dockerFile);
        File arfifactDockerFullPathRef = new File(arfifactDockerFullPath);

        if (!dockerFileRef.exists()) {
            String msg = String.format("Dockerfile:[%s]不存在", dockerFile);
            throw new RuntimeException(msg);
        }

        File arfifactDirRef = arfifactDockerFullPathRef.getParentFile();
        if (!arfifactDirRef.exists()) {
            String msg = String.format("成品库位置:[%s]不存在", arfifactDirRef.getPath());
            throw new RuntimeException(msg);
        }

        try {
            FileUtils.copyFile(dockerFileRef, arfifactDockerFullPathRef);
        } catch (IOException e) {
            String msg = String.format("拷贝Dockerfile:[%s]到目录:[%s],出现错误,错误详细内容为:[%s]", dockerFile, arfifactDirRef.getPath(), e.getMessage());
            throw new RuntimeException(msg);
        }
    }

    protected Set<String> processTags(String tags, Map<String, Object> ctx) {
        Set<String> resultTags = new HashSet<>(0);
        if (null != tags) {
            String[] tagsArray = tags.split(",");
            for (String tag : tagsArray) {
                String newTag = expression(tag, ctx);
                resultTags.add(newTag);
            }
        }
        return resultTags;
    }

    protected List<DockerBuildArg> processArgs(List<DockerBuildArg> args, Map<String, Object> ctx) {
        List<DockerBuildArg> resultArgs = new ArrayList<>(args.size());
        if (!args.isEmpty()) {
            for (DockerBuildArg arg : args) {
                String key = expression(arg.getKey(), ctx);
                String value = expression(arg.getValue(), ctx);

                DockerBuildArg dockerBuildArg = new DockerBuildArg();
                dockerBuildArg.setKey(key);
                dockerBuildArg.setValue(value);
                resultArgs.add(dockerBuildArg);
            }
        }
        return resultArgs;
    }


    protected String getArfifactDir(DockerParams actionParams, Map<String, Object> ctx) {
        String result = actionParams.getArfifactDir();
        if (null == result) {
            result = (String) ctx.get(Constant.Artifact.ARTIFACT_DIR);
        }
        // 处理表达式
        if (null != result) {
            result = expression(result, ctx);
        }
        return result;
    }

    protected String expression(String template, Map<String, Object> ctx) {
        return dockerFaceService.getExpressionService().prase(template, ctx);
    }

    @Override
    public String name() {
        return DOCKER_BUILD_IMAGE_ACTION;
    }
}
