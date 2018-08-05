package com.hzyi.jplab.dum;

import com.hzyi.jplab.core.model.Assembly;
import com.hzyi.jplab.core.solver.Solver;
import com.hzyi.jplab.core.controller.Controller;
import com.hzyi.jplab.core.application.BaseApplication;

public class DumApplication extends BaseApplication {

  public static void main(String[] args) {
  	launch(args);
  }

  protected String initializeApplicationName() {
    return "Dum Application";
  }

  protected Assembly initializeAssembly() {
    return null;
  }

  protected Solver initializeSolver() {
    return null;
  }

  protected Controller initializeController() {
    return null;
  }
}