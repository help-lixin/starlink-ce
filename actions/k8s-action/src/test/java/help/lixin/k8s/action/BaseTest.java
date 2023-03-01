package help.lixin.k8s.action;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

public abstract class BaseTest {

    protected KubernetesClient client = null;

    private static final String KUBE_CONFIG = "/Users/lixin/.kube/config";

    @Before
    public void init() throws Exception {
        String kubeconfigContents = IOUtils.toString(new FileInputStream(KUBE_CONFIG), "UTF-8");
        Config config = Config.fromKubeconfig(kubeconfigContents);
        client = new DefaultKubernetesClient(config);
    }

    protected String getToken() throws Exception {
        String tokenPath = "/Users/lixin/harbor-token.txt";
        // 有这么严重的Bug吗?读取文件时,最后附加上一个回车符.
        String token = FileUtils.readFileToString(new File(tokenPath), Charset.forName("ISO-8859-1"));
        token = token.substring(0, token.length() - 1);
        return token;
    }
}
