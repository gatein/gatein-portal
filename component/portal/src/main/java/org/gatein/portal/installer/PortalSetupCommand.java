/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.portal.installer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

/**
 * Implementation of portal-setup.sh script. Either asks for the initial root password in the console or accepts the provided -p
 * &lt;password&gt; command line parameter. The password is encoded and stored in configuration.properties file provided in -f
 * command line parameter.
 *
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 *
 */
public class PortalSetupCommand {

    /**
     * Exception thrown by methods of {@link PortalSetupCommand}. Handlers are expected to call
     * {@link PortalSetupCommand#usage(Throwable)}.
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     *
     */
    public static class SetupCommandException extends Exception {

        private static final long serialVersionUID = 2766753482448227104L;

        /**
         * @param message
         */
        public SetupCommandException(String message) {
            super(message);
        }

        /**
         * @param message
         * @param cause
         */
        public SetupCommandException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * @param cause
         */
        public SetupCommandException(Throwable cause) {
            super(cause);
        }

    }

    /**
     * The command line option -h (Help).
     */
    public static final String HELP_OPTION = "-h";

    /**
     * The command line option -p (Password).
     */
    public static final String PASSWORD_OPTION = "-p";

    /**
     * The command line option -f (Properties File Path).
     */
    public static final String PROPERTIES_FILE_OPTION = "-f";

    /**
     * See {@link Properties#load(java.io.InputStream)}.
     */
    public static final String PROPERTIES_STANDARD_ENCODING = "iso-8859-1";

    /**
     * The name of the wrapper script.
     */
    public static final String SCRIPT_NAME = "portal-setup";

    /**
     * We do not consider logical lines spread over more than one physical line here - see
     * {@link Properties#load(java.io.Reader)}.
     *
     * @param line
     * @return
     */
    private static boolean containsRootPasswordProperty(String line) {
        int offset = 0;
        /* ignorable whitespace at the beginning */
        WHILE: while (offset < line.length()) {
            switch (line.charAt(offset)) {
                case ' ':
                case '\t':
                case '\f':
                    offset++;
                    break;
                default:
                    break WHILE;
            }
        }
        /* Try to match propName */
        String propName = PortalSetupService.ROOT_PASSWORD_PROPERTY;
        for (int i = 0; i < propName.length(); i++) {
            if (offset >= line.length() || propName.charAt(i) != line.charAt(offset++)) {
                return false;
            }
        }
        /*
         * propName matched what is next?
         */
        if (offset == line.length()) {
            /* no property value */
            return true;
        } else {
            switch (line.charAt(offset)) {
                case ' ':
                case '\t':
                case '\f':
                case '=':
                case ':':
                    /* end of property name */
                    return true;
                default:
                    /* no end of property name */
                    return false;
            }
        }
    }

    /**
     * Main method. See {@link PortalSetupCommand}.
     *
     * @param args
     */
    public static void main(String[] args) {

        PortalSetupCommand cmd = new PortalSetupCommand();
        try {
            cmd.parseArgs(args);
            cmd.validate();
            cmd.process();
        } catch (SetupCommandException e) {
            cmd.usage(e);
        }

    }

    /**
     * Holds root's plain text password.
     */
    private String password;

    /**
     * Portal property file path.
     */
    private File propertiesFile;

    /**
     * A public constructor.
     */
    public PortalSetupCommand() {
    }

    /**
     * Parses command line arguments and complains if necessary.
     *
     * @param args
     */
    public void parseArgs(String[] args) throws SetupCommandException {

        if (args != null) {
            if (args.length < 2) {
                throw new SetupCommandException("Missing parameter " + PROPERTIES_FILE_OPTION + ".");
            } else {
                for (int i = 0; i < args.length;) {
                    String arg = args[i++];
                    if (PASSWORD_OPTION.equals(arg)) {
                        if (i >= args.length) {
                            throw new SetupCommandException("Password expected after " + PASSWORD_OPTION + ".");
                        } else {
                            this.password = args[i++];
                        }
                    } else if (PROPERTIES_FILE_OPTION.equals(arg)) {
                        if (i >= args.length) {
                            throw new SetupCommandException("Properties file path expected after " + PROPERTIES_FILE_OPTION
                                    + ".");
                        } else {
                            this.propertiesFile = new File(args[i++]);
                        }
                    } else if (HELP_OPTION.equals(arg)) {
                        throw new SetupCommandException("");
                    } else {
                        throw new SetupCommandException("Unrecognized parameter '" + arg + "'.");
                    }
                }
            }
        }

    }

