package abc;

import javax.inject.Inject;

import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.template.Template;

/**
 * @author Julien Viet
 */
public class Controller {

    @Inject
    @Path("index.gtmpl")
    Template index;

    @View
    public Response.Content index() {
        return index.ok();
    }
}
