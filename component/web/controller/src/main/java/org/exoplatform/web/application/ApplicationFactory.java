package org.exoplatform.web.application;

public interface ApplicationFactory<A extends Application, C> {
    A createApplication(C config);
}
