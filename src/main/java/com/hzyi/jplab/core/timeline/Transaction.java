package com.hzyi.jplab.core.timeline;

import com.google.common.base.Preconditions;
import com.hzyi.jplab.core.model.Assembly;
import java.util.function.Function;

public class Transaction {

  private Runnable once =
      new Runnable() {

        boolean run = false;

        @Override
        public void run() {
          Preconditions.checkState(!run, "transaction can only be run once");
          run = true;
        }
      };

  private Verifier verifier;

  Assembly run(Assembly snapshot, Function<Assembly, Assembly> runner) {
    once.run();
    Assembly finishing = runner.apply(snapshot);
    if (verifier == null) {
      return finishing;
    }
    if (verifier.verify(snapshot, finishing)) {
      return finishing;
    }
    finishing = verifier.onFinish(runner.apply(verifier.onStart(snapshot)));
    if (verifier.newVerifier().verify(snapshot, finishing)) {
      return finishing;
    }
    throw new IllegalStateException("transaction failed");
  }

  void withVerifier(Verifier verifier) {
    if (verifier != null) {
      this.verifier = verifier;
    } else {
      throw new UnsupportedOperationException("multiple verifiers unsupported");
    }
  }
}
