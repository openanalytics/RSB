/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2014
 *
 *   ===========================================================================
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.openanalytics.rsb.rservi.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;

/**
 * Variant of <code>de.walware.rj.servi.webapp.ServletRJContext</code> that can locate RJ JARs in
 * the classpath and that supports Maven-versioned JARs.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class ServletRJContext extends RJContext
{
    private final ServletContext servletContext;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public ServletRJContext(final ServletContext context)
    {
        this.servletContext = context;
    }

    @Override
    protected String[] getLibDirPaths() throws RjInvalidConfigurationException
    {
        try
        {
            final URL rjResourceUrl = ServletRJContext.class.getClassLoader().getResource(
                RJContext.class.getName().replace('.', '/') + ".class");

            final String rjResourcePath = rjResourceUrl.getPath();
            final int indexOfColon = rjResourcePath.indexOf(':');
            final int indexOfBang = rjResourcePath.indexOf('!');

            final String rjJarsPath = new File(rjResourcePath.substring(indexOfColon + 1, indexOfBang)).getParentFile()
                .getCanonicalPath();

            final String webInfLibPath = servletContext.getRealPath("WEB-INF/lib");

            final HashSet<String> uniqueLibDirPaths = new HashSet<String>(Arrays.asList(rjJarsPath,
                webInfLibPath));
            servletContext.log("Collected lib dir paths: " + uniqueLibDirPaths);

            return uniqueLibDirPaths.toArray(EMPTY_STRING_ARRAY);
        }
        catch (final IOException ioe)
        {
            throw new RjInvalidConfigurationException("Failed to collect lib dir paths", ioe);
        }
    }

    @Override
    protected PathEntry searchLib(final List<PathEntry> files, final String libId)
    {
        final Pattern pattern = Pattern.compile(".*" + File.separatorChar + Pattern.quote(libId)
                                                + "([-_]{1}.*)?\\.jar$");

        for (final PathEntry entry : files)
        {
            if (pattern.matcher(entry.getPath()).matches())
            {
                return entry;
            }
        }

        return null;
    }

    @Override
    public String getServerPolicyFilePath() throws RjInvalidConfigurationException
    {
        String path = servletContext.getRealPath("WEB-INF/lib");
        final int length = path.length();
        if ((length == 0)
            || ((path.charAt(length - 1) != '/') && (path.charAt(length - 1) != File.separatorChar)))
        {
            path = path + File.separatorChar;
        }
        return path + "security.policy";
    }

    @Override
    protected String getPropertiesDirPath()
    {
        return "/WEB-INF/";
    }

    @Override
    protected InputStream getInputStream(final String path) throws IOException
    {
        return this.servletContext.getResourceAsStream(path);
    }

    @Override
    protected OutputStream getOutputStream(final String path) throws IOException
    {
        final String realPath = this.servletContext.getRealPath(path);
        if (realPath == null)
        {
            throw new IOException("Writing to '" + path + "' not supported.");
        }
        final File file = new File(realPath);
        if (!file.exists())
        {
            file.createNewFile();
        }
        return new FileOutputStream(file, false);
    }
}
