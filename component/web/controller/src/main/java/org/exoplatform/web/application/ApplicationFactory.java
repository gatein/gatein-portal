package org.exoplatform.web.application;

import org.exoplatform.web.application.Application;

public interface ApplicationFactory<A extends Application, C>
{
   A createApplication(C config);
}
