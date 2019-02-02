package com.kiran.sling.models;

import java.util.Calendar;

/**
 * The Package Info Model to set the Basic Packages Info.
 *
 * @author Kiran. Created on 2nd Feb. 2019.
 */
public class PackageInfoModel {

    private String packageName;

    private String packageVersion;

    private Calendar lastInstalled;

    private String packagePath;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(final String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public Calendar getLastInstalled() {
        return lastInstalled;
    }

    public void setLastInstalled(final Calendar lastInstalled) {
        this.lastInstalled = lastInstalled;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(final String packagePath) {
        this.packagePath = packagePath;
    }
}
