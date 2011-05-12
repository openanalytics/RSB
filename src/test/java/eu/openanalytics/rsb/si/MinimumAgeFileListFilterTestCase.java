/*
 *   R Service Bus
 *   
 *   Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
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

package eu.openanalytics.rsb.si;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author "OpenAnalytics <rsb.development@openanalytics.eu>"
 */
@RunWith(MockitoJUnitRunner.class)
public class MinimumAgeFileListFilterTestCase {

    @Mock
    private File testFile;

    private MinimumAgeFileListFilter filter;

    @Before
    public void prepareTest() {
        filter = new MinimumAgeFileListFilter();
        filter.setMinimumAge(10000);

        when(testFile.exists()).thenReturn(true);
    }

    @Test
    public void acceptFile() {
        when(testFile.lastModified()).thenReturn(0L);
        assertThat(filter.accept(testFile), is(true));
    }

    @Test
    public void rejectFile() {
        when(testFile.lastModified()).thenReturn(System.currentTimeMillis());
        assertThat(filter.accept(testFile), is(false));
    }
}
