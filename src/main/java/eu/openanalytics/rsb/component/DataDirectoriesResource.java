/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of Open Analytics NV, 2010-2020
 *
 *   ===========================================================================
 *
 *   This file is part of R Service Bus.
 *
 *   R Service Bus is free software: you can redistribute it and/or modify
 *   it under the terms of the Apache License as published by
 *   The Apache Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   Apache License for more details.
 *
 *   You should have received a copy of the Apache License
 *   along with R Service Bus.  If not, see <http://www.apache.org/licenses/>.
 *
 */

package eu.openanalytics.rsb.component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.Util;
import eu.openanalytics.rsb.rest.types.Directory;
import eu.openanalytics.rsb.rest.types.FileType;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component("dataDirectoriesResource")
@Path("/" + Constants.DATA_DIR_PATH)
// Produces "regular" XML/JSON content types as browsers don't understand subtypes
// correctly
@Produces({Constants.JSON_CONTENT_TYPE, Constants.XML_CONTENT_TYPE, Constants.RSB_XML_CONTENT_TYPE,
    Constants.RSB_JSON_CONTENT_TYPE})
public class DataDirectoriesResource extends AbstractResource
{
    private final Map<String, File> rootMap = new HashMap<String, File>();

    // exposed for unit testing
    Map<String, File> getRootMap()
    {
        return rootMap;
    }

    @PostConstruct
    public void setupRootMap() throws IOException
    {
        final List<File> dataDirectoryRoots = getConfiguration().getDataDirectories();
        if (dataDirectoryRoots != null)
        {
            for (final File dataDirectoryRoot : dataDirectoryRoots)
            {
                final String rootKey = Base64.encodeBase64URLSafeString(DigestUtils.md5Digest(dataDirectoryRoot.getCanonicalPath()
                    .getBytes()));
                rootMap.put(rootKey, dataDirectoryRoot);
            }
        }
    }

    @Path("/")
    @GET
    public Directory browseRoots(@Context final HttpHeaders httpHeaders, @Context final UriInfo uriInfo)
        throws URISyntaxException, IOException
    {
        final Directory roots = Util.REST_OBJECT_FACTORY.createDirectory();
        roots.setPath("/");
        roots.setName("Remote Data");
        roots.setUri(Util.buildDataDirectoryUri(httpHeaders, uriInfo, "/").toString());
        roots.setEmpty(rootMap.isEmpty());

        for (final Entry<String, File> rootEntry : rootMap.entrySet())
        {
            final Directory root = Util.REST_OBJECT_FACTORY.createDirectory();
            final File rootDirectory = rootEntry.getValue();
            root.setPath(rootDirectory.getCanonicalPath());
            root.setName(rootDirectory.getName());
            root.setUri(Util.buildDataDirectoryUri(httpHeaders, uriInfo, rootEntry.getKey()).toString());
            root.setEmpty(isDirectoryEmpty(rootDirectory));
            roots.getDirectories().add(root);
        }

        return roots;
    }

    @Path("/{rootId}{b64extension : (/b64extension)?}")
    @GET
    public Directory browsePath(@PathParam("rootId") final String rootId,
                                @PathParam("b64extension") final String b64extension,
                                @Context final HttpHeaders httpHeaders,
                                @Context final UriInfo uriInfo) throws URISyntaxException, IOException
    {

        final File rootDataDir = rootMap.get(rootId);
        if (rootDataDir == null)
        {
            throw new NotFoundException(new RuntimeException("No root data dir configured"));
        }

        final String extension = (b64extension != null ? new String(Base64.decodeBase64(b64extension)) : "");
        final File targetDataDir = new File(rootDataDir, extension);

        if (!targetDataDir.exists())
        {
            throw new NotFoundException(new RuntimeException("Invalid root data dir: " + targetDataDir));
        }

        // ensure the target data dir is below the root dir to prevent tampering
        final String rootDataDirCanonicalPath = rootDataDir.getCanonicalPath();
        final String targetDataDirCanonicalPath = targetDataDir.getCanonicalPath();
        if (!StringUtils.startsWith(targetDataDirCanonicalPath, rootDataDirCanonicalPath))
        {
            throw new AccessDeniedException("Target data dir: " + targetDataDirCanonicalPath
                                            + " is not below root dir: " + rootDataDirCanonicalPath);
        }

        final Directory result = Util.REST_OBJECT_FACTORY.createDirectory();
        result.setPath(rootDataDirCanonicalPath + extension);
        result.setName(targetDataDir.getName());
        result.setUri(Util.buildDataDirectoryUri(httpHeaders, uriInfo, rootId, b64extension).toString());

        final File[] targetDataDirFiles = targetDataDir.listFiles();
        result.setEmpty(targetDataDirFiles.length == 0);

        for (final File child : targetDataDirFiles)
        {
            if (FileUtils.isSymlink(child))
            {
                getLogger().warn("Symlinks are not supported: " + child);
            }
            else if (child.isFile())
            {
                final FileType fileType = Util.REST_OBJECT_FACTORY.createFileType();
                fileType.setPath(child.getCanonicalPath());
                fileType.setName(child.getName());
                result.getFiles().add(fileType);
            }
            else if (child.isDirectory())
            {
                final Directory childDir = Util.REST_OBJECT_FACTORY.createDirectory();
                childDir.setPath(child.getCanonicalPath());
                childDir.setName(child.getName());
                final String childB64extension = Base64.encodeBase64URLSafeString(StringUtils.difference(
                    rootDataDirCanonicalPath, child.getCanonicalPath()).getBytes());
                childDir.setUri(Util.buildDataDirectoryUri(httpHeaders, uriInfo, rootId, childB64extension)
                    .toString());
                childDir.setEmpty(isDirectoryEmpty(child));
                result.getDirectories().add(childDir);
            }
            else
            {
                getLogger().warn("Unsupported file type: " + child);
            }
        }

        return result;
    }

    private boolean isDirectoryEmpty(final File file)
    {
        if (!file.isDirectory())
        {
            return true;
        }
        final File[] childFiles = file.listFiles();
        return childFiles == null ? true : childFiles.length == 0;
    }
}
