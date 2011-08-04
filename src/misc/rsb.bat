rem
rem    R Service Bus
rem    
rem    Copyright (c) Copyright of OpenAnalytics BVBA, 2010-2011
rem 
rem    ===========================================================================
rem 
rem    This program is free software: you can redistribute it and/or modify
rem    it under the terms of the GNU Affero General Public License as published by
rem    the Free Software Foundation, either version 3 of the License, or
rem    (at your option) any later version.
rem 
rem    This program is distributed in the hope that it will be useful,
rem    but WITHOUT ANY WARRANTY; without even the implied warranty of
rem    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem    GNU Affero General Public License for more details.
rem 
rem    You should have received a copy of the GNU Affero General Public License
rem    along with this program.  If not, see <http://www.gnu.org/licenses/>.
rem 
rem    @author rsb.development@openanalytics.eu
rem

if not "%R_HOME%" == "" goto okRHome
echo "R_HOME not set"
goto end

:okRHome
if not "%R_LIBS%" == "" goto okRLibs
echo "R_LIBS not set"
goto end

:okRLibs
echo Using R_HOME:          "%R_HOME%"
echo Using R_LIBS:          "%R_LIBS%"

set "CURRENT_DIR=%cd%"
set "EXECUTABLE=%CURRENT_DIR%\startup.bat"
call "%EXECUTABLE%" %CMD_LINE_ARGS%

:end

