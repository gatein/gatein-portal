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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Implementation of portal-setup.sh script.
 * This class ask for a first user root password for GateIn portal.
 * Encoded it and stored in configuration.properties under key gatein.portal.setup.initialpassword.root
 *
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 *
 */
public class PortalSetupCommand {

    public static final String CONF_JBOSS = "../standalone/configuration/gatein/configuration.properties";
    public static final String CONF_TOMCAT = "../gatein/conf/configuration.properties";
    public static final String TOMCAT_SCRIPT = "catalina.sh";

    /**
     * @param args
     */
    public static void main(String[] args) {

        System.out.print("Setting root password: ");
        String password1 = new String(System.console().readPassword());
        System.out.print("\nRepeat root password: ");
        String password2 = new String(System.console().readPassword());

        if (!password1.equals(password2)) {
            System.out.println("ERROR passwords are not equals !");
            System.exit(1);
        }

        System.out.println("Creating password...");
        String encoded = encodePassword(password2);
        // Reading properties
        Properties props = new Properties();
        boolean isTomcat = false;
        File fCheck= new File(TOMCAT_SCRIPT);
        File f = null;
        if (!fCheck.exists()) {
            File f_jboss = new File(CONF_JBOSS);
            if (!f_jboss.exists()) {
                System.out.println("ERROR " + CONF_JBOSS + " doesn't exist !");
                System.exit(1);
            }
            f = f_jboss;
        } else {
            File f_tomcat = new File(CONF_TOMCAT);
            if (!f_tomcat.exists()) {
                System.out.println("ERROR " + CONF_TOMCAT + " doesn't exist !");
                System.exit(1);
            }
            f = f_tomcat;
            isTomcat = true;
        }

        // Check if property is present in file
        boolean pPresent = false;
        try {
            props.load(new FileInputStream( f ));
            if (props.getProperty(PortalSetupService.ROOT_PASSWORD_PROPERTY) != null) pPresent = true;
        } catch (IOException e) {
            if (!isTomcat)
                System.out.println("ERROR problem reading " + CONF_JBOSS + " file");
            else
                System.out.println("ERROR problem reading " + CONF_TOMCAT + " file");
            System.exit(1);
        }

        // Saving properties in raw to preserve comments and layout
        try {
            // Property is not in file - appending new property
            if (!pPresent) {
                FileWriter fw = new FileWriter(f, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("# Modified by portal-setup.sh\n");
                bw.write(PortalSetupService.ROOT_PASSWORD_PROPERTY + "=" + encoded);
                bw.close();
            } else {
                // Reading file by line
                StringBuffer sb = new StringBuffer();
                FileInputStream fs = new FileInputStream(f);
                InputStreamReader is = new InputStreamReader(fs);
                BufferedReader br = new BufferedReader(is);
                String line;
                while(true) {
                    line = br.readLine();
                    if (line == null) break;
                    // Check property and changes it in file
                    if (line.contains(PortalSetupService.ROOT_PASSWORD_PROPERTY)) {
                        line = PortalSetupService.ROOT_PASSWORD_PROPERTY + "=" + encoded;
                    }
                    sb.append(line + "<<MARK>>");
                }
                fs.close();
                is.close();
                br.close();
                // Writing modified file
                FileWriter fw = new FileWriter(f);
                BufferedWriter bw = new BufferedWriter(fw);
                for (String l : sb.toString().split("<<MARK>>")) {
                    bw.write(l + "\n");
                }
                bw.close();
            }
            // Finish
            if (!isTomcat)
                System.out.println(CONF_JBOSS + " file updated !");
            else
                System.out.println(CONF_TOMCAT + " file updated !");
            System.out.println(PortalSetupService.ROOT_PASSWORD_PROPERTY + "=" + encoded);
        } catch (IOException e) {
            if (!isTomcat)
                System.out.println("ERROR problem writting " + CONF_JBOSS + " file");
            else
                System.out.println("ERROR problem writting " + CONF_TOMCAT + " file");
        }
    }

    public static String encodePassword(String decodedPassword) {

        if (decodedPassword == null)
            return null;

        String encodedPassword = null;
        try {
            byte[] salt = PortalSetupService.SALT.substring(0, 8).getBytes();
            int count = PortalSetupService.COUNT;
            char[] password = PortalSetupService.KEY.toCharArray();
            PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
            PBEKeySpec keySpec = new PBEKeySpec(password);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
            SecretKey cipherKey = factory.generateSecret(keySpec);
            encodedPassword = PBEUtils.encode64(decodedPassword.getBytes("UTF-8"), "PBEwithMD5andDES", cipherKey, cipherSpec);
        } catch (Exception e) {
            System.out.println("ERROR: root password can not be decoded!");
        }
        return encodedPassword;
    }

}
