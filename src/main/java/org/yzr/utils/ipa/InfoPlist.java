package org.yzr.utils.ipa;

import java.util.List;

public class InfoPlist {

    private Plist plist;

    private String appName;

    private String version;

    private String buildVersion;

    private String bundleID;

    private String minimumOSVersion;

    private String iconName;

    public InfoPlist(Plist plist) {
        this.plist = plist;
        //
        String appName = this.plist.stringValueForKeyPath("CFBundleDisplayName");
        if (appName == null) {
            appName = this.plist.stringValueForKeyPath("CFBundleName");
        }
        this.appName = appName;
        //
        String version = this.plist.stringValueForPath("CFBundleShortVersionString");
        this.version = version;
        //
        String buildVersion = this.plist.stringValueForPath("CFBundleVersion");
        this.buildVersion = buildVersion;
        //
        String bundleID = this.plist.stringValueForPath("CFBundleIdentifier");
        this.bundleID = bundleID;
        //
        String minimumOSVersion = this.plist.stringValueForPath("MinimumOSVersion");
        this.minimumOSVersion = minimumOSVersion;
        //
        String iconName = this.plist.stringValueForPath("CFBundleIcons.CFBundlePrimaryIcon.CFBundleIconName");
        if (iconName == null) {
            iconName = this.plist.stringValueForKeyPath("CFBundleIconFile");
        }
        if (iconName == null) {
            List<String> iconFiles = this.plist.arrayValueForPath("CFBundleIcons.CFBundlePrimaryIcon.CFBundleIconFiles");
            if (iconFiles != null && iconFiles.size() > 0) {
                iconName = iconFiles.get(iconFiles.size() - 1);
            }
        }
        this.iconName = iconName;
    }


    public String getAppName() {
        return appName;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getBundleID() {
        return bundleID;
    }

    public String getMinimumOSVersion() {
        return minimumOSVersion;
    }

    public String getIconName() {
        return iconName;
    }
}