    /**
     * Writes the changes to {@link #propertiesFile}.
     *
     * @throws SetupCommandException
     *
     */
    public void process() throws SetupCommandException {

        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(propertiesFile);
            props.load(in);
        } catch (FileNotFoundException e) {
            throw new SetupCommandException("File does not exist: '" + propertiesFile.getAbsolutePath() + "'", e);
        } catch (IOException e) {
            throw new SetupCommandException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (props.containsKey(PortalSetupService.ROOT_PASSWORD_PROPERTY)) {
            /* edit the file */
            BufferedReader r = null;

            OutputStream out = null;
            BufferedWriter w = null;
            File propertiesFileBackup = new File(propertiesFile + ".backup");
            try {

                /* rename the original file */
                propertiesFile.renameTo(propertiesFileBackup);

                in = new FileInputStream(propertiesFileBackup);
                r = new BufferedReader(new InputStreamReader(in, PROPERTIES_STANDARD_ENCODING));

                out = new FileOutputStream(propertiesFile);
                w = new BufferedWriter(new OutputStreamWriter(out, PROPERTIES_STANDARD_ENCODING));

                String line = null;
                while ((line = r.readLine()) != null) {
                    if (containsRootPasswordProperty(line)) {
                        writeRootPasswordLine(w);
                    } else {
                        w.write(line);
                        w.write('\n');
                    }
                }

            } catch (FileNotFoundException e) {
                throw new SetupCommandException("File does not exist: '" + propertiesFile.getAbsolutePath() + "'", e);
            } catch (UnsupportedEncodingException e) {
                /* Shoud never happen with PROPERTIES_STANDARD_ENCODING = "iso-8859-1" */
                throw new SetupCommandException(e.getMessage(), e);
            } catch (IOException e) {
                throw new SetupCommandException(e.getMessage(), e);
            } finally {
                if (w != null) {
                    try {
                        w.close();
                    } catch (IOException e) {
                        throw new SetupCommandException(e.getMessage(), e);
                    }
                }
                if (r != null) {
                    try {
                        r.close();
                        propertiesFileBackup.delete();
                    } catch (IOException e) {
                        throw new SetupCommandException(e.getMessage(), e);
                    }
                }
            }

        } else {
            /* append */
            OutputStream out = null;
            Writer w = null;
            try {
                out = new FileOutputStream(propertiesFile, true);
                w = new OutputStreamWriter(out, PROPERTIES_STANDARD_ENCODING);
                writeRootPasswordLine(w);
            } catch (FileNotFoundException e) {
                throw new SetupCommandException("File does not exist: '" + propertiesFile.getAbsolutePath() + "'", e);
            } catch (UnsupportedEncodingException e) {
                /* Shoud never happen with PROPERTIES_STANDARD_ENCODING = "iso-8859-1" */
                throw new SetupCommandException(e.getMessage(), e);
            } catch (IOException e) {
                throw new SetupCommandException(e.getMessage(), e);
            } finally {
                if (w != null) {
                    try {
                        w.close();
                    } catch (IOException e) {
                        throw new SetupCommandException(e.getMessage(), e);
                    }
                }
            }
        }

    }

    /**
     * Writes the root password line to {@link #propertiesFile}.
     *
     * @param w
     * @throws SetupCommandException
     * @throws IOException
     */
    private void writeRootPasswordLine(Writer w) throws SetupCommandException, IOException {
        try {
            String encodedPassword = PortalSetupService.encodePassword(password);
            w.write('\n');
            w.write(PortalSetupService.ROOT_PASSWORD_PROPERTY);
            w.write('=');
            w.write(encodedPassword);
            w.write(" # Modified by ");
            w.write(SCRIPT_NAME);
            w.write(".sh\n");
        } catch (NoSuchAlgorithmException e) {
            throw new PortalSetupCommand.SetupCommandException(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            throw new PortalSetupCommand.SetupCommandException(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new PortalSetupCommand.SetupCommandException(e.getMessage(), e);
        } catch (Exception e) {
            throw new PortalSetupCommand.SetupCommandException(e.getMessage(), e);
        }
    }

    /**
     * Prints an error message if there is any and command usage to stderr.
     *
     * @param string
     */
    public void usage(Throwable error) {
        if (error != null) {
            String msg = error.getMessage();
            if (msg != null && msg.length() > 0) {
                System.err.println("Error: " + msg);
            }
        }
        System.err.println("================================================================================");
        System.err.println(SCRIPT_NAME + "[ .sh | .bat ] - a utility for setting an initial password for");
        System.err.println("                portal user 'root'.");
        System.err.println();
        System.err.println("Usage: " + SCRIPT_NAME + "[ .sh | .bat ] [ " + PROPERTIES_FILE_OPTION + " <path> ] [ "
                + PASSWORD_OPTION + " <password> ] [ " + HELP_OPTION + " ]");
        System.err.println();
        System.err
                .println("    " + PROPERTIES_FILE_OPTION + " <path> - path to portal properties file. If not set explicitly,");
        System.err.println("                the default path for the given platform will be used.");
        System.err.println();
        System.err.println("    " + PASSWORD_OPTION
                + " <password> - the password that will be appended to properties file given");
        System.err.println("                in " + PROPERTIES_FILE_OPTION + ".");
        System.err.println();
        System.err.println("    " + HELP_OPTION + " - Print these usage instructions and exit.");
        System.err.println();
    }

    /**
     * Validates the parameters and asks the user for missing information on the console if necessary.
     *
     * @throws IOException
     * @throws FileNotFoundException
     *
     */
    public void validate() throws SetupCommandException {
        if (propertiesFile == null) {
            throw new SetupCommandException("Missing parameter " + PROPERTIES_FILE_OPTION + ".");
        } else if (!propertiesFile.exists()) {
            throw new SetupCommandException("File does not exist: '" + propertiesFile.getAbsolutePath() + "'");
        }

        if (password == null) {
            System.out.print("Set root password: ");
            password = new String(System.console().readPassword());
            System.out.print("Repeat root password: ");
            String password2 = new String(System.console().readPassword());

            if (!password.equals(password2)) {
                throw new SetupCommandException("Passwords are not equal.");
            }
        }

    }

}
