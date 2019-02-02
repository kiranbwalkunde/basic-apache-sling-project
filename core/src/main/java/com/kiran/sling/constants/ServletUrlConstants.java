package com.kiran.sling.constants;

/**
 * The Class for the Servlets Paths.
 *
 * @author Kiran. Created on 02nd Feb. 2019.
 */
public final class ServletUrlConstants {

    /** The Private Constructor to prevent the Instantiation of the class object. */
    private ServletUrlConstants() {}

    /** The Servlet URL for the Latest Files. */
    public static final String GET_LATEST_FILES = "/bin/services/sadashiv/private/packages";

    /** The Servlet URL for the JCR Nodes Backup. */
    public static final String GET_JCR_NODES_BACKUP = "/bin/services/sadashiv/private/backup-paths";
}
