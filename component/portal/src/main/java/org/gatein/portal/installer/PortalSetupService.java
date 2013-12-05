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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * This class is responsible to check a flag in JCR and memory for proper GateIn root password setup.
 *
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 *
 */
public class PortalSetupService {

    public static final String GATEIN_SETUP_ENABLE = "gatein.portal.setup.enable";
    public static final String ROOT_PASSWORD_PROPERTY = "gatein.portal.setup.initialpassword.root";
    public static final String REPOSITORY_NAME = "repository";
    public static final String WORKSPACE_NAME = "portal-system";
    public static final String SETUP_FLAG = "gatein-setup-flag";
    public static final String SALT = "unodostrescuatro";
    public static final int COUNT = 9;
    public static final String KEY = "somearbitrarycrazystringthatdoesnotmatter";

    // We check root password per portal container
    private static HashMap<String, Boolean> setup = new HashMap<String, Boolean>();

    private static String getPCName() {
        return ExoContainerContext.getCurrentContainer().getContext().getPortalContainerName();
    }

    public static void checkJcrFlag() {
        setup.put(getPCName(), false);
        try {
            RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer()
                    .getComponentInstanceOfType(RepositoryService.class);
            Session session = repoService.getRepository(REPOSITORY_NAME).getSystemSession(WORKSPACE_NAME);
            if (session.itemExists("/" + SETUP_FLAG))
                setup.put(getPCName(), true);
            session.logout();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setJcrFlag() {
        try {
            RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer()
                    .getComponentInstanceOfType(RepositoryService.class);
            Session session = repoService.getRepository(REPOSITORY_NAME).getSystemSession(WORKSPACE_NAME);
            session.getRootNode().addNode(SETUP_FLAG);
            session.save();
            session.logout();
            setup.put(getPCName(), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String rootPassword() {

        String password = null;
        try {
            password = decodePassword(System.getProperty(ROOT_PASSWORD_PROPERTY));
            if (password == null) {
                password = randomPassword();
                setup.put(getPCName(), false);
            } else {
                setup.put(getPCName(), true);
                setJcrFlag();
            }
            return password;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decodePassword(String encodedPassword) throws Exception {

        if (encodedPassword == null) {
            return null;
        }

        byte[] salt = SALT.substring(0, 8).getBytes();
        int count = COUNT;
        char[] password = KEY.toCharArray();
        PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec keySpec = new PBEKeySpec(password);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
        SecretKey cipherKey = factory.generateSecret(keySpec);
        return PBEUtils.decode64(encodedPassword, "PBEwithMD5andDES", cipherKey, cipherSpec);
    }

    /**
     * Encodes a plain text password using PBEwithMD5andDES and Base64.
     *
     * @param plainTextPassword
     * @return
     * @throws Exception
     * @throws UnsupportedEncodingException
     * @throws PortalSetupCommand.SetupCommandException
     */
    public static String encodePassword(String plainTextPassword) throws Exception {

        if (plainTextPassword == null) {
            return null;
        }

        String encodedPassword = null;
        byte[] salt = SALT.substring(0, 8).getBytes();
        int count = COUNT;
        char[] password = KEY.toCharArray();
        PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec keySpec = new PBEKeySpec(password);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
        SecretKey cipherKey = factory.generateSecret(keySpec);
        encodedPassword = PBEUtils.encode64(plainTextPassword.getBytes("UTF-8"), "PBEwithMD5andDES", cipherKey, cipherSpec);
        return encodedPassword;
    }

    /**
     * PortalContainer's name matches with ServletContextName.
     * @see org.exoplatform.container.RootContainer
     */
    public static boolean isSetup(String context) {
        return (setup.get(context) != null ? setup.get(context) : false);
    }

    public static boolean isSetup() {
        return isSetup(getPCName());
    }

    public static void setFlag() {
        setup.put(getPCName(), true);
    }

    private static String randomPassword() {
        return new BigInteger(130, new SecureRandom()).toString(8);
    }
}
