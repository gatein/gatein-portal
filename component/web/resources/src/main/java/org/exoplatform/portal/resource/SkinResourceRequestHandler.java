package org.exoplatform.portal.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.exoplatform.commons.utils.BinaryOutput;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.controller.resource.ResourceRequestHandler;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SkinResourceRequestHandler extends WebRequestHandler {

    /** . */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** . */
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /** . */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /** . */
    public static final String LAST_MODIFIED = "Last-Modified";

    /** . */
    private final SkinService skinService;

    public SkinResourceRequestHandler(SkinService skinService) {
        this.skinService = skinService;
    }

    @Override
    public String getHandlerName() {
        return "skin";
    }

    @Override
    public boolean execute(final ControllerContext context) throws Exception {
        String compressParam = context.getParameter(ResourceRequestHandler.COMPRESS_QN);
        boolean compress = "min".equals(compressParam);

        //
        final HttpServletResponse response = context.getResponse();

        // Check if cached resource has not been modifed, return 304 code
        long ifModifiedSince = context.getRequest().getDateHeader(IF_MODIFIED_SINCE);
        long cssLastModified = skinService.getLastModified(context);
        if (isNotModified(ifModifiedSince, cssLastModified)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        } else {
            //
            response.setContentType("text/css; charset=UTF-8");

            final OutputStream out = response.getOutputStream();
            final BinaryOutput output = new BinaryOutput() {
                public Charset getCharset() {
                    return UTF_8;
                }

                public void write(byte b) throws IOException {
                    out.write(b);
                }

                public void write(byte[] bytes) throws IOException {
                    out.write(bytes);
                }

                public void write(byte[] bytes, int off, int len) throws IOException {
                    out.write(bytes, off, len);
                }
            };
            ResourceRenderer renderer = new ResourceRenderer() {
                public BinaryOutput getOutput() {
                    return output;
                }

                public void setExpiration(long seconds) {
                    if (seconds > 0) {
                        response.addHeader("Cache-Control", "max-age=" + seconds + ",s-maxage=" + seconds);
                    } else {
                        response.setHeader("Cache-Control", "no-cache");
                    }

                    long lastModified = skinService.getLastModified(context);
                    response.setDateHeader(LAST_MODIFIED, lastModified);
                }
            };

            //
            final String resource = "/" + context.getParameter(ResourceRequestHandler.RESOURCE_QN) + ".css";
            try {
                if (skinService.renderCSS(context, renderer, compress)) {
                    // Ok we did the job
                    return true;
                } else {
                    log.warn("CSS " + resource + " not found");
                    return false;
                }
            } catch (Exception e) {
                if (e instanceof SocketException) {
                    // Should we print something/somewhere exception message
                } else {
                    // We want to ignore the ClientAbortException since this is caused by the users
                    // browser closing the connection and is not something we should be logging.
                    if(e.getClass().toString().contains("ClientAbortException")) {
                        return true;
                    }
                    log.error("Could not render css " + resource, e);
                }
                return false;
            }
        }
    }

    /**
     * If cached resource has not changed since date in http header (If_Modified_Since), return true otherwise return false.
     */
    private boolean isNotModified(long ifModifedSince, long lastModified) {
        if (!PropertyManager.isDevelopping()) {
            if (ifModifedSince >= lastModified) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean getRequiresLifeCycle() {
        return false;
    }
}
