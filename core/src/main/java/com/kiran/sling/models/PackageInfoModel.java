package com.kiran.sling.models;

import java.util.Calendar;

/**
 * The Package Info Model to set the Basic Packages Info.
 *
 * @author Kiran. Created on 2nd Feb. 2019.
 */
public class PackageInfoModel {

    /** The Name of the Package. */
    private String packageName;

    /** The Package Version. */
    private String packageVersion;

    /** The Last Installed of the Package. */
    private Calendar lastInstalled;

    /** The Path of the Package in the JCR. */
    private String packagePath;

    /**
     * Gets the name of the package.
     *
     * @return packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the Name of the Package.
     *
     * @param packageName the name of the package.
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the Version of the Package.
     *
     * @return packageVersion
     */
    public String getPackageVersion() {
        return packageVersion;
    }

    /**
     * Sets the Version of the Package.
     *
     * @param packageVersion the Version of the Package.
     */
    public void setPackageVersion(final String packageVersion) {
        this.packageVersion = packageVersion;
    }

    /**
     * Gets the Last Installed of the Package.
     *
     * @return lastInstalled the last installed of the package.
     */
    public Calendar getLastInstalled() {
        return lastInstalled;
    }

    /**
     * Sets the Last Installed of the Package.
     *
     * @param lastInstalled the Last Installed of the Package.
     */
    public void setLastInstalled(final Calendar lastInstalled) {
        this.lastInstalled = lastInstalled;
    }

    /**
     * Gets the Package Path.
     *
     * @return the JCR path of the Package.
     */
    public String getPackagePath() {
        return packagePath;
    }

    /**
     * Sets the Path of the Package.
     *
     * @param packagePath the path of the package.
     */
    public void setPackagePath(final String packagePath) {
        this.packagePath = packagePath;
    }
}
