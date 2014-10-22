/**
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.applicationregistry.webui.component;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.Source;
import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.webui.application.GadgetUtil;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.Param;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.ResourceValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.Validator;

/**
 * Created by The eXo Platform SAS Author : Pham Thanh Tung thanhtungty@gmail.com Jul 29, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", initParams = @ParamConfig(name = "SampleGadget", value = "app:/WEB-INF/conf/uiconf/applicationregistry/component/SampleGadget.groovy"), events = {
        @EventConfig(listeners = UIGadgetEditor.SaveActionListener.class),
        @EventConfig(listeners = UIGadgetEditor.CancelActionListener.class, phase = Phase.DECODE) })
@Serialized
public class UIGadgetEditor extends UIForm {

    public static final String FIELD_SOURCE = "source";
    public static final String FIELD_NAME = "name";

    private Source source_;

    private String fullName_;

    private String dirPath;

    public UIGadgetEditor(InitParams initParams) throws Exception {
        Param param = initParams.getParam("SampleGadget");
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        String sample = (String) param.getMapGroovyObject(context);
        addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(MandatoryValidator.class)
                .addValidator(StringLengthValidator.class, 2, 50).addValidator(ResourceValidator.class)
                .addValidator(ExpressionValidator.class, "^[\\p{L}][\\p{L}._\\-\\d]+$", "UIGadgetEditor.msg.Invalid"));
        addUIFormInput(new UIFormTextAreaInput(FIELD_SOURCE, FIELD_SOURCE, sample).addValidator(MandatoryValidator.class)
                .addValidator(GadgetSpecValidator.class));
    }

    public Source getSource() {
        return source_;
    }

    public void setSource(Source source) throws Exception {
        source_ = source;
        fullName_ = source_.getName();
        UIFormTextAreaInput uiInputSource = getUIFormTextAreaInput(FIELD_SOURCE);
        uiInputSource.setValue(source_.getTextContent());
    }

    public void setGadgetName(String name) {
        UIFormStringInput uiInputName = getUIStringInput(FIELD_NAME);
        uiInputName.setValue(name);
    }

    public String getSourceFullName() {
        return fullName_;
    }

    public String getSourceName() {
        return (fullName_ != null) ? extractName(fullName_) : null;
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        UIFormTextAreaInput uiInputSource = getUIFormTextAreaInput(FIELD_SOURCE);
        UIFormStringInput uiInputName = getUIStringInput(FIELD_NAME);
        uiInputSource.setValue(uiInputSource.getValue());
        if (this.isEdit()) {
            uiInputName.setReadOnly(true);
        }

        super.processRender(context);
    }

    private String extractName(String fullName) {
        int idx = fullName.indexOf('.');
        return (idx > 0) ? fullName.substring(0, idx) : fullName;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getDirPath() {
        return dirPath;
    }

    public static class SaveActionListener extends EventListener<UIGadgetEditor> {

        public void execute(Event<UIGadgetEditor> event) throws Exception {
            UIGadgetEditor uiForm = event.getSource();
            UIGadgetManagement uiManagement = uiForm.getParent();
            String gadgetName;
            String text = uiForm.getUIFormTextAreaInput(UIGadgetEditor.FIELD_SOURCE).getValue();
            GadgetRegistryService service = uiForm.getApplicationComponent(GadgetRegistryService.class);
            SourceStorage sourceStorage = uiForm.getApplicationComponent(SourceStorage.class);
            boolean isEdit = uiForm.isEdit();
            if (isEdit) {
                gadgetName = uiForm.getSourceFullName();
            } else {
                gadgetName = uiForm.getUIStringInput(UIGadgetEditor.FIELD_NAME).getValue();
            }

            //
            Gadget gadget = service.getGadget(gadgetName);

            if (isEdit) {
                if (gadget == null) {
                    UIApplication uiApp = event.getRequestContext().getUIApplication();
                    uiApp.addMessage(new ApplicationMessage("gadget.msg.changeNotExist", null, ApplicationMessage.WARNING));
                    uiManagement.reload();
                    return;
                }
            } else {
                // If gadget is null we need to create it first
                if (gadget == null) {
                    gadget = new Gadget();
                    gadget.setName(gadgetName);

                    // Those data will be taken from the gadget XML anyway
                    gadget.setDescription("");
                    gadget.setThumbnail("");
                    gadget.setLocal(true);
                    gadget.setTitle("");
                    gadget.setReferenceUrl("");

                    // Save gadget with empty data first
                    service.saveGadget(gadget);
                } else {
                    UIApplication uiApp = event.getRequestContext().getUIApplication();
                    uiApp.addMessage(new ApplicationMessage("UIGadgetEditor.gadget.msg.gadgetIsExist", null,
                            ApplicationMessage.WARNING));
                    return;
                }
            }
            //
            Source source = new Source(gadgetName, "application/xml");
            source.setTextContent(text);
            source.setLastModified(Calendar.getInstance());

            try {
                sourceStorage.saveSource(gadget, source);
                uiManagement.removeChild(UIGadgetEditor.class);
                // This will update the source and also update the gadget related
                // cached meta data
                // from the source
                uiManagement.setSelectedGadget(gadget.getName());
                event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);

                // Send request to invalidate the cache to Shindig
                String gadgetServerUrl = GadgetUtil.getGadgetServerUrl();
                String gadgetUrl = GadgetUtil.reproduceUrl(gadget.getUrl(), gadget.isLocal());
                String metadataUrl = gadgetServerUrl + (gadgetServerUrl.endsWith("/") ? "" : "/") + "metadata";
                String queryString = "{\"context\":{\"ignoreCache\":\"true\"},\"gadgets\":[" + "{\"url\":\"" + gadgetUrl
                        + "\"}]}";
                event.getRequestContext().getJavascriptManager().require("SHARED/base")
                        .addScripts("ajaxRequest('POST', '" + metadataUrl + "', true, '" + queryString + "');");
            } catch (UnsupportedEncodingException e) {
                UIApplication uiApp = event.getRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIGadgetEditor.msg.unsupportedEncoding",
                        new Object[] { e.getMessage() }, ApplicationMessage.ERROR));
                return;
            }
        }

    }

    private boolean isEdit() {
        return (this.getSource() != null);
    }

    public static class CancelActionListener extends EventListener<UIGadgetEditor> {

        public void execute(Event<UIGadgetEditor> event) throws Exception {
            UIGadgetEditor uiForm = event.getSource();
            UIGadgetManagement uiManagement = uiForm.getParent();
            Gadget selectedGadget = uiManagement.getSelectedGadget();
            if (selectedGadget != null) {
                uiManagement.setSelectedGadget(selectedGadget.getName());
            } else
                uiManagement.reload();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
        }

    }

    public static class GadgetSpecValidator implements Validator, Serializable {

        public void validate(UIFormInput uiInput) throws Exception {
            try {
                new GadgetSpec(Uri.parse("http://exoplatform.org"), (String) uiInput.getValue());
            } catch (Exception se) {
                throw new MessageException(new ApplicationMessage("UIGadgetEditor.msg.invalidSpec", null,
                        ApplicationMessage.WARNING));
            }
        }

    }
}
