package help.lixin.shell.service;

import help.lixin.core.pipeline.service.IExpressionService;

public class ShellFaceService {

    private IExpressionService expressionService;

    public ShellFaceService(IExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public IExpressionService getExpressionService() {
        return expressionService;
    }
}
