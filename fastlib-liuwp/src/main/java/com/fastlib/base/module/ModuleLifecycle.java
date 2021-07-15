package com.fastlib.base.module;

/**
 * Created by sgfb on 18/7/17.
 * 模块生命周期
 */
public interface ModuleLifecycle {

    void created();

    void destroyed();
}
