package help.lixin.core.pipeline.ctx.impl;

import help.lixin.core.pipeline.ctx.PipelineContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Global级别的上下文.
 */
public class GlobalPipelineContext implements PipelineContext {
    private final Map<String, Object> vars = new HashMap<>();

    @Override
    public void addVar(String varName, Object varValue) {
        vars.put(varName, varValue);
    }

    @Override
    public Object getVar(String varName) {
        return vars.get(varName);
    }

    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    @Override
    public String toString() {
        return "GlobalPipelineContext{" +
                "vars=" + vars +
                '}';
    }
}
