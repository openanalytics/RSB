@REM    R Service Bus
@REM    
@REM    Copyright (c) Copyright of Open Analytics NV, 2010-2014
@REM 
@REM    ===========================================================================
@REM 
@REM    This program is free software: you can redistribute it and/or modify
@REM    it under the terms of the GNU Affero General Public License as published by
@REM    the Free Software Foundation, either version 3 of the License, or
@REM    (at your option) any later version.
@REM 
@REM    This program is distributed in the hope that it will be useful,
@REM    but WITHOUT ANY WARRANTY; without even the implied warranty of
@REM    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
@REM    GNU Affero General Public License for more details.
@REM 
@REM    You should have received a copy of the GNU Affero General Public License
@REM    along with this program.  If not, see <http://www.gnu.org/licenses/>.
@REM 
@REM    @author rsb.development@openanalytics.eu

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

