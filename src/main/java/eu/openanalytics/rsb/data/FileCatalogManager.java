/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2013
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

package eu.openanalytics.rsb.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import eu.openanalytics.rsb.Constants;
import eu.openanalytics.rsb.component.AbstractComponent;
import eu.openanalytics.rsb.config.Configuration.CatalogSection;
import eu.openanalytics.rsb.config.Configuration.DepositDirectoryConfiguration;
import eu.openanalytics.rsb.config.Configuration.DepositEmailConfiguration;

/**
 * A file-based optionally-aware file catalog.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@Component
public class FileCatalogManager extends AbstractComponent implements CatalogManager
{
    @PostConstruct
    public void createCatalogTree() throws IOException
    {
        final Set<String> applicationNames = getConfiguration().isApplicationAwareCatalog()
                                                                                           ? collectionAllApplicationNames()
                                                                                           : Collections.singleton("ignored");
        for (final String applicationName : applicationNames)
        {
            for (final CatalogSection catalogSection : CatalogSection.values())
            {
                FileUtils.forceMkdir(getCatalogSectionDirectory(catalogSection, applicationName));
            }
        }
    }

    private Set<String> collectionAllApplicationNames()
    {
        final Set<String> applicationNames = new HashSet<String>();

        if (getConfiguration().getApplicationSecurityConfiguration() != null)
        {
            applicationNames.addAll(getConfiguration().getApplicationSecurityConfiguration().keySet());
        }

        if (getConfiguration().getDepositRootDirectories() != null)
        {
            for (final DepositDirectoryConfiguration ddc : getConfiguration().getDepositRootDirectories())
            {
                applicationNames.add(ddc.getApplicationName());
            }
        }

        if (getConfiguration().getDepositEmailAccounts() != null)
        {
            for (final DepositEmailConfiguration dec : getConfiguration().getDepositEmailAccounts())
            {
                applicationNames.add(dec.getApplicationName());
            }
        }

        if (getConfiguration().getApplicationSecurityConfiguration() != null)
        {
            applicationNames.addAll(getConfiguration().getApplicationSecurityConfiguration().keySet());
        }

        return applicationNames;
    }

    @PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
    public Map<Pair<CatalogSection, File>, List<File>> getCatalog(final String applicationName)
    {
        final Map<Pair<CatalogSection, File>, List<File>> catalog = new HashMap<Pair<CatalogSection, File>, List<File>>();

        for (final CatalogSection catalogSection : CatalogSection.values())
        {
            final File catalogSectionDirectory = getCatalogSectionDirectory(catalogSection, applicationName);

            catalog.put(Pair.of(catalogSection, catalogSectionDirectory),
                Arrays.asList(catalogSectionDirectory.listFiles(Constants.FILE_ONLY_FILTER)));
        }

        return catalog;
    }

    @PreAuthorize("hasPermission(#applicationName, 'APPLICATION_USER')")
    public File getCatalogFile(final CatalogSection catalogSection,
                               final String applicationName,
                               final String fileName)
    {
        final File catalogSectionDirectory = getCatalogSectionDirectory(catalogSection, applicationName);
        return new File(catalogSectionDirectory, fileName);
    }

    @PreAuthorize("hasPermission(#applicationName, 'APPLICATION_ADMIN')")
    public Pair<PutCatalogFileResult, File> putCatalogFile(final CatalogSection catalogSection,
                                                           final String applicationName,
                                                           final String fileName,
                                                           final InputStream in) throws IOException
    {
        final File catalogSectionDirectory = getCatalogSectionDirectory(catalogSection, applicationName);

        final File catalogFile = new File(catalogSectionDirectory, fileName);
        final boolean preExistingFile = catalogFile.isFile();

        final FileWriter fw = new FileWriter(catalogFile);
        IOUtils.copy(in, fw);
        IOUtils.closeQuietly(fw);

        final PutCatalogFileResult putCatalogFileResult = preExistingFile
                                                                         ? PutCatalogFileResult.UPDATED
                                                                         : PutCatalogFileResult.CREATED;

        getLogger().info(
            StringUtils.capitalize(putCatalogFileResult.toString().toLowerCase()) + " " + fileName
                            + " in catalog section " + catalogSection.toString() + " as file: " + catalogFile);

        return Pair.of(putCatalogFileResult, catalogFile);
    }

    private File getCatalogSectionDirectory(final CatalogSection catalogSection, final String applicationName)
    {
        if ((getConfiguration().isApplicationAwareCatalog()) && (StringUtils.isBlank(applicationName)))
        {
            throw new IllegalArgumentException(
                "Failed to access the catalog because no application name has been provided but RSB is running with an application aware catalog.");
        }

        final File actualCatalogRoot = getConfiguration().isApplicationAwareCatalog()
                                                                                     ? new File(
                                                                                         getConfiguration().getCatalogRootDirectory(),
                                                                                         applicationName)
                                                                                     : getConfiguration().getCatalogRootDirectory();

        return new File(actualCatalogRoot, catalogSection.getSubDir());
    }
}
