package com.ixnah.app.las.clangd;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.project.Project;
import com.ixnah.app.las.transform.TransformSupport;
import com.ixnah.app.las.util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClangdSupport {

    private ClangdSupport() {
        throw new UnsupportedOperationException();
    }

    public static void load(String osArch) {
        // TODO: clion用的魔改版clangd没法直接替换
//        TransformSupport.getTransformPipeHandler().add(new ClangdTransformer());
    }

    // TODO: com.jetbrains.cidr.lang.daemon.clang.clangd.lsp.server.ClangServer 适配
    public static GeneralCommandLine processCmdArgs(Object context, GeneralCommandLine commandLine) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LogUtil.d(commandLine.toString());
        // TODO: 处理参数
//        ParametersList parametersList = commandLine.getParametersList();
//        parametersList.getList().forEach(params -> {
//            String[] split = params.split("=", 2);
//            if (split.length >= 1) {
//                switch (split[0]) {
//                    case "--clion-mode", "-update-debounce", "-clion-static-index", "-clion-dynamic-index",
//                            "-include-ineligible-results", "-keep-asts", "-clion-extra-completion-preamble",
//                            "-clion-keep-obsolete-ast": {
//                        parametersList.replaceOrAppend(params, "");
//                        break;
//                    }
//                    case "-index": {
//                        parametersList.replaceOrAppend(params, "-index=" + (Boolean.getBoolean(split[1]) ? 1 : 0));
//                        break;
//                    }
//                    case "-recovery-ast": {
//                        parametersList.replaceOrAppend(params, "--recovery-ast");
//                        break;
//                    }
//                }
//            }
//        });
        Method getProject = context.getClass().getMethod("getProject");
        Project project = (Project) getProject.invoke(context);
        GeneralCommandLine result = new GeneralCommandLine();
        result.withExePath(commandLine.getExePath()).withWorkDirectory(project.getBasePath());
        return result;
    }
}
