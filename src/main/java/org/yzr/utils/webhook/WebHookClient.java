package org.yzr.utils.webhook;

import org.yzr.model.App;
import org.yzr.model.WebHook;
import org.yzr.utils.file.PathManager;

import java.util.HashMap;
import java.util.Map;

public class WebHookClient {

    static Map<String, Object> webHookList;

    static {
        webHookList = new HashMap<>();
    }

    /**
     *
     * @param app
     * @param pathManager
     */
    public static void sendMessage(App app, PathManager pathManager) {
        if (app.getWebHookList() == null || app.getWebHookList().size() < 1) {
            return;
        }

        for (WebHook webHook :
                app.getWebHookList()) {
            String webHookType = webHook.getType();
            IWebHook iWebHook = getWebHook(webHookType);
            if (iWebHook != null) {
                iWebHook.sendMessage(app, pathManager);
            }
        }
    }


    /**
     *
     * @param webHookType
     * @return
     */
    private static IWebHook getWebHook(String webHookType) {
        if (webHookType == null || webHookType.length() < 1) {
            return null;
        }
        IWebHook iWebHook = (IWebHook) webHookList.get(webHookType);
        if (iWebHook != null) {
            return iWebHook;
        }

        try {
            //
            Class aClass = Class.forName("org.yzr.utils.webhook." + webHookType +"WebHook");
            iWebHook = (IWebHook) aClass.newInstance();
            webHookList.put(webHookType, iWebHook);
            return iWebHook;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
